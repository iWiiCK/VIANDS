package com.example.viands5;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class FirestoreHandler
{
    private static final String TAG = "firestore_error";
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private  final String ROOT_COLLECTION = "users";
    private final String PRODUCTS_COLLECTION = "products";
    private final String LISTS_COLLECTION = "lists";
    private final String PRODUCTS_TO_LISTS_COLLECTION = "products_to_lists";
    private  String userID;
    private String lastBackup = "You Haven't Created Any Backups";

    private Context context;
    private MySQLiteDB mySQLiteDB;


    public FirestoreHandler(Context context)
    {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        userID = user.getUid();

        this.context = context;
        mySQLiteDB = new MySQLiteDB(this.context);

        DocumentReference docRef = firestore.collection(ROOT_COLLECTION).document(userID);
        docRef.get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists())
                    lastBackup = String.valueOf(document.getData().get("last_scanned"));
                else
                    lastBackup = "You Have Not Created Backups !";
            }
            else
                Log.d(TAG, "get failed with ", task.getException());
        });
    }

    public String getLastBackup()
    {
        return lastBackup;
    }


    public void restoreBackup()
    {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Restoring Backup");
        progressDialog.setMessage("We are Restoring your Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.restore_button_logo);
        progressDialog.show();

        mySQLiteDB.factoryResetDatabase();

        firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            mySQLiteDB.addProduct(new Product(
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

        firestore.collection(ROOT_COLLECTION).document(userID).collection(LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            mySQLiteDB.addList(new List(
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

        firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            mySQLiteDB.addProductsToLists(
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

    public void backUpLocalStorage()
    {
        String backupTime = String.valueOf(Calendar.getInstance().getTime());

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Creating Backup");
        progressDialog.setMessage("Your Backup will be Created Shortly");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.backup_button_logo);
        progressDialog.show();

        Map<String, Object> data = new HashMap<>();
        data.put("user_name", user.getDisplayName());
        data.put("last_scanned", backupTime);

        firestore.collection(ROOT_COLLECTION).document(user.getUid()).set(data);

        //Clearing the Products
        /////////////////////////
        firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            Log.d(TAG, document.getId() + " => " + document.getData().get("code"));

                            firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }

                        backupProducts();
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });

        //Clearing the Lists
        /////////////////////////
        firestore.collection(ROOT_COLLECTION).document(userID).collection(LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            Log.d(TAG, document.getId() + " => " + document.getData().get("code"));

                            firestore.collection(ROOT_COLLECTION).document(userID).collection(LISTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }

                        backupLists();
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());
                });

        //Clearing products_to_lists lists
        /////////////////////////
        firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                .get()
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                        {
                            firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                                    .document(document.getId()).delete();
                        }


                        backupProductsToLists();
                    }
                    else
                        Log.w(TAG, "Error getting documents.", task.getException());

                    progressDialog.dismiss();
                });
    }


    private void backupProducts()
    {
        Cursor cursor;
        Map<String, Object> products = new HashMap<>();

        cursor = mySQLiteDB.readProductsTableData();
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

                firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_COLLECTION)
                        .document(cursor.getString(0)).set(products);
            }
        }

    }

    private void backupLists()
    {
        Cursor cursor;
        Map<String, Object> lists = new HashMap<>();

        cursor = mySQLiteDB.readListsTableData();
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                lists.put("id", String.valueOf(cursor.getInt(0)));
                lists.put("list_name", cursor.getString(1));
                lists.put("list_description", cursor.getString(2));
                lists.put("list_colour", String.valueOf(cursor.getInt(3)));

                firestore.collection(ROOT_COLLECTION).document(userID).collection(LISTS_COLLECTION)
                        .document(cursor.getString(0)).set(lists);
            }
        }
    }

    private void backupProductsToLists()
    {
        Cursor cursor;
        Map<String, Object> productsToLists = new HashMap<>();

        cursor = mySQLiteDB.readProductsToListsTableData();
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                productsToLists.put("product_code", cursor.getString(0));
                productsToLists.put("list_id", cursor.getString(1));

                firestore.collection(ROOT_COLLECTION).document(userID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                        .document(cursor.getString(0) +"_"+ cursor.getInt(1)).set(productsToLists);
            }
        }
    }
}
