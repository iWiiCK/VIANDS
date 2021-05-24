package com.example.viands5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button startButton;
    MySQLiteDB mySQLiteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Start Service to Check Internet Connectivity (STILL HAVE TO IMPLEMENT THIS)
        //////////////////////////////////////////////////////////////////////////////

        //Create the Local SQLite database for Products, Lists and Products_toLists
        mySQLiteDB = new MySQLiteDB(this);

        //TODO: Make sure you clear the dummy data base with the method after done testing.
        DummyDBData dummyDBData = new DummyDBData();
        //dummyDBData.loadDummyData(mySQLiteDB);
        //dummyDBData.clearDummyData(mySQLiteDB);
        mySQLiteDB.addList(new List(0, "Recent Products", "This is a list of all the Recent Products Scanned By You", 0));

        //TODO: Automate this task later on so it'll automatically open the main screen after a few seconds.
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> loadMainScreen(v.getContext()));

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