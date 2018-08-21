package com.abkv.choseone;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abkv.choseone.data.DbHelper;
import com.abkv.choseone.data.GoogleDriveHandler;
import com.abkv.choseone.nearbysearch.NearbySearch;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
{
    // The debug tag.
    private String TAG = "ABKV";

    // The key for using google places API.
    private static final String KEY_PLACES_API = "AIzaSyDSqgtLhjCmqI-KpeDNLn0lMYzk8qrnFWg";

    // The url for nearby searching.
    private static final String URL_NEARBY_SEARCH = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=%s&radius=%s&location=%s&types=restaurant&keyword=%s";

    // The client which is used to gather the location of device.
    private GoogleApiClient mClient = null;

    // The main text view on the screen.
    private TextView mTextMessage;

    // The list view which shows the result from nearby searching.
    private ListView mListView = null;

    // The list view which shows selected items.
    private ListView mSelectedListView = null;

    // The component which is used to input the keyword of nearby searching.
    private EditText mEditText = null;

    // The options of food type.
    private Spinner mSpinner = null;

    // The pager for different pages.
    private ViewPager mPager = null;

    // The selected items.
    private List<com.abkv.choseone.Place> mSelectedList = new LinkedList<>();

    // The query result.
    private List<com.abkv.choseone.Place> mResultList = new ArrayList<>();

    // The adapter for the list view to show the selected results.
    private ArrayAdapter<String> mSelectedArrayAdapter = null;

    // The adapter for the list view to show the searching results.
    private ArrayAdapter<String> mArrayAdapter = null;

    private OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
            case R.id.navigation_home:

                mArrayAdapter.clear();
                mSelectedArrayAdapter.clear();
                mSelectedList.clear();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String selectedItem = mSpinner.getSelectedItem().toString();
                        String keyword = mEditText.getText().toString();

                        if (keyword.equalsIgnoreCase(""))
                        {
                            keyword = selectedItem;
                        }

                        Looper.prepare();
                        try
                        {
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                            final List<String> results = new ArrayList<>();

                            // Log.i(this, "Longitude: " + location.getLongitude() + " Latitude: " + location.getLatitude());

                            String place = "24.826278,121.010912";//location.getLatitude() + "," + location.getLongitude();
                            String radius = "10000";

                            mResultList.clear();

                            new NearbySearch().builder(getBaseContext()).setRadius(radius).setKeyword(keyword).setLocation(place).execute(new NearbySearch.OnPlaceFoundListener()
                            {
                                @Override
                                public void onPlaceFound(final com.abkv.choseone.Place place)
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            mResultList.add(place);
                                            mArrayAdapter.add(place.toString());
                                            mArrayAdapter.notifyDataSetInvalidated();
                                        }
                                    });
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

                mPager.setCurrentItem(1);
                return true;
            case R.id.navigation_notifications:
                Toast.makeText(getBaseContext(), new Dice(mSelectedList).roll().getName(), Toast.LENGTH_LONG).show();

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

        final List<View> PAGES = new ArrayList<>();

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mEditText = findViewById(R.id.editText);
        mSpinner = findViewById(R.id.spinner);
        mPager = findViewById(R.id.viewPager);

        mSelectedListView = new ListView(this);

        mSelectedArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        mSelectedListView.setAdapter(mSelectedArrayAdapter);
        mSelectedListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                com.abkv.choseone.Place target = mResultList.get(i);

                mSelectedList.remove(target);
                mSelectedArrayAdapter.remove(target.toString());
                mSelectedArrayAdapter.notifyDataSetInvalidated();
            }
        });

        mListView = new ListView(this);
        mListView.setAdapter(mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>()));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                com.abkv.choseone.Place target = mResultList.get(i);

                if (!mSelectedList.contains(target))
                {
                    mSelectedList.add(target);
                    mSelectedArrayAdapter.add(target.toString());
                    mSelectedArrayAdapter.notifyDataSetInvalidated();
                }

                for (com.abkv.choseone.Place place : mSelectedList)
                {
                    Logger.i(this, place.getName());
                }
            }
        });

        mSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.food_types, android.R.layout.simple_list_item_1));

        PAGES.add(mListView);
        PAGES.add(mSelectedListView);

        mPager.setAdapter(new PagerAdapter()
        {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position)
            {
                View view = PAGES.get(position);

                container.addView(view);

                return view;
            }

            @Override
            public int getCount()
            {
                return PAGES.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
            {
                return view == object;
            }
        });
        mPager.setCurrentItem(0);

        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        DbHelper.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Logger.i(this, "request code: ", requestCode);

        switch (requestCode)
        {
        case 0:
            if (resultCode != RESULT_OK)
            {
                // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                // required and is fatal. For apps where sign-in is optional, handle
                // appropriately
                Logger.e(this, "Sign-in failed.");
                finish();
                return;
            }

            Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (getAccountTask.isSuccessful())
            {
                GoogleDriveHandler.getInstance(this, data);
            }
            else
            {
                Logger.e(this, "Sign-in failed.");
                finish();
            }
            break;
        case 1:
//            if (resultCode == RESULT_OK)
//            {
//                DriveId driveId = data.getParcelableExtra(
//                        OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
//                mOpenItemTaskSource.setResult(driveId);
//            } else
//            {
//                mOpenItemTaskSource.setException(new RuntimeException("Unable to open file"));
//            }
            break;
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (result != ConnectionResult.SUCCESS)
        {
            GoogleApiAvailability.getInstance().getErrorDialog(this, 0, 0).show();
        }
        else
        {
            Logger.i(this, result);

            GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER).build();
            GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

            startActivityForResult(client.getSignInIntent(), 0);
        }
    }

    private List<String> nearbySearch(String keyword, String radius, String location)
    {
        String url = String.format(URL_NEARBY_SEARCH, KEY_PLACES_API, radius, location, keyword);
        InputStream inputStream = null;
        List<String> result = new ArrayList<>();

        mResultList.clear();

        try
        {
            boolean hasNextpage = false;
            String nextPage = "";

            do
            {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

                hasNextpage = false;
                inputStream = connection.getInputStream();

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                {
                    JSONObject jObject = new JSONObject(convertInputStreamToString(inputStream));
                    JSONArray resultArray = jObject.getJSONArray("results");

                    nextPage = jObject.optString("next_page_token");

                    if (!nextPage.isEmpty())
                    {
                        hasNextpage = true;

                        url = url.split("&pagetoken")[0];
                        url = url + "&pagetoken=" + nextPage;

                        Logger.i(this, "next page: " + nextPage);
                        Logger.i(this, url);
                    }

                    for (int i = 0; i < resultArray.length(); i++)
                    {
                        JSONObject resultObject = resultArray.getJSONObject(i);
                        com.abkv.choseone.Place place = com.abkv.choseone.Place.createPlace(resultObject);

                        String distanceUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=place_id:%s&key=%s";
                        HttpsURLConnection distanceRequest = (HttpsURLConnection) new URL(String.format(distanceUrl, location, place.getPlaceId(), KEY_PLACES_API)).openConnection();

                        JSONObject distanceJson = new JSONObject(convertInputStreamToString(distanceRequest.getInputStream()));
                        JSONObject distance = distanceJson.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance");

                        mResultList.add(place);
                        result.add(place.toString() + "\n" + "距離: " + distance.getString("text"));

                        Logger.i(this, place.toString());
                    }
                }
                else
                {
                    Logger.i(this, "Response code: " + connection.getResponseCode());
                    Toast.makeText(this, "The connection didn't work!", Toast.LENGTH_LONG).show();
                }
            } while (hasNextpage);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null)
        {
            result += line;
        }

        inputStream.close();

        return result;
    }

    private String searchPlaces(String constraint)
    {
        String result = null;

        if (mClient.isConnected())
        {
            Logger.i(this, "Starting autocomplete query for: " + constraint);

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
                Logger.e(this, "Error getting autocomplete prediction API call: " + status.toString());

                result = "Error getting autocomplete prediction API call: " + status.toString();

                autocompletePredictions.release();
            }

            Logger.i(this, "Query completed. Received " + autocompletePredictions.getCount()
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Logger.i(this, "Connection Failed!");
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
