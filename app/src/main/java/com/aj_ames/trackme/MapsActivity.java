package com.aj_ames.trackme;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TrackGPS gps;
    double longitude, newLat, newLng, lat_url,lng_url;
    double latitude;
    private static String TAG = MapsActivity.class.getSimpleName();
    FloatingActionButton locator;
    public String url="http://api.thingspeak.com/channels/255333/feed/last.json";
    private ProgressDialog pDialog;
    public LatLng newlatlng,latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gps = new TrackGPS(MapsActivity.this);


        if(gps.canGetLocation()){


            longitude = gps.getLongitude();
            latitude = gps .getLatitude();

            //Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_LONG).show();
        }
        else
        {
            gps.showSettingsAlert();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    */

        newlatlng = new LatLng(lat_url, lng_url);
        //Toast.makeText(MapsActivity.this, String.valueOf(lat_url)+String.valueOf(lng_url),Toast.LENGTH_LONG).show();
        locator = (FloatingActionButton) findViewById(R.id.fab);
        locator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOldMark(lat_url, lng_url);
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        makeJsonObjectRequest();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        Geocoder gc = new Geocoder(MapsActivity.this);
        List<android.location.Address> list = null;
        try {
            list = gc.getFromLocation(latitude,longitude,1);
            newLat = latitude;
            newLng = longitude;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        android.location.Address add = list.get(0);
        String addressLine1 = add.getAddressLine(1);
        String addressLine2 = add.getAddressLine(2);
        latLng= new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(addressLine1).snippet(addressLine2)).setVisible(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 22));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }

    private void addOldMark(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);

        Geocoder gc = new Geocoder(MapsActivity.this);

        List<android.location.Address> list = null;

        //LatLng latLng = marker.getPosition();

        try {
            list = gc.getFromLocation(lat_url, lng_url,1);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        android.location.Address add = list.get(0);
        String addressLine1 = add.getAddressLine(0);
        String addressLine2 = add.getAddressLine(2);
        mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).title("Child's location ").snippet(addressLine1));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 22));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);


        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, lat_url, lng_url, results);
        float distance = results[0];

        double dist = (double) distance / 1000;
        DecimalFormat f = new DecimalFormat("##.00");

        Toast.makeText(getApplicationContext(), "Distance to Travel is: " + f.format(dist) + " km", Toast.LENGTH_LONG).show();

    }

    private void makeJsonObjectRequest() {

        showpDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {

                    lat_url = response.getDouble("field1");
                    lng_url = response.getDouble("field2");


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidepDialog();
            }
        });

        // Adding request to request queue
        AppCont.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
