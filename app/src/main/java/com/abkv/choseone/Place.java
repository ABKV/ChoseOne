package com.abkv.choseone;

import android.location.Location;

public class Place
{
    private String mName = "";
    private String mAddress = "";
    private String mRating = "";
    private Location mLocation = null;
    private String mId = "";

    private Place()
    {
    }

    public static Place createPlace(String name, String address, String rating, Location location)
    {
        Place result = new Place();

        result.mName = name;
        result.mAddress = address;
        result.mRating = rating;
        result.mLocation = location;

        return result;
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

    public Location getLocation()
    {
        return mLocation;
    }

}
