package com.example.map_location;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;

import mapapi.clusterutil.clustering.ClusterManager;
import mapapi.overlayutil.WalkingRouteOverlay;

public class MapActivity extends AppCompatActivity implements View.OnClickListener {

    private MapView mMapView;

    /**
     * 请输入需要搜索的内容
     */
    private EditText mEtSearch;
    /**
     * 搜索
     */
    private Button mBtnSearch;
    private RecyclerView mRv;

    //百度地图的数据操作
    private BaiduMap baiduMap;
    //百度地图定位的类
    private LocationClient locationClient;


    /******************检索********************/

    private SearchItemAdapter searchItemAdapter;
    private ArrayList<PoiInfo> poiInfos;
    private PoiSearch poiSearch;

    /**
     * 请输入我的所在位置
     */
    private EditText mEtStart;
    /**
     * 请我的终止位置
     */
    private EditText mEtEnd;
    /**
     * 搜索
     */
    private Button mBtnNotSearch;
    private RecyclerView mRvNot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_map );
        initView();
        initMap();
        initLocation();

        //初始化检索
        initPoi();

        //初始化路径规
        initRoutePlan();
    }

    private void initView() {
        mMapView = (MapView) findViewById( R.id.mapView );
        mMapView.setOnClickListener( this );


        mEtSearch = (EditText) findViewById( R.id.et_search );
        mBtnSearch = (Button) findViewById( R.id.btn_search );
        mBtnSearch.setOnClickListener( this );
        mRv = (RecyclerView) findViewById( R.id.rv );

        mRv.addItemDecoration( new DividerItemDecoration( this, DividerItemDecoration.VERTICAL ) );
        mMapView = (MapView) findViewById( R.id.mapView );
        mMapView.setOnClickListener( this );
        mEtStart = (EditText) findViewById( R.id.et_start );
        mEtEnd = (EditText) findViewById( R.id.et_end );
        mBtnNotSearch = (Button) findViewById( R.id.btn_not_search );
        mBtnNotSearch.setOnClickListener( this );
        mRvNot = (RecyclerView) findViewById( R.id.rv_not );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    private void initMap() {
        baiduMap = mMapView.getMap();
        //baiduMap.setMyLocationEnabled( true );
        //设置为普通类型的地图
        //baiduMap.setMapType( BaiduMap.MAP_TYPE_SATELLITE );

        //自定义地图
        baiduMap.setTrafficEnabled( true );
        baiduMap.setCustomTrafficColor( "#ffba0101", "#fff33131", "#ffff9e19", "#00000000" );
        //对地图状态做更新，否则可能不会触发渲染，造成样式定义无法立即生效。
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo( 13 );
        baiduMap.animateMapStatus( u );
        baiduMap.setMapType( BaiduMap.MAP_TYPE_NORMAL );

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.mapView:
                break;
            case R.id.btn_search:
                search();
                break;
            case R.id.btn_not_search:
                searchRoute();
                break;
        }
    }

    /**
     * 初始化定位
     */

    private void initLocation() {
        //定义初始化
        locationClient = new LocationClient( this );
        LocationClientOption option = new LocationClientOption();

        //打开gps
        option.setOpenGps( true );

        //设置坐标类型
        option.setCoorType( "bd0911" );
        option.setScanSpan( 1000 );

        //设置locationClientOption
        locationClient.setLocOption( option );

        //注册LocationListener 监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener( myLocationListener );
        //开启地图定位图层
        locationClient.start();


    }

    /**
     * 地图监听事件
     */
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不在处理新接收的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy( bdLocation.getRadius() )
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction( bdLocation.getDirection() ).latitude( bdLocation.getLatitude() )
                    .longitude( bdLocation.getLongitude() ).build();
            baiduMap.setMyLocationData( locData );

        }
    }


    /**
     * 以当前的经纬度为圆心绘制一个圆
     *
     * @param lat
     * @param gt
     */
    private void drawCircle(double lat, double gt) {
        //设置圆心位置
        LatLng center = new LatLng( lat, gt );
        //设置圆对象
        CircleOptions circleOptions = new CircleOptions().center( center )
                .radius( 200 )
                .fillColor( 0x50ff0000 )
                .stroke( new Stroke( 2, 0xff000000 ) ); //设置边框的宽度和颜色
        baiduMap.clear();
        //在地图上添加显示圆
        baiduMap.addOverlay( circleOptions );

    }


    private void addMark(double lat, double gt) {
        //定义Maker坐标点
        LatLng point = new LatLng( lat, gt );
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource( R.drawable.notation );
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position( point )
                .icon( bitmap );
        //在地图上添加Marker，并显示
        baiduMap.addOverlay( option );
        String s = baiduMap.toString();
        Log.e( "111222", "addMark: "+s);
    }

    /*******************检索**********************/
    private void initPoi() {
        poiInfos = new ArrayList<>();
        searchItemAdapter = new SearchItemAdapter( this, poiInfos );
        mRv.setLayoutManager( new LinearLayoutManager( this ) );
        mRv.setAdapter( searchItemAdapter );

        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener( poiSearchResultListener );

        searchItemAdapter.setIOnClickItem( new SearchItemAdapter.IOnClickItem() {
            @Override
            public void iOnClickItem(int position) {
                PoiInfo poiInfo = poiInfos.get( position );
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng( poiInfo.location );
                baiduMap.setMapStatus( mapStatusUpdate );
                drawCircle( poiInfo.location.latitude, poiInfo.location.longitude );
                addMark( poiInfo.location.latitude, poiInfo.location.longitude );


            }
        } );

    }

    /**
     * 搜索
     */
    private void search() {
        String search = mEtSearch.getText().toString();
        if (!TextUtils.isEmpty( search )) {
            PoiCitySearchOption poiCitySearchOption = new PoiCitySearchOption();
            poiCitySearchOption.city( search );
            poiCitySearchOption.keyword( search );
            poiSearch.searchInCity( poiCitySearchOption );

        }

    }

    /**
     * 搜索的监听
     */
    OnGetPoiSearchResultListener poiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            Log.i( TAG, "onGetPoiResult" );
            poiInfos.clear();
            if (poiResult.getAllPoi() != null && poiResult.getAllPoi().size() > 0) {
                poiInfos.addAll( poiResult.getAllPoi() );
                searchItemAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            Log.i( TAG, "onGetPoiDetailResult" );
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
            Log.i( TAG, "onGetPoiDetailResult" );
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            Log.i( TAG, "onGetPoiIndoorResult" );
        }
    };

    /*******************************路径规划*************************/
    private RoutePlanSearch routePlanSearch;
    private PlanNode startNode,endNode; //开始和结束的坐标点
    SuggestionSearch suggestionSearch; //地点检索的类
    SuggestAdapter suggestAdapter; //路径规划搜索出来的列表
    ArrayList<SuggestionResult.SuggestionInfo> suggestList; //地点检索的结果
    boolean isStart = true; //当前处理的是否是起点
    LatLng startLatLng; //起点的经纬度

    //初始化路径规划
    private void initRoutePlan() {

        suggestionSearch = SuggestionSearch.newInstance();
        suggestList = new ArrayList<>();
        suggestAdapter = new SuggestAdapter(this,suggestList);
        mRvNot.setLayoutManager( new LinearLayoutManager( this ) );
        mRvNot.setAdapter( suggestAdapter);

        //设置监听地点检索
        suggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);

        mEtStart.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    isStart = true;
                    mRvNot.setVisibility(View.VISIBLE);
                }
            }
        });
        //监听起点输入框的变化
        mEtStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //起点输入改变以后获取对应的起点数据
                SuggestionSearchOption option = new SuggestionSearchOption();
                option.city("北京");
                option.citylimit(true);
                option.keyword(s.toString());
                suggestionSearch.requestSuggestion(option);
            }
        });
        //监听终点输入框的光标
        mEtEnd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    isStart = false;
                    mRvNot.setVisibility(View.VISIBLE);
                }
            }
        });
        //监听终点输入框的输入
        mEtEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //获取终点框对应的数据
                SuggestionSearchOption option = new SuggestionSearchOption();
                option.city("北京");
                option.citylimit(true);
                option.keyword(s.toString());
                suggestionSearch.requestSuggestion(option);
            }
        });

        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener( routePlanResultListener );

        suggestAdapter.setIOnClickItem( new SuggestAdapter.IOnClickItem() {
            @Override
            public void iOnClickItem(int position) {
                SuggestionResult.SuggestionInfo suggestionInfo = suggestList.get(position);
                if(isStart){
                    mEtStart.setText(suggestionInfo.getKey());
                    startLatLng = suggestionInfo.getPt();
                }else{
                    mEtEnd.setText(suggestionInfo.getKey());
                }
                mRvNot.setVisibility(View.GONE);
            }
        } );

    }

    /**
     * 地点检索监听
     */
    OnGetSuggestionResultListener suggestionResultListener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            //地点检索结果
            if(suggestionResult.getAllSuggestions()!=null){
                suggestList.clear();
                suggestList.addAll(suggestionResult.getAllSuggestions());
                suggestAdapter.notifyDataSetChanged();
            }

        }
    };

    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            Log.i( TAG, "onGetWalkingRouteResult" );

            //创建一个路径规划的类
            WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay( baiduMap );
            //判断当前查找出来的路线
            if (walkingRouteResult.getRouteLines() != null && walkingRouteResult.getRouteLines().size() > 0) {
                walkingRouteOverlay.setData( walkingRouteResult.getRouteLines().get( 0 ) );
                walkingRouteOverlay.addToMap();
            }
        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            Log.i( TAG, "onGetTransitRouteResult" );
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
            Log.i( TAG, "onGetMassTransitRouteResult" );
        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            Log.i( TAG, "onGetDrivingRouteResult" );
        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
            Log.i( TAG, "onGetIndoorRouteResult" );
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            Log.i( TAG, "onGetBikingRouteResult" );
        }
    };

    private void searchRoute(){
        String startName,endName;
        startName = mEtStart.getText().toString();
        endName = mEtEnd.getText().toString();
        if(TextUtils.isEmpty(startName) || TextUtils.isEmpty(endName)){
            Toast.makeText(this, "请输入正确起点和终点", Toast.LENGTH_SHORT).show();
        }else{
            startNode = PlanNode.withCityNameAndPlaceName("北京",startName);
            endNode = PlanNode.withCityNameAndPlaceName("北京",endName);
            WalkingRoutePlanOption option = new WalkingRoutePlanOption();
            option.from(startNode);
            option.to(endNode);
            //搜索路径
            routePlanSearch.walkingSearch(option);
        }
    }

    private static final String TAG = "MapActivity";
}