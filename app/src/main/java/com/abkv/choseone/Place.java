package com.abkv.choseone;

import org.json.JSONException;
import org.json.JSONObject;

public class Place
{
    private String mName = "";
    private String mAddress = "";
    private String mRating = "";
    private String mLat = "";
    private String mLng = "";
    private String mId = "";

    private Place()
    {
    }

    public static Place createPlace(String name, String address, String rating, String latitude, String longitude, String id)
    {
        Place result = new Place();

        result.mName = name;
        result.mAddress = address;
        result.mRating = rating;
        result.mLat = latitude;
        result.mLng = longitude;
        result.mId = id;

        return result;
    }

    public static Place createPlace(JSONObject jObject) throws JSONException
    {
        Place result = new Place();
        JSONObject locationObject = jObject.getJSONObject("geometry").getJSONObject("location");

        result.mName = jObject.getString("name");
        result.mAddress = jObject.getString("vicinity");
        result.mRating = jObject.getString("rating");
        result.mId = jObject.getString("place_id");
        result.mLat = locationObject.getString("lat");
        result.mLng = locationObject.getString("lng");

        return result;
    }

    @Override
    public String toString()
    {
        return mName.concat("\n").concat(mAddress).concat("\n").concat("評價: ").concat(mRating).concat("/5");
    }

    public String getName()
    {
        return mName;
    }

    public String getAddress()
    {
        return mAddress;
    }

    public String getRating()
    {
        return mRating;
    }

    public String getLatitude()
    {
        return mLat;
    }

    public String getLongitude() { return mLng; }

    public String getPlaceId() { return mId; }

}
