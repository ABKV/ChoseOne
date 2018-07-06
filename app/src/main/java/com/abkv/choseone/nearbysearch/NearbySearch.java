package com.abkv.choseone.nearbysearch;

import android.content.Context;
import android.util.Log;

import com.abkv.choseone.Place;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NearbySearch
{
    // The key for using google places API.
    private static final String KEY_PLACES_API = "AIzaSyDSqgtLhjCmqI-KpeDNLn0lMYzk8qrnFWg";

    // The url for nearby searching.
    private static final String URL_NEARBY_SEARCH = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=%s&radius=%s&location=%s&types=restaurant&keyword=%s";

    //
    private Context mContext = null;

    //
    private String mRadius = "10000";

    //
    private String mKeyword = "";

    //
    private String mLocation = "";

    public interface OnPlaceFoundListener
    {
        void onPlaceFound(Place place);
    }

    public class NearbySearchBuilder
    {
        public NearbySearchBuilder(Context context)
        {
            mContext = context;
        }

        public NearbySearchBuilder setRadius(String radius)
        {
            mRadius = radius;

            return this;
        }

        public NearbySearchBuilder setKeyword(String keyword)
        {
            mKeyword = keyword;

            return this;
        }

        public NearbySearchBuilder setLocation(String location)
        {
            mLocation = location;

            return this;
        }

        public void execute(OnPlaceFoundListener listener)
        {
            String url = String.format(URL_NEARBY_SEARCH, KEY_PLACES_API, mRadius, mLocation, mKeyword);
            List<String> result = new ArrayList<>();

            try
            {
                boolean hasNextpage = false;
                String nextPage = "";

                do
                {
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                    InputStream inputStream = connection.getInputStream();

                    hasNextpage = false;
                    
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

                            Log.i("NearbySearch", "next page: " + nextPage);
                            Log.i("NearbySearch", url);
                        }

                        for (int i = 0; i < resultArray.length(); i++)
                        {
                            JSONObject resultObject = resultArray.getJSONObject(i);
                            com.abkv.choseone.Place place = com.abkv.choseone.Place.createPlace(resultObject);

                            String distanceUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=place_id:%s&key=%s";
                            HttpsURLConnection distanceRequest = (HttpsURLConnection) new URL(String.format(distanceUrl, mLocation, place.getPlaceId(), KEY_PLACES_API)).openConnection();

                            JSONObject distanceJson = new JSONObject(convertInputStreamToString(distanceRequest.getInputStream()));
                            JSONObject distance = distanceJson.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance");

                            result.add(place.toString() + "\n" + "距離: " + distance.getString("text"));
                            listener.onPlaceFound(place);
                        }
                    }
                    else
                    {
                        Log.i("NearbySearch", "Response code: " + connection.getResponseCode());
                    }
                } while (hasNextpage);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
    }

    public NearbySearchBuilder builder(Context context)
    {
        return new NearbySearchBuilder(context);
    }
}
