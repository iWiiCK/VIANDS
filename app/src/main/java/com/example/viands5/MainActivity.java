package com.example.viands5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    MySQLiteDB mySQLiteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final int DELAY_SECONDS = 1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create the Local SQLite database for Products, Lists and Products_toLists
        mySQLiteDB = new MySQLiteDB(this);

        //TODO: Make sure you clear the dummy data base with the method after done testing.
        DummyDBData dummyDBData = new DummyDBData();
        //dummyDBData.loadDummyData(mySQLiteDB);
        //dummyDBData.clearDummyData(mySQLiteDB);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMainScreen(getBaseContext());
            }
        }, DELAY_SECONDS* 1000);

    }

    //This will load the main screen
    private void loadMainScreen(Context context)
    {
        //Creating the Default List which is the Recent Product List
        /////////////////////////////////////////////////////////////
        Intent i = new Intent(context, MainScreenActivity.class);
        startActivity(i);
        finish();
    }
}