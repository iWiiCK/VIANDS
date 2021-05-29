package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * 1 - This activity is the main screen of the entire application.
 */
public class MainScreenActivity extends AppCompatActivity
{
    private boolean signedIn = false;
    private long back_pressed = 0;
    private ImageView listEmptyImage;
    private TextView listEmptyLabel;

    private final MySQLiteDB MY_SQLITE_DB = new MySQLiteDB(MainScreenActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        CheckNetwork checkNetwork = new CheckNetwork(this);
        listEmptyImage = findViewById(R.id.listEmptyImage);
        listEmptyLabel = findViewById(R.id.listEmptyLabel);

        //Firebase Google authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //Checking whether the user is logged in with firestore;
        if(user != null)
            signedIn = true;

        //Populating the Recycler View with recent products the user saved
        populateRecyclerView();

        //Switching to the custom Lists Layout
        ImageButton customListsButton = findViewById(R.id.customListsButton);
        customListsButton.setOnClickListener(v ->
        {
            Intent i = new Intent(MainScreenActivity.this, ManageCustomListsActivity.class);
            startActivity(i);
        });


        //Enable the "ZXING" barcode scanner when the user enters the "Scan new product" Button.
        Button scanNewProductButton = findViewById(R.id.scanNewProductButton);
        scanNewProductButton.setOnClickListener(v ->
        {
            if(checkNetwork.isOnline())
            {
                IntentIntegrator integrator = new IntentIntegrator(MainScreenActivity.this);
                integrator.setOrientationLocked(true);

                integrator.setBarcodeImageEnabled(true);
                integrator.setPrompt("Scan Food Product");
                integrator.initiateScan();
            }

            else
                Toast.makeText(this, "You Are Offline !",Toast.LENGTH_SHORT).show();
        });

        //Typing a barcode activity
        Button typeBarcodeButton = findViewById(R.id.typeBarcodeButton);
        typeBarcodeButton.setOnClickListener(v ->
        {
            if(checkNetwork.isOnline())
            {
                Intent i = new Intent(MainScreenActivity.this, TypeBarcode.class);
                startActivity(i);
            }

            else
                Toast.makeText(this, "You Are Offline !",Toast.LENGTH_SHORT).show();
        });
    }

    //Giving a prompt message when the user back presses in the main screen
    ////////////////////////////////////////////////////////
    @Override
    public void onBackPressed()
    {
        final int DELAY = 2000;

        if (back_pressed + DELAY > System.currentTimeMillis())
            super.onBackPressed();

        else
            Toast.makeText(getBaseContext(), "Back Press Once More To Exit", Toast.LENGTH_SHORT).show();

        back_pressed = System.currentTimeMillis();
    }

    //Populating the Recycler View
    /////////////////////////////////
    private void populateRecyclerView()
    {
        ProductsInListHandler productInList = new ProductsInListHandler(0);//0 is the list if of Recent Products Lists.
        productInList.loadList(MY_SQLITE_DB);

        //Populating the Recycler View
        ///////////////////////////////////////////////////////////////////////////////////////////
        CustomLinearAdapter customLinearAdapter = new CustomLinearAdapter(MainScreenActivity.this, this, false, 0,
                productInList.getBarcodeInList(),
                productInList.getNAME(),
                productInList.getGRADE(),
                productInList.getNOVA_GROUP(),
                productInList.getINGREDIENTS(),
                productInList.getNUTRIENTS(),
                productInList.getPRODUCT_IMAGE());


        RecyclerView recyclerView = findViewById(R.id.mainScreenRecyclerView);
        recyclerView.setAdapter(customLinearAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainScreenActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if(productInList.getBarcodeInList().size() == 0)
        {
            listEmptyLabel.setVisibility(View.VISIBLE);
            listEmptyImage.setVisibility(View.VISIBLE);
        }

        else
        {
            listEmptyLabel.setVisibility(View.GONE);
            listEmptyImage.setVisibility(View.GONE);
        }
    }

    //Updating the custom lists when restarting the activity
    //////////////////////////////////////////////////////////
    @Override
    protected void onRestart()
    {
        super.onRestart();
        recreate();
    }

    // Get the results of a barcode Scan:
    //////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(result != null)
        {
            if(result.getContents() != null)
            {
                Intent i = new Intent(MainScreenActivity.this, SearchingTheDatabase.class);
                i.putExtra("PRODUCT_BARCODE", result.getContents());
                startActivity(i);
            }
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen_menu, menu);

        if(signedIn)
            menu.getItem(0).setTitle(R.string.my_account);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.login_button)
        {
            Intent i = new Intent(getBaseContext(), LoginScreen.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}