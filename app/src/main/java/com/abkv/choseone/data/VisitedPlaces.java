package com.abkv.choseone.data;

import android.content.ContentValues;
import android.content.Context;

public class VisitedPlaces
{
    public static final String TABLE_NAME = "VisitedPlaces";
    public static final String ID = "visited_places_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String PLACE_ID = "place_id";
    public static final String COMMENT = "comment";

    private DbHelper mDbHelper = null;
    private ContentValues mContentValues = new ContentValues();

    private int mId = 0;
    private long mTimestamp = 0l;
    private String mName = "";
    private String mAddress = "";
    private String mPlaceId = "";
    private String mComment = "";

    public VisitedPlaces(Context context)
    {
        mDbHelper = DbHelper.getInstance(context);
    }


    public long insert()
    {
        long id = mDbHelper.getWritableDatabase().insert(TABLE_NAME, null, mContentValues);

        mContentValues.clear();

        return id;
    }

    public int getId()
    {
        return mId;
    }

    public long getTimestamp()
    {
        return mTimestamp;
    }

    public String getName()
    {
        return mName;
    }

    public String getAddress()
    {
        return mAddress;
    }

    public String getPlaceId()
    {
        return mPlaceId;
    }

    public String getComment()
    {
        return mComment;
    }

    public void setTimestamp(long timestamp)
    {
        mTimestamp = timestamp;
        mContentValues.put(TIMESTAMP, timestamp);
    }

    public void setName(String name)
    {
        mName = name;
        mContentValues.put(NAME, name);
    }

    public void setAddress(String address)
    {
        mAddress = address;
        mContentValues.put(ADDRESS, address);
    }

    public void setPlaceId(String placeId)
    {
        mPlaceId = placeId;
        mContentValues.put(PLACE_ID, placeId);
    }

    public void setComment(String comment)
    {
        mComment = comment;
        mContentValues.put(COMMENT, comment);
    }
}
