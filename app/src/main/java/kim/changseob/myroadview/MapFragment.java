package kim.changseob.myroadview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapLayout;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import kim.changseob.myroadview.widget.RoadViewPopupMenu;

public class MapFragment extends Fragment
        implements MapView.OpenAPIKeyAuthenticationResultListener,
        MapView.MapViewEventListener,
        MapView.POIItemEventListener,
        LocationListener {

    final String TAG = "MapFragment";

    ViewGroup mapViewContainer;

    public Handler mHandler;
    private RoadViewPopupMenu mPopupMenu;
    View dummyViewForAnchor;

    private MapView mMapView;

    private LocationManager mLocationManager;
    private Location mInitialLocation = null;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mHandler = new Handler();

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        // based on Kakao Map API docs
        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, this);

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapViewContainer = v.findViewById(R.id.map_view_root);
        MapLayout mapLayout = new MapLayout(getActivity());
        mMapView = mapLayout.getMapView();
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setDaumMapApiKey(getKaKaoAPIKeyFromManifest());
        mMapView.setMapType(MapView.MapType.Standard);

        mapViewContainer.addView(mapLayout);

        dummyViewForAnchor = new View(getContext());
        dummyViewForAnchor.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        dummyViewForAnchor.setBackgroundColor(Color.TRANSPARENT);

        mapViewContainer.addView(dummyViewForAnchor);
        dummyViewForAnchor.setX(mapViewContainer.getX() + mapViewContainer.getWidth() / 2);
        dummyViewForAnchor.setY(mapViewContainer.getY() + mapViewContainer.getHeight() / 2);

        return v;
    }

    private String getKaKaoAPIKeyFromManifest() {
        String key = "";
        try {
            ApplicationInfo app = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            key = bundle.getString("com.kakao.sdk.AppKey");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return key;
    }

    @Override
    public void onLocationChanged(Location location) {
        mInitialLocation = location;

        Log.i(TAG, "initial location: (" + mInitialLocation.getLatitude() + ", " + mInitialLocation.getLongitude() + "), accuracy: " + mInitialLocation.getAccuracy() + "m");

        MapPoint currentMapPoint = MapPoint.mapPointWithGeoCoord(mInitialLocation.getLatitude(),mInitialLocation.getLongitude());
        mMapView.setMapCenterPoint(currentMapPoint, false);

        // 1px = 1m in ZoomLevel3
        mMapView.setZoomLevel(3, false);

        // draw pin on current(initial) location
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Show Road Views");
        marker.setTag(0);
        marker.setMapPoint(currentMapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mMapView.addPOIItem(marker);
        mMapView.setPOIItemEventListener(this);
        // draw circle for accuracy
        MapCircle locationAccuracyCircle = new MapCircle(currentMapPoint, (int)mInitialLocation.getAccuracy(), Color.argb(255, 255,0,255), Color.argb(91, 216,191,216));
        mMapView.addCircle(locationAccuracyCircle);

        // set proper zoom level
        mMapView.setZoomLevel(1, true);

        // remove initial updates
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.i(TAG, "MapView had loaded. Now, MapView APIs could be called safely");
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.537229,127.005515), 2, true);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {
        Log.i(TAG,	String.format("Open API Key Authentication Result : code=%d, message=%s", i, s));
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        // move camera to selected poi item
        mapView.setMapCenterPoint(mapPOIItem.getMapPoint(), true);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        if(mapPOIItem.getTag() == 0) { // current position

//            Intent intent = new Intent(this, MyRoadViewActivity.class);
//            startActivity(intent);
            dummyViewForAnchor.setX(mapViewContainer.getX() + mapViewContainer.getWidth() / 2);
            dummyViewForAnchor.setY(mapViewContainer.getY() + mapViewContainer.getHeight() / 2);
            Log.e(TAG, "X: " + dummyViewForAnchor.getX() + ",  Y: " + dummyViewForAnchor.getY());
            RoadViewPopupMenu popupMenu = new RoadViewPopupMenu(getContext(), dummyViewForAnchor);
            String[] menus = {"Show Kakao Roadview", "Show My Roadview", "Add My Roadview"};
            popupMenu.SetMenu(menus);
            popupMenu.show();
        }
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
