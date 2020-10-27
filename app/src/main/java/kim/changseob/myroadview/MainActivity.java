package kim.changseob.myroadview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapLayout;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kim.changseob.myroadview.widget.RoadViewPopupMenu;

public class MainActivity extends AppCompatActivity
        implements MapView.OpenAPIKeyAuthenticationResultListener, MapView.MapViewEventListener, MapView.POIItemEventListener, LocationListener{

    final String TAG = "MainActivity";

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    ViewGroup mapViewContainer;

    public Handler mHandler;
    private RoadViewPopupMenu mPopupMenu;
    View dummyViewForAnchor;

    private MapView mMapView;

    private LocationManager mLocationManager;
    private Location mInitialLocation = null;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        checkPermissions();

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, this);

        setContentView(R.layout.activity_main);

        mapViewContainer = (ViewGroup) findViewById(R.id.map_view_root);
        MapLayout mapLayout = new MapLayout(this);
        mMapView = mapLayout.getMapView();
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);
        mMapView.setMapViewEventListener(this);
        mMapView.setDaumMapApiKey(getKaKaoAPIKeyFromManifest());
        mMapView.setMapType(MapView.MapType.Standard);

        mapViewContainer.addView(mapLayout);

        dummyViewForAnchor = new View(this);
        dummyViewForAnchor.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        dummyViewForAnchor.setBackgroundColor(Color.TRANSPARENT);

        mapViewContainer.addView(dummyViewForAnchor);
        dummyViewForAnchor.setX(mapViewContainer.getX() + mapViewContainer.getWidth() / 2);
        dummyViewForAnchor.setY(mapViewContainer.getY() + mapViewContainer.getHeight() / 2);
    }


    private String getKaKaoAPIKeyFromManifest() {
        String key = "";
        try {
            ApplicationInfo app = getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            key = bundle.getString("com.kakao.sdk.AppKey");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return key;
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        // move camera to selected poi item
        mapView.setMapCenterPoint(mapPOIItem.getMapPoint(), true);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        if(mapPOIItem.getTag() == 0) { // current position

//            Intent intent = new Intent(this, MyRoadViewActivity.class);
//            startActivity(intent);
            dummyViewForAnchor.setX(mapViewContainer.getX() + mapViewContainer.getWidth() / 2);
            dummyViewForAnchor.setY(mapViewContainer.getY() + mapViewContainer.getHeight() / 2);
            Log.e(TAG, "X: " + dummyViewForAnchor.getX() + ",  Y: " + dummyViewForAnchor.getY());
            RoadViewPopupMenu popupMenu = new RoadViewPopupMenu(this, dummyViewForAnchor);
            String[] menus = {"Show Kakao Roadview", "Show My Roadview"};
            popupMenu.SetMenu(menus);
            popupMenu.show();
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {
        Log.i(TAG,	String.format("Open API Key Authentication Result : code=%d, message=%s", i, s));
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
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
