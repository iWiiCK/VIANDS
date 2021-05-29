package com.example.viands5;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 1 - This class is created with the purpose of handling the firestore database.
 * 2 - The firestore database is implemented as a backup system in this application.
 * 3 - Once a successful google authentication is done, a user will be created with the Unique ID.
 * 4 - This unique ID will be the document name and each document like this will consist of data such as
 *      names, last backup data(timestamp) and 3 more collections for products, lists and lists_to_products.
 */
public class FirestoreHandler
{
    private static final String TAG = "firestore_error";
    private final FirebaseFirestore FIRESTORE;
    private final FirebaseUser USER;

    private  final String ROOT_COLLECTION = "users";
    private final String PRODUCTS_COLLECTION = "products";
    private final String LISTS_COLLECTION = "lists";
    private final String PRODUCTS_TO_LISTS_COLLECTION = "products_to_lists";
    private final String USER_ID;

    private final Context CONTEXT;
    private final MySQLiteDB MY_SQLITE_DB;

    public FirestoreHandler(Context context)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        USER = auth.getCurrentUser();
        FIRESTORE = FirebaseFirestore.getInstance();
        USER_ID = USER.getUid();

        this.CONTEXT = context;
        MY_SQLITE_DB = new MySQLiteDB(this.CONTEXT);

    }

    public String getUSER_ID() {
        return USER_ID;
    }

    //This method wll handle the restoring of a previously saved backup
    ///////////////////////////////////////////////////////////////////////
    public void restoreBackup()
    {
        ProgressDialog progressDialog = new ProgressDialog(CONTEXT);
        progressDialog.setTitle("Restoring Backup");
        progressDialog.setMessage("We are Restoring your Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.restore_button_logo);
        progressDialog.show();

        MY_SQLITE_DB.factoryResetDatabase();

        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            MY_SQLITE_DB.addProduct(new Product(
                                    document.getString("code"),
                                    document.getString("name"),
                                    document.getString("grade"),
                                    document.getString("nova_grade"),
                                    document.getString("ingredients"),
                                    document.getString("nutrients"),
                                    null
                            ));
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });

        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            MY_SQLITE_DB.addList(new List(
                                    document.getString("id"),
                                    document.getString("list_name"),
                                    document.getString("list_description"),
                                    document.getString("list_colour")
                            ));
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });

        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            MY_SQLITE_DB.addProductsToLists(
                                    document.getString("product_code"),
                                    document.getString("list_id")
                            );
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());

                    progressDialog.dismiss();
                });
    }

    //This method will handle the Backup functionality of the application.
    //This included manual and automatic backup systems
    public void backUpLocalStorage()
    {
        ProgressDialog progressDialog = new ProgressDialog(CONTEXT);
        progressDialog.setTitle("Creating Backup");
        progressDialog.setMessage("Your Backup will be Created Shortly");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.backup_button_logo);

        Cursor cursor = MY_SQLITE_DB.getUserPreferences(USER_ID);
        if(cursor != null && cursor.getInt(1) == 0)
        {
            progressDialog.show();
        }

        backupProducts();
        backupLists();
        backupProductsToLists();

        progressDialog.dismiss();
    }

    private void setLastBackupData()
    {
        String backupTime = String.valueOf(Calendar.getInstance().getTime());
        Map<String, Object> data = new HashMap<>();
        String LAST_BACKUP = "last_scanned";
        data.put(LAST_BACKUP, backupTime);
        String USER_NAME = "user_name";
        data.put(USER_NAME, USER.getDisplayName());

        FIRESTORE.collection(ROOT_COLLECTION).document(USER.getUid()).set(data);
    }


    //Backing up products to the SQLite Database
    public void backupProducts()
    {
        Cursor cursor = MY_SQLITE_DB.readProductsTableData();
        Map<String, Object> products = new HashMap<>();

        setLastBackupData();

        //Clearing the Products
        /////////////////////////
        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            Log.d(TAG, document.getId() + " => " + document.getData().get("code"));

                            FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }

                        if(cursor != null)
                        {
                            while (cursor.moveToNext())
                            {
                                products.put("code", cursor.getString(0));
                                products.put("name", cursor.getString(1));
                                products.put("grade", cursor.getString(2));
                                products.put("nova_grade", String.valueOf(cursor.getInt(3)));
                                products.put("ingredients", cursor.getString(4));
                                products.put("nutrients", cursor.getString(5));
                                //TODO: DID not backup the Image yet
                                //products.put("product_image", cursor.getString(6));

                                FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_COLLECTION)
                                        .document(cursor.getString(0)).set(products);
                            }
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });

    }

    //Backup lists to the Firestore database
    public void backupLists()
    {
        Cursor cursor = MY_SQLITE_DB.readListsTableData();
        Map<String, Object> lists = new HashMap<>();

        setLastBackupData();

        //Clearing the Lists
        /////////////////////////
        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            Log.d(TAG, document.getId() + " => " + document.getData().get("code"));

                            FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(LISTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }

                        if(cursor != null)
                        {
                            while (cursor.moveToNext())
                            {
                                lists.put("id", String.valueOf(cursor.getInt(0)));
                                lists.put("list_name", cursor.getString(1));
                                lists.put("list_description", cursor.getString(2));
                                lists.put("list_colour", String.valueOf(cursor.getInt(3)));

                                FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(LISTS_COLLECTION)
                                        .document(cursor.getString(0)).set(lists);
                            }
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });
    }

    //Backup products_to_lists to the firestore database
    public void backupProductsToLists()
    {
        Cursor cursor = MY_SQLITE_DB.readProductsToListsTableData();
        Map<String, Object> productsToLists = new HashMap<>();

        setLastBackupData();

        //Clearing products_to_lists lists
        /////////////////////////
        FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }


                        if(cursor != null)
                        {
                            while (cursor.moveToNext())
                            {
                                productsToLists.put("product_code", cursor.getString(0));
                                productsToLists.put("list_id", cursor.getString(1));

                                FIRESTORE.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                                        .document(cursor.getString(0) +"_"+ cursor.getInt(1)).set(productsToLists);
                            }
                        }
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });
    }
}
