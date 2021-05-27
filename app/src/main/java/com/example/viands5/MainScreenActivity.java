package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainScreenActivity extends AppCompatActivity
{

    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean signedIn = false;


    private long back_pressed = 0;
    private ImageButton customListsButton;
    private Button scanNewProductButton;
    private Button typeBarcodeButton;

    private MySQLiteDB mySQLiteDB = new MySQLiteDB(MainScreenActivity.this);
    private CustomLinearAdapter customLinearAdapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Firebase Google authentication
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user != null)
            signedIn = true;


        //Populating the Recycler View
        /////////////////////////////////
        populateRecyclerView();
        CheckNetwork checkNetwork = new CheckNetwork(this);


        //Switching to the custom Lists Layout
        ///////////////////////////////////////
        customListsButton = findViewById(R.id.customListsButton);
        customListsButton.setOnClickListener(v ->
        {
            Intent i = new Intent(MainScreenActivity.this, ManageCustomListsActivity.class);
            startActivity(i);
        });


        //Enable the "xzing" barcode scanner when the user enters the "Scan new product" Button.
        ////////////////////////////////////////////////////////////////////////////////////////
        scanNewProductButton = findViewById(R.id.scanNewProductButton);
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

        typeBarcodeButton = findViewById(R.id.typeBarcodeButton);
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
        {
            super.onBackPressed();
        }

        else
        {
            Toast.makeText(getBaseContext(),"Back Press Once More To Exit", Toast.LENGTH_SHORT).show();
        }

        back_pressed = System.currentTimeMillis();
    }

    //Populating the Recycler View
    /////////////////////////////////
    private void populateRecyclerView()
    {
        ProductsInListHandler productInList = new ProductsInListHandler(0);//0 is the list if of Recent Products Lists.
        productInList.loadList(mySQLiteDB);

        //Populating the Recycler View
        ///////////////////////////////////////////////////////////////////////////////////////////
        customLinearAdapter = new CustomLinearAdapter(MainScreenActivity.this , this, false,0,
                productInList.getBarcodeInList(),
                productInList.getName(),
                productInList.getGrade(),
                productInList.getNovaGroup(),
                productInList.getIngredients(),
                productInList.getNutrients(),
                productInList.getProductImage());


        recyclerView = findViewById(R.id.mainScreenRecyclerView);
        recyclerView.setAdapter(customLinearAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainScreenActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    // Get the results of a barcode Scan:
    //////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if(requestCode ==1)
            recreate();

        if(result != null)
        {
            if(result.getContents() != null)
            {
                recreate();
                Intent i = new Intent(MainScreenActivity.this, SearchingTheDatabse.class);
                i.putExtra("PRODUCT_BARCODE", result.getContents());
                startActivity(i);
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
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