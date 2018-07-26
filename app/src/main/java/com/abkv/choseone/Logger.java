package com.abkv.choseone;

import android.util.Log;

public class Logger
{
    private static String TAG = "ChoseOne";

    public static void i(Object clazz, Object... objects)
    {
        Log.i(TAG, formatLogs(clazz.getClass(), objects));
    }

    public static void d(Object clazz, Object... objects)
    {
        Log.d(TAG, formatLogs(clazz.getClass(), objects));
    }

    public static void v(Object clazz, Object... objects)
    {
        Log.v(TAG, formatLogs(clazz.getClass(), objects));
    }

    public static void w(Object clazz, Object... objects)
    {
        Log.w(TAG, formatLogs(clazz.getClass(), objects));
    }

    public static void e(Object clazz, Object... objects)
    {
        Log.e(TAG, formatLogs(clazz.getClass(), objects));
    }

    private static String formatLogs(Class<?> clazz, Object... objects)
    {
        StringBuilder builder = new StringBuilder();

        for (StackTraceElement[] elements : Thread.getAllStackTraces().values())
        {
            for (StackTraceElement element : elements)
            {
                if (clazz.getName().equals(element.getClassName()))
                {
                    builder.append("[").append(element.getFileName()).append(" - ").append(element.getLineNumber()).append("] ");

                    for (Object obj : objects)
                    {
                        builder.append(obj);
                    }

                    break;
                }
            }
        }

        return builder.toString();
    }
}
