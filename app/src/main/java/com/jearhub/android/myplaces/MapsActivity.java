package com.jearhub.android.myplaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jearhub.android.myplaces.Model.MyPlaces;
import com.jearhub.android.myplaces.Model.Results;
import com.jearhub.android.myplaces.Remote.IGoogleAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int MY_PERMISSION_CODE = 1000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private double latitude, longitude;
    private Location mLastLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;

    IGoogleAPIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_maps );
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById ( R.id.map );
        mapFragment.getMapAsync ( this );

        //Init Service
        mService = Common.getGoogleAPIService ();

        //Request Runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission ();
        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById ( R.id.bottom_navigation );
        bottomNavigationView.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId ())
                {
                    case R.id.action_Supermarket:
                        nearByPlace("Supermarket");
                        break;
                    case R.id.action_Laundry:
                        nearByPlace("Laundry");
                        break;
                    case R.id.action_Gym:
                        nearByPlace("Gym");
                        break;
                    case R.id.action_Hospital:
                        nearByPlace("Hospital");
                        break;
                    default:
                            break;
                }

                return true;
            }
        } );
    }

    private void nearByPlace(final String placeType) {
        mMap.clear ();
        String url = getUrl(latitude,longitude,placeType);

        mService.getNearByPlaces ( url )
                .enqueue ( new Callback<MyPlaces> () {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                        if (response.isSuccessful ())
                        {
                            for (int i = 0; i < response.body ().getResults ().length;i++)
                            {
                                MarkerOptions markerOptions = new MarkerOptions ();
                                Results googlePlace = response.body ().getResults ()[i];
                                double lat = Double.parseDouble ( googlePlace.getGeometry ().getLocation ().getLat () );
                                double lng = Double.parseDouble ( googlePlace.getGeometry ().getLocation ().getLng () );
                                String placeName = googlePlace.getName ();
                                String vicinity = googlePlace.getVicinity ();
                                LatLng latLng = new LatLng ( lat,lng );
                                markerOptions.position ( latLng );
                                markerOptions.title ( placeName );
                                if (placeType.equals ( "Supermarket" ))
                                    markerOptions.icon ( BitmapDescriptorFactory.fromResource ( R.drawable.ic_shopping_cart_black_24dp ) );
                                else if (placeType.equals ( "Laundry" ))
                                    markerOptions.icon ( BitmapDescriptorFactory.fromResource ( R.drawable.ic_local_laundry_service_black_24dp ) );
                                else if (placeType.equals ( "Gym" ))
                                    markerOptions.icon ( BitmapDescriptorFactory.fromResource ( R.drawable.ic_fitness_center_black_24dp ) );
                                else if (placeType.equals ( "Hospital" ))
                                    markerOptions.icon ( BitmapDescriptorFactory.fromResource ( R.drawable.ic_local_hospital_black_24dp ) );
                                else
                                    markerOptions.icon ( BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_RED));

                                //Move camera
                                mMap.moveCamera ( CameraUpdateFactory.zoomTo ( 11 ) );
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                } );
    }

    private String getUrl(double latitude, double longitude, String placeType) {
        return "";
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale ( this, Manifest.permission.ACCESS_FINE_LOCATION ))
                ActivityCompat.requestPermissions ( this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE );
            else
                ActivityCompat.requestPermissions ( this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE );
                return false;
            }
        else
            return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //init Google Play Services
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClien();
                mMap.setMyLocationEnabled ( true );
            }
        }
            else{
                buildGoogleApiClien();
                mMap.setMyLocationEnabled ( true );
            }
        }

    private synchronized void buildGoogleApiClien() {
        mGoogleApiClient = new GoogleApiClient.Builder (this)
                .addConnectionCallbacks ( this )
                .addOnConnectionFailedListener ( this )
                .addApi ( LocationServices.API )
                .build ();
        mGoogleApiClient.connect ();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest ();
        mLocationRequest.setInterval ( 1000 );
        mLocationRequest.setFastestInterval ( 1000 );
        mLocationRequest.setPriority ( LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY );
        if (ContextCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates ( mGoogleApiClient,mLocationRequest,this );
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect ();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mMarker != null)
            mMarker.remove ();

        latitude = location.getLatitude ();
        longitude = location.getLongitude ();

        LatLng latLng = new LatLng ( latitude,longitude );
        MarkerOptions markerOptions = new MarkerOptions ()
                .position ( latLng )
                .title ( "My Position" )
                .icon ( BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN) );

        //Move camera
        mMap.moveCamera ( CameraUpdateFactory.newLatLng ( latLng ) );
        mMap.animateCamera ( CameraUpdateFactory.zoomTo ( 11 ) );

        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates ( mGoogleApiClient, this );
    }
}
