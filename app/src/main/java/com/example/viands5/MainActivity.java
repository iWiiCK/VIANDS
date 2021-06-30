package com.example.viands5;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 1 - this is the started activit of the application.
 * 2 - after the splash screen, the user will be navigated to the application main menu.
 * 3 - The SQLite table will be created if it was not created before
 */
public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final int DELAY_SECONDS = 1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Create the Local SQLite database for Products, Lists and Products_toLists
        MySQLiteDB mySQLiteDB = new MySQLiteDB(this);

        //Checking whether the user is logged in with firestore;
        if(user != null)
        {
            Cursor cursor = mySQLiteDB.getUserPreferences(user.getUid());

            if(cursor != null)
            {
                if(cursor.getInt(1) == 1)
                {
                    //if the user has enabled auto backup, backup data at startup
                    FirestoreHandler firestoreHandler = new FirestoreHandler(this);
                    firestoreHandler.backUpLocalStorage();
                }
            }
        }

        //Adding the default list if it doesn't exist
        if(!mySQLiteDB.defaultListExists())
            mySQLiteDB.addList(new List(0, "Recent Products", "This is a list of all the Recent Products Scanned By You", 0));

        //Splash screen
        new Handler(Looper.getMainLooper()).postDelayed(() ->
        {
            Intent i = new Intent(getBaseContext(), MainScreenActivity.class);
            startActivity(i);
            finish();
        }, DELAY_SECONDS* 1000);
    }
}