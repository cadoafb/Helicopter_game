package com.example.android.game;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

public class Save extends Activity
{
    public static final String MyPREFERENCES = "MyPrefs";
    private int score = 0;
    private int coins = 0;

    public Save(int _score, int _coins)
    {
        this.score = _score;
        this.coins = _coins;

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.high_score), score);
        editor.commit();

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        this.score = sharedPref.getInt(getString(R.string.high_score), 4);

    }

    public void load()
    {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        //int defaultValue = getResources().getInteger(R.string.saved_high_score_default);
        score = sharedPref.getInt(getString(R.string.high_score), 4);
    }

    public int getBest()
    {
        return this.score;
    }

    public int getCoins()
    {
        return this.coins;
    }


}