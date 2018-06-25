package com.abkv.choseone;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    private String TAG = "ABKV";
    private GoogleApiClient mClient = null;
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
            case R.id.navigation_home:
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Looper.prepare();
                        final StringBuilder result = new StringBuilder();

                        final AtomicReference<Double> longitude = new AtomicReference<>();
                        final AtomicReference<Double> latitude = new AtomicReference<>();

                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        // Define a listener that responds to location updates
                        LocationListener locationListener = new LocationListener()
                        {
                            public void onLocationChanged(Location location)
                            {
                                Log.i(TAG, "onLocationChanged");
                                // Called when a new location is found by the network location provider.
                                longitude.set(location.getLongitude());
                                latitude.set(location.getLatitude());

                                Log.i(TAG, "Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());

                                String key = "AIzaSyDSqgtLhjCmqI-KpeDNLn0lMYzk8qrnFWg";
                                String place = latitude.get() + "," + longitude.get();
                                String radius = "10000";
                                String keyword = "拉麵";

                                //設定API
                                String listApi = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=%s&radius=%s&location=%s&types=restaurant&keyword=%s";
                                Log.i(TAG, result.append(get(String.format(listApi, key, radius, place, keyword))).toString());

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        mTextMessage.setText(result.toString());
                                    }
                                });
                            }

                            public void onStatusChanged(String provider, int status, Bundle extras)
                            {
                                Log.i(TAG, "onStatusChanged");
                            }

                            public void onProviderEnabled(String provider)
                            {
                                Log.i(TAG, "onProviderEnabled");
                            }

                            public void onProviderDisabled(String provider)
                            {
                                Log.i(TAG, "onProviderDisabled");
                            }
                        };

                        try
                        {
                            Log.i(TAG, "" + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

                            // Register the listener with the Location Manager to receive location updates
//                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            Log.i(TAG, "Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());

                            String key = "AIzaSyDSqgtLhjCmqI-KpeDNLn0lMYzk8qrnFWg";
                            String place = location.getLatitude() + "," + location.getLongitude();
                            String radius = "10000";
                            String keyword = "拉麵";

                            //設定API
                            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=%s&radius=%s&location=%s&types=restaurant&keyword=%s";

                            result.append(get(String.format(url, key, radius, place, keyword)));
                            Log.i(TAG, result.toString());

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    mTextMessage.setText(result.toString());
                                }
                            });
                        }
                        catch (SecurityException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                return true;
            case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);

                final StringBuilder builder = new StringBuilder();

                try
                {
                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                            .getCurrentPlace(mClient, new PlaceFilter());
                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>()
                    {
                        @Override
                        public void onResult(PlaceLikelihoodBuffer likelyPlaces)
                        {
                            for (PlaceLikelihood placeLikelihood : likelyPlaces)
                            {
                                Place place = placeLikelihood.getPlace();
                                String content = "Place: " + place.getName()
                                        + " Address: " + place.getAddress()
                                        + " has likelihood: " + placeLikelihood.getLikelihood();

                                builder.append(content).append("\n");

                                Log.i(TAG, content);
                            }
                            likelyPlaces.release();

                            mTextMessage.setText("ABKV" + builder.toString());
                        }
                    });

                    Log.i(TAG, result.toString());
                }
                catch (SecurityException e)
                {
                    e.printStackTrace();
                }

                return true;
            case R.id.navigation_notifications:
                ExecutorService executorSearching = Executors.newSingleThreadExecutor();
                final StringBuilder result = new StringBuilder();

                executorSearching.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        result.append(searchPlaces("咖哩"));

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mTextMessage.setText(result.toString());
                            }
                        });
                    }
                });
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private String get(String url)
    {
        InputStream inputStream = null;
        String result = "";

        try
        {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

            inputStream = connection.getInputStream();

            Log.i(TAG, "Response code: " + connection.getResponseCode());

            // convert inputstream to string
            if (inputStream != null)
            {
                JSONObject jObject = new JSONObject(convertInputStreamToString(inputStream));

                for(int i = 0;i<jObject.getJSONArray("results").length();i++)
                {
                    result += jObject.getJSONArray("results").getJSONObject(i).getString("name") + "\n";
                    Log.i("results name", jObject.getJSONArray("results").getJSONObject(i).getString("name"));
                }
            }
            else
            {
                result = "Did not work!";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private  String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while((line = bufferedReader.readLine()) != null)
        {
            result += line;
        }

        inputStream.close();

        return result;
    }

    private String searchPlaces (String constraint)
    {
        String result = null;

        if (mClient.isConnected())
        {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);

            AutocompleteFilter filter = new AutocompleteFilter.Builder().setTypeFilter(Place.TYPE_FOOD).build();

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi.getAutocompletePredictions(mClient, constraint, null, filter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess())
            {
                Toast.makeText(this, "Error contacting API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());

                result = "Error getting autocomplete prediction API call: " + status.toString();

                autocompletePredictions.release();
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext())
            {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(prediction);
                result += prediction.getFullText(null) + "\n";
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();
        }

        return result;
    }

    @Override
    public void onConnectionFailed (@NonNull ConnectionResult connectionResult)
    {
        Log.i(TAG, "Connection Failed!");
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
