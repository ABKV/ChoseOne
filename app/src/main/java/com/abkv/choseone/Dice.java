package com.abkv.choseone;

import java.util.List;
import java.util.Random;

public class Dice
{
    private List<Place> mList = null;

    public Dice(List<Place> list)
    {
        mList = list;
    }

    public Place roll()
    {
        return mList.get(new Random().nextInt(mList.size()));
    }
}
