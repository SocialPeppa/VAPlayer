package com.example.panda.mobileplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheUtils {

    public static void putString(Context context,String key,String values){
        SharedPreferences sharedPreferences=context.getSharedPreferences("panda",Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key,values).commit();
    }

    public static String getString(Context context,String key){
        SharedPreferences sharedPreferences=context.getSharedPreferences("panda",Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    public static void putPlayMode(Context context,String key,int values){
        SharedPreferences sharedPreferences=context.getSharedPreferences("panda",Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key,values).commit();
    }

    public static int getPlayMode(Context context,String key){
        SharedPreferences sharedPreferences=context.getSharedPreferences("panda",Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key,0);
    }


}
