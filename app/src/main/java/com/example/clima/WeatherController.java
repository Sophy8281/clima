package com.example.clima;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    // Request Codes:
    final int REQUEST_CODE = 123; // Request Code for permission request callback
    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    // Set LOCATION_PROVIDER here. Using LocationManager.GPS_Provider for Fine Location (good for emulator):
    // Recommend using LocationManager.NETWORK_PROVIDER on physical devices (reliable & fast!)
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = findViewById(R.id.locationTV);
        mWeatherImage = findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = findViewById(R.id.tempTV);
        ImageButton changeCityButton = findViewById(R.id.changeCityButton);

        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(v -> {

            Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);

            // Using startActivityForResult since we just get back the city name.
            // Providing an arbitrary request code to check against later.
            startActivity(myIntent);
        });
    }

    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();

        Log.d("Clima", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if (city != null) {
            getWeatherForNewCity(city);
        } else {
            Log.d("Clima", "Getting weather for current location.. ");
            getWeatherForCurrentLocation();
        }
    }

    // TODO: Add getWeatherForNewCity(String city) here:
    // Configuring the parameters when a new city has been entered:
    private void getWeatherForNewCity(String city) {

        Log.d("Clima", "Getting weather for new city");

        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        letsDoSomeNetworking(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    // Location Listener callbacks here, when the location has changed.
    private void getWeatherForCurrentLocation() {

        Log.d("Clima", "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                Log.d("Clima", "onLocationChanged() callback received");

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d("Clima", "longitude is: " + longitude);
                Log.d("Clima", "latitude is: " + latitude);

                // Providing 'lat' and 'lon' (spelling: Not 'long') parameter values
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Log statements to help you debug your app.
                Log.d("Clima", "onStatusChanged() callback received. Status: " + status);
                Log.d("Clima", "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Clima", "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Checking against the request code we specified earlier.
        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermissionsResult(): Permission granted!");

                // Getting weather only if we were granted permission.
                getWeatherForCurrentLocation();
            } else {
                Log.d("Clima", "Permission denied =( ");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params)  {

        // AsyncHttpClient belongs to the loopj dependency.
        AsyncHttpClient client = new AsyncHttpClient();

        // Making an HTTP GET request by providing a URL and the parameters.
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("Clima", "Success! JSON: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                Log.e("Clima", "Fail " + e.toString());
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

                Log.d("CLima", "Status code " + statusCode);
                Log.d("Clima", "Here's what we got instead " + response.toString());
            }

        });
    }

    // TODO: Add updateUI() here:
    // Updates the information shown on screen.
    private void updateUI(WeatherDataModel weather) {

        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        // Update the icon based on the resource id of the image in the drawable folder.
        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }

    // TODO: Add onPause() here:
    // Freeing up resources when the app enters the paused state.
    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }
}