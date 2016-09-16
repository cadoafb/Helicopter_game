package com.example.android.game;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class SavingKeyVal extends Activity {
    private int Level = 0;
    private int HighScore = 0;
    private int MyScore = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.algs);

        // make up some new values
        Level = 2;
        MyScore = 2015;
        HighScore = 9378;

        // save the result
        saveData();
    }

    private void saveData() {
        SharedPreferences sp =
                getSharedPreferences("MyPrefs",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("level", Level);
        editor.putInt("myscore", MyScore);
        editor.putInt("highscore", HighScore);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences sp =
                getSharedPreferences("MyPrefs",
                        Context.MODE_PRIVATE);
        Level = sp.getInt("level", Level);
        MyScore = sp.getInt("myscore", MyScore);
        HighScore = sp.getInt("highscore", HighScore);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}