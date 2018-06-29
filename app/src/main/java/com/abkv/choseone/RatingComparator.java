package com.abkv.choseone;

import java.util.Comparator;

public class RatingComparator implements Comparator<Place>
{
    @Override
    public int compare(Place o, Place t1)
    {
        return Double.compare(Double.parseDouble(o.getRating()), Double.parseDouble(t1.getRating()));
    }
}
