package first.alexander.com.googlemaps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import DirectionFinderPackage.DirectionFinder;
import DirectionFinderPackage.DirectionFinderListener;
import DirectionFinderPackage.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, GoogleMap.OnMapClickListener{
    private GoogleMap mMap;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    //private Marker destinationPoint; (for TESTING MapOnClick)
    private ProgressDialog progressDialog;

    private LocationManager locationManager;

    private Double latitude;
    private Double longitude;
    private LatLng latLng;
    private boolean follow;

    private static final int LOC_PERMISSION_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String locProvider;
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locProvider  = LocationManager.NETWORK_PROVIDER;
            getLocation(locProvider);
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locProvider = LocationManager.GPS_PROVIDER;
            getLocation(locProvider);
        }
        else {
            Toast.makeText(this, "Please enable your GPS provider!", Toast.LENGTH_LONG).show();
        }

        Button btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });


        etDestination = (EditText) findViewById(R.id.etDestination);

        // Button that enables follows current location on map
        ToggleButton toggle = (ToggleButton) findViewById(R.id.followButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                follow = isChecked;
                if(isChecked)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.2f));
            }
        });

    }


    @Override
    public void onMapClick(LatLng latLng) {

        // Adding a new marker (FOR TESTING onMapClick)
        /*if(destinationPoint != null){
            destinationPoint.remove();
        }
        destinationPoint = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                .title("Picked Location")
                .position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.2f));*/



        if(latitude != null && longitude != null) {
            String origin = Double.toString(latitude) + "," + Double.toString(longitude);
            String destination = Double.toString(latLng.latitude) + "," + Double.toString(latLng.longitude);

            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        else{

         return;

        }
    }

    private void sendRequest() {
        // Reset duration and distance display
        String noPath = "0 km";
        ((TextView) findViewById(R.id.tvDuration)).setText(noPath);
        ((TextView) findViewById(R.id.tvDistance)).setText(noPath);

        String origin = Double.toString(latitude) + "," + Double.toString(longitude);
        String destination = etDestination.getText().toString();

        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Closes keyboard
        View viewFocus = this.getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait",
                "Finding direction...", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        // No route was found
        if(routes.isEmpty()) {
            Toast.makeText(this, "Path not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Route route : routes) {
            // TODO: Centre in centre of path, adjustable zoom
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, (float) 12.5));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.rgb(0, 155, 224)).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    private void getLocation(String provider) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION_CODE);
            return;
        }

        locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                boolean noLocation = false;

                // If a previous location did not exist, like when starting the app
                if (latitude == null || longitude == null)
                    noLocation = true;

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                latLng = new LatLng(latitude, longitude);

                // Move the camera to the current location
                if (follow || noLocation)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.2f));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if(status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                    Toast.makeText(MapsActivity.this, "Location provider is unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(MapsActivity.this, "Location provider enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MapsActivity.this, "Location provider disabled, please enable!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOC_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Recreates activity, should probably not do this
                    this.recreate();
                }
                else {
                    Toast.makeText(this, "Location permission required to function!", Toast.LENGTH_LONG).show();
                    // App crashes if you press anything lol
                }
                break;
            }
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }


}

