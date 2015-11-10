package com.easyapp.googlemap_route_record;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.easyapp.googlemap_route_record.model.Point;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    private double distance = 0;
    private TextView tv_LatitudeText, tv_LongitudeText;
    private TextView tv_distance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_LatitudeText = (TextView) findViewById(R.id.tv_LatitudeText);
        tv_LongitudeText = (TextView) findViewById(R.id.tv_LongitudeText);
        tv_distance = (TextView) findViewById(R.id.tv_distance);

        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected void startLocationUpdates() {
        Log.d("startLocationUpdates", "startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("onConnected", "onConnected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mCurrentLocation != null) {
            Log.d("FusedLocationApi", "FusedLocationApi");
            tv_LatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
            tv_LongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            updateUI();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("connectionResult", "" + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO something
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    private void updateUI() {
        Log.d("updateUI", "updateUI");
        tv_LatitudeText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        tv_LongitudeText.setText(String.valueOf(mCurrentLocation.getLongitude()));

        Point point = new Point();
        point.setLatitude(mCurrentLocation.getLatitude() + "");
        point.setLongitude(mCurrentLocation.getLongitude() + "");
        point.save();

        List<Point> points = Point.getAll();
        Logger.d(points.size() + "");
        ArrayList<LatLng> latLngs = new ArrayList<>();
        PolylineOptions polyLineOptions = new PolylineOptions();

        // traversing through routes
        for (int i = 0; i < points.size(); i++) {

            Point item = points.get(i);
            double lat = Double.parseDouble(item.getLatitude());
            double lng = Double.parseDouble(item.getLongitude());
            LatLng position = new LatLng(lat, lng);
            latLngs.add(position);
            if (i > 0) {
                Point old_item = points.get(i - 1);
                double old_lat = Double.parseDouble(old_item.getLatitude());
                double old_lng = Double.parseDouble(old_item.getLongitude());
                distance = distance + DistanceCalculator.distance(lat, lng, old_lat, old_lng, "K");
                Logger.d(DistanceCalculator.distance(lat, lng, old_lat, old_lng, "K") + "");
                Logger.d("distance: " + distance + "");
            }
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat("##.#####");
        distance = Double.parseDouble(df.format(distance));

        tv_distance.setText(distance + " Kilometers\n");
        polyLineOptions.addAll(latLngs);
        polyLineOptions.width(2);
        polyLineOptions.color(Color.BLUE);
        mMap.addPolyline(polyLineOptions);
        Logger.d("addPolyline");

    }
}
