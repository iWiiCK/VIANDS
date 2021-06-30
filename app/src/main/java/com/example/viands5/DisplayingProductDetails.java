package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * 1 - This class represent a detailed product data.
 * 2 - Unlike the recycler view items, here, all the data are displayed.
 * 3 - The products will be displayed either from the local SQLite data base or
 *     from fetching data from the API.
 */
public class DisplayingProductDetails extends AppCompatActivity
{
    private MySQLiteDB mySQLiteDB;
    private boolean loadedFromList = false;
    private Button saveOrChangeListButton;
    private int listId;
    private String code;

    private String name, grade, ingredients, nutrients ;
    private int novaGroup, gradeDrawable, novaGroupDrawable;
    private byte[] byteProductImage = null;
    private boolean refreshedProduct = false, complete = true;

    private ArrayList<String> listName, listDescription, numOfItemsInList;
    private ArrayList<Integer> listIds, listColour;

    private TextView productNameInDetails, ingredientsInDetails, nutrientsInDetails;
    private TextView listNamePlaintext;
    private TextView listDescriptionPlainText;
    private ImageView nutritionalValueImage, novaGroupImage, productPlaceHolderImage;

    private CardView hiddenLayout, hiddenNewListLayout, hiddenEmptyListLogo;
    private ImageButton hideButton;
    private Button cancelCreatingListButton, saveAndAddButton;
    private FloatingActionButton addNewListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaying_product_details);
        Bundle extras = getIntent().getExtras();
        mySQLiteDB = new MySQLiteDB(this);

        productNameInDetails = findViewById(R.id.productNameInDetails);
        ingredientsInDetails = findViewById(R.id.ingredientsInDetails);
        nutrientsInDetails = findViewById(R.id.nutrientsInDetails);
        nutritionalValueImage = findViewById(R.id.nutritionalValueImage);
        novaGroupImage = findViewById(R.id.novaGroupImage);
        saveOrChangeListButton = findViewById(R.id.saveOrChangeListButton);
        productPlaceHolderImage = findViewById(R.id.productPlaceHolderImage);
        TextView offlineDataMessage = findViewById(R.id.offlineDataMessage);

        hiddenLayout = findViewById(R.id.hiddenListsLayout);
        hideButton = findViewById(R.id.hideButton);
        addNewListButton = findViewById(R.id.addNewListButton);
        hiddenEmptyListLogo = findViewById(R.id.hiddenEmptyListLogo);

        hiddenNewListLayout = findViewById(R.id.hiddenNewListLayout);
        cancelCreatingListButton = findViewById(R.id.cancelCreatingListButton);
        saveAndAddButton = findViewById(R.id.saveAndAddButton);
        listNamePlaintext = findViewById(R.id.listNamePlaintext);
        listDescriptionPlainText = findViewById(R.id.listDescriptionPlainText);

        if(extras != null)
        {
            loadedFromList = extras.getBoolean("LOADED_FROM_LIST");
            listId = extras.getInt("LIST_ID");
            refreshedProduct = extras.getBoolean("PRODUCT_REFRESHED");
            complete = extras.getBoolean("COMPLETE");
        }

        if(!complete)
        {
            saveOrChangeListButton.setEnabled(false);
            saveOrChangeListButton.setBackgroundColor(Color.GRAY);
            Toast.makeText(this, "Incomplete Product !", Toast.LENGTH_SHORT).show();
        }

        else
        {
            saveOrChangeListButton.setEnabled(true);
            saveOrChangeListButton.setBackgroundColor(getResources().getColor(R.color.viands_confirm_button_skin));
        }

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v ->
        {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("REFRESH",true);
            setResult(RESULT_OK,returnIntent);
            finish();
        });

        //If displaying a refreshed product, show a toast.
        if(refreshedProduct)
        {
            Toast.makeText(this, "Refreshed Data", Toast.LENGTH_SHORT).show();
            loadedFromList = true;
        }

        //While Details are displayed from a list
        ///////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////
        if(loadedFromList)
        {
            //Displaying product from a list, but not a refreshed product
            if(!refreshedProduct)
            {
                offlineDataMessage.setVisibility(View.VISIBLE);
                assert extras != null;
                displayProductFromList(extras);

                changeButton();
                saveOrChangeListButton.setOnClickListener(v ->
                        selectListToAdd());
            }
            //Displaying product from a list, but a refreshed product. (Real time data)
            else
            {
                offlineDataMessage.setVisibility(View.GONE);
                displayProductFromTheApi(extras);
                saveOrChangeListButton.setText(R.string.update_product);
                saveOrChangeListButton.setOnClickListener(v ->
                {
                    mySQLiteDB.updateProduct(new Product(code, name, grade, novaGroup, ingredients, nutrients, byteProductImage));
                    saveOrChangeListButton.setEnabled(false);
                    saveOrChangeListButton.setBackgroundColor(getResources().getColor(R.color.dark_grey_cardview_bg));
                });
            }

        }

        //While details are displayed from a barcode scan
        ///////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////
        if(!loadedFromList)
        {
            displayProductFromTheApi(extras);

            saveOrChangeListButton.setOnClickListener(v ->
            {
                selectListToAdd();

                if(!mySQLiteDB.productAlreadySaved(code))
                {
                    mySQLiteDB.addProduct(new Product(code, name, grade, novaGroup, ingredients, nutrients, byteProductImage));
                }
            });
        }
    }

    //Select a list to add the new porduct
    //////////////////////////////////////////
    private void selectListToAdd()
    {
        hiddenLayout.setVisibility(View.VISIBLE);
        loadDataFromDB();
        CustomGridAdapter customGridAdapter = new CustomGridAdapter(DisplayingProductDetails.this,
                this,
                true,
                code,
                listIds,
                listName,
                listDescription,
                listColour,
                numOfItemsInList);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGridOfLists);
        recyclerView.setAdapter(customGridAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.scrollToPosition(customGridAdapter.getItemCount());

        //Adding a decorated divider to the recycler view items
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.recycler_view_divider)));
        recyclerView.addItemDecoration(itemDecorator);

        if(customGridAdapter.getLIST_IDS().size() < 1)
        {
            hiddenEmptyListLogo.setVisibility(View.VISIBLE);
        }

        hideButton.setOnClickListener(v->
                hiddenLayout.setVisibility(View.GONE));

        addNewListButton.setOnClickListener(v->
                createNewListAndAdd());

    }

    //Create and add new product to a new list
    //////////////////////////////////////////////
    private void createNewListAndAdd()
    {
        hiddenNewListLayout.setVisibility(View.VISIBLE);

        cancelCreatingListButton.setOnClickListener(v->
        {
            listNamePlaintext.setText("");
            listDescriptionPlainText.setText("");
            hiddenNewListLayout.setVisibility(View.GONE);

            forceHideKeyboard(this, listNamePlaintext);
        });

        saveAndAddButton.setOnClickListener(v->
        {
            if(!listNamePlaintext.getText().toString().isEmpty() && !listDescriptionPlainText.getText().toString().isEmpty())
            {
                int listId = mySQLiteDB.readNumOfLists();
                mySQLiteDB.addList(new List(listId, listNamePlaintext.getText().toString(), listDescriptionPlainText.getText().toString(), 0));
                mySQLiteDB.addProductsToLists(code, listId);

                hiddenLayout.setVisibility(View.GONE);
                hiddenNewListLayout.setVisibility(View.GONE);
                hiddenEmptyListLogo.setVisibility(View.GONE);
            }

            forceHideKeyboard(this, listNamePlaintext);
        });
    }

    //Loading the lists from the database
    ///////////////////////////////////////
    private void loadDataFromDB()
    {
        int index = 1;
        int count;
        listIds = new ArrayList<>();
        listName = new ArrayList<>();
        listDescription = new ArrayList<>();
        listColour = new ArrayList<>();
        numOfItemsInList = new ArrayList<>();

        Cursor cursor = mySQLiteDB.readListsTableData();

        if(cursor.getCount() != 0)
        {
            cursor.moveToNext();

            while (cursor.moveToNext())
            {
                listIds.add(cursor.getInt(0));
                listName.add(cursor.getString(1));
                listDescription.add(cursor.getString(2));
                listColour.add(cursor.getInt(3));
                count = mySQLiteDB.numOfProductsInList(index);
                numOfItemsInList.add(count + " Items");
                index++;
            }
        }

        //reversing the list since the grid layout start scroll position can't be manipulated when displaying on reverse
        Collections.reverse(listIds);
        Collections.reverse(listName);
        Collections.reverse(listDescription);
        Collections.reverse(listColour);
        Collections.reverse(numOfItemsInList);
    }

    //Change the button styles when displaying local and fetched data
    ///////////////////////////////////////////////////////////////////
    private void changeButton()
    {
        saveOrChangeListButton.setText(R.string.change_list_label);
        saveOrChangeListButton.setBackgroundColor(getResources().getColor(R.color.button_skin_bucky));
        saveOrChangeListButton.setTextColor(Color.WHITE);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    //Display details of the product when being displayed from the local database
    /////////////////////////////////////////////////////////////////////////////////
    private void displayProductFromList(Bundle extras)
    {
        listId = extras.getInt("LIST_ID");
        byteProductImage = extras.getByteArray("PRODUCT_IMAGE");

        code = extras.getString("PRODUCT_BARCODE");
        name = extras.getString("PRODUCT_NAME");
        gradeDrawable = extras.getInt("PRODUCT_GRADE");
        novaGroupDrawable = extras.getInt("PRODUCT_NOVA_GROUP");
        ingredients = extras.getString("PRODUCT_INGREDIENTS");
        nutrients = extras.getString("PRODUCT_NUTRIENTS");

        novaGroupImage.setImageResource(novaGroupDrawable);
        nutritionalValueImage.setImageResource(gradeDrawable);

        productNameInDetails.setText(name);
        ingredientsInDetails.setText(ingredients);
        nutrientsInDetails.setText(nutrients);

        if(byteProductImage != null)
        {
            ByteArrayInputStream imageStream = new ByteArrayInputStream(byteProductImage);
            Bitmap bitmap= BitmapFactory.decodeStream(imageStream);
            productPlaceHolderImage.setImageBitmap(bitmap);
        }
    }

    //Displaying details of a product from the api
    ///////////////////////////////////////////////////
    private void displayProductFromTheApi(Bundle extras)
    {
        try
        {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byteProductImage = stream.toByteArray();
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable)
                {


                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            //Loading the image from a URL to the imageView
            Picasso.get().load(extras.getString("PRODUCT_IMAGE_URL")).into(productPlaceHolderImage);
            Picasso.get().load(extras.getString("PRODUCT_IMAGE_URL")).into(target);
        }

        catch (Exception e)
        {
            Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        }


        code = extras.getString("API_CODE");
        name = extras.getString("API_NAME");
        grade = extras.getString("API_GRADE");
        gradeDrawable = extras.getInt("API_GRADE_DRAWABLE");
        novaGroupDrawable = extras.getInt("API_NOVA_GROUP_DRAWABLE");
        novaGroup = extras.getInt("API_NOVA_GROUP");
        ingredients = extras.getString("API_INGREDIENTS");
        nutrients = extras.getString("API_NUTRIENTS");

        novaGroupImage.setImageResource(novaGroupDrawable);
        nutritionalValueImage.setImageResource(gradeDrawable);

        productNameInDetails.setText(name);
        ingredientsInDetails.setText(ingredients);
        nutrientsInDetails.setText(nutrients);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refresh_product_data, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        CheckNetwork checkNetwork = new CheckNetwork(this);
        if(checkNetwork.isOnline())
            refreshProduct(code);
        else

            Toast.makeText(this, "You Are Offline !", Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    //Refreshing product details
    /////////////////////////////////
    private void refreshProduct(String barcode)
    {
        Intent i = new Intent(DisplayingProductDetails.this, SearchingTheDatabase.class);
        i.putExtra("PRODUCT_BARCODE", barcode);
        i.putExtra("PRODUCT_REFRESHED", true);
        startActivity(i);
        finish();
    }

    //Force Hiding the Keyboard
    ////////////////////////////////
    public static void forceHideKeyboard(@NonNull Activity activity, @NonNull TextView editText) {
        if (activity.getCurrentFocus() == null || !(activity.getCurrentFocus() instanceof EditText)) {
            editText.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}