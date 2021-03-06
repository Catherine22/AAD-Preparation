package com.catherine.materialdesignapp.open_weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.catherine.materialdesignapp.activities.BaseActivity;
import com.catherine.materialdesignapp.listeners.OnRequestPermissionsListener;
import com.catherine.materialdesignapp.open_weather.models.Rain;
import com.catherine.materialdesignapp.open_weather.models.Snow;
import com.catherine.materialdesignapp.open_weather.models.WeatherResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class WeatherPageActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnCircleClickListener, LocationListener {
    public final static String TAG = WeatherPageActivity.class.getSimpleName();
    private final static String WEATHER_URL = "api.openweathermap.org";
    private final static String PATH_DATA = "data";
    private final static String PATH_VERSION = "2.5";
    private final static String PATH_FIND = "find";
    private final static String APP_ID = BuildConfig.OPEN_WEATHER_API_KEY;
    private final static long MIN_TIME = 3000; // ms
    private final static float MIN_DISTANCE = 3f; // meter

    private OkHttpClient client;
    private AlertDialog alertDialog;
    private TextView tv_info;

    private GoogleMap googleMap;
    private CircleOptions circleOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_page);
        init();
    }

    private void init() {
        String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        getPermissions(permission, new OnRequestPermissionsListener() {
            @Override
            public void onGranted() {
                initView();
                initOkHttp();
                registerLocationListener();
            }

            @Override
            public void onDenied(@Nullable List<String> deniedPermissions) {
                finish();
            }

            @Override
            public void onRetry() {
                init();
            }
        });
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // enable back arrow on the top left area
            getSupportActionBar().setTitle(TAG);
        }
        tv_info = findViewById(R.id.tv_info);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initOkHttp() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.i(TAG, message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.openweathermap));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(bis);
            bis.close();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("openweathermap.org", cert);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustManagers, new SecureRandom());
            client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustManagers[0])
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri.Builder getDefaultUriBuilder() {
        return new Uri.Builder()
                .scheme("https")
                .authority(WEATHER_URL)
                .appendPath(PATH_DATA)
                .appendPath(PATH_VERSION);
    }

    private Headers.Builder getDefaultHeadersBuilder() {
        return new Headers.Builder()
                .add("Content-Type", "application/json");
    }

    @SuppressLint("MissingPermission")
    private void registerLocationListener() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // In this case, listener will be called when MIN_TIME == 3s AND MIN_DISTANCE = 3m
        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME,
                MIN_DISTANCE,
                this

        );
    }

    private void getForecast(final LatLng latLng) {
        Uri uri = getDefaultUriBuilder()
                .appendPath(PATH_FIND)
                .appendQueryParameter("lat", latLng.latitude + "")
                .appendQueryParameter("lon", latLng.longitude + "")
                .appendQueryParameter("cnt", "10")
                .appendQueryParameter("appid", APP_ID)
                .build();
        Headers headers = getDefaultHeadersBuilder().build();
        Request request = new Request.Builder()
                .headers(headers)
                .url(uri.toString())
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                popUpWarningDialog(e.getMessage(), (dialog, which) -> updateMapAndForecast(googleMap, getLocation()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.code() != 200 || response.body() == null) {
                    String sb = "code: " + response.code();
                    popUpWarningDialog(sb, (dialog, which) -> updateMapAndForecast(googleMap, getLocation()));
                    return;
                }

                final String body = response.body().string();
                Gson gson = new Gson();
                WeatherResult result = gson.fromJson(body, WeatherResult.class);
                // convert 1H/3H to oneHour/threeHours
                try {
                    JSONArray ja = new JSONObject(body).getJSONArray("list");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        JSONObject rain = jo.optJSONObject("rain");
                        JSONObject snow = jo.optJSONObject("snow");
                        if (rain != null)
                            result.getList()[i].setRain(gson.fromJson(rain.toString(), Rain.class));
                        if (snow != null)
                            result.getList()[i].setSnow(gson.fromJson(snow.toString(), Snow.class));
                    }
                    Log.d(TAG, result.toString());
                    runOnUiThread(() -> tv_info.setText(body));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void popUpWarningDialog(String message, DialogInterface.OnClickListener onRetry) {
        runOnUiThread(() -> {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            alertDialog = new AlertDialog.Builder(WeatherPageActivity.this)
                    .setTitle(R.string.warnings)
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setTitle(message)
                    .setNegativeButton(R.string.retry, onRetry)
                    .create();
            alertDialog.show();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        circleOptions = new CircleOptions();
        circleOptions.clickable(true);
        googleMap.setOnCircleClickListener(this);
        Location location = getLocation();
        updateMapAndForecast(googleMap, location);
    }


    private void updateMapAndForecast(GoogleMap googleMap, Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Toast.makeText(this, latLngToName(latLng), Toast.LENGTH_LONG).show();

        circleOptions.center(latLng);
        googleMap.addCircle(circleOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        getForecast(latLng);
    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public String latLngToName(LatLng latLng) {
        String unknown = "unknown";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unknown;
    }

    @Override
    public void onCircleClick(Circle circle) {
        Toast.makeText(this, latLngToName(circle.getCenter()), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged");
        updateMapAndForecast(googleMap, location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // User enabled GPS
    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled");
        updateMapAndForecast(googleMap, getLocation());
    }

    // User disabled GPS
    @Override
    public void onProviderDisabled(String provider) {
        popUpWarningDialog("GPS is not available", (dialog, which) -> updateMapAndForecast(googleMap, getLocation()));
    }
}
