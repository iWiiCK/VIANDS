package com.example.viands5;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity
{

    private static final int RC_SIGN_IN = 120;
    private static final String TAG = "sign_in";
    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    private Button googleSignInButton, goBackButton, signOutButton;
    private CardView signInLayout, alreadySignedInLayout;
    private ImageView googleAccountImage;
    private TextView userName;

    private FirebaseFirestore firestore;
    private Button backupDataButton, restoreBackupButton;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        goBackButton = findViewById(R.id.goBackButton);
        signInLayout = findViewById(R.id.signInLayout);
        alreadySignedInLayout = findViewById(R.id.alreadySignedInLayout);
        googleAccountImage = findViewById(R.id.googleAccountImage);
        userName = findViewById(R.id.userName);
        signOutButton = findViewById(R.id.signOutButton);
        backupDataButton = findViewById(R.id.backupDataButton);
        restoreBackupButton = findViewById(R.id.restoreBackupButton);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();


        if(user != null)
            displayAlreadyLoggedInScreen();

        else
            displaySignInScreen();
    }

    private void displaySignInScreen()
    {
        signInLayout.setVisibility(View.VISIBLE);
        alreadySignedInLayout.setVisibility(View.GONE);

        goBackButton.setOnClickListener(v-> finish());
        googleSignInButton.setOnClickListener(v->
        {
            signIn();
        });
    }

    private void displayAlreadyLoggedInScreen()
    {
        signInLayout.setVisibility(View.GONE);
        alreadySignedInLayout.setVisibility(View.VISIBLE);

        displayUserData();

        signOutButton.setOnClickListener(v->
        {
            auth.signOut();
            displaySignInScreen();

        });
    }

    private void signIn()
    {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Exception exception = task.getException();
            if(task.isSuccessful())
            {
                try
                {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                }

                catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                }
            }

            else {
                assert exception != null;
                Log.w(TAG, exception.toString());
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = auth.getCurrentUser();
                            userId = auth.getCurrentUser().getUid();

                            assert user != null;
                            displayUserData();
                            signInLayout.setVisibility(View.GONE);
                            alreadySignedInLayout.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void displayUserData()
    {
        final String ROOT_COLLECTION = "users";
        final String USER_ID = user.getUid();

        Picasso.get().load(user.getPhotoUrl()).into(googleAccountImage);
        userName.setText(user.getDisplayName());

        backupDataButton.setOnClickListener(v->
        {
            Map<String, Object> data = new HashMap<>();
            data.put("user_name", user.getDisplayName());
            data.put("last_scanned", Calendar.getInstance().getTime());

            //Setting up root data, updating backup times
            firestore.collection(ROOT_COLLECTION).document(USER_ID).set(data);
            backUpLocalStorage(ROOT_COLLECTION, USER_ID);

        });
    }

    private void backUpLocalStorage(final String ROOT_COLLECTION, final String USER_ID)
    {
        Cursor cursor;
        final String PRODUCTS_COLLECTION = "products";
        final String LISTS_COLLECTION = "lists";
        final String PRODUCTS_TO_LISTS_COLLECTION = "products_to_lists";

        Map<String, Object> products = new HashMap<>();
        Map<String, Object> lists = new HashMap<>();
        Map<String, Object> productsToLists = new HashMap<>();

        MySQLiteDB mySQLiteDB = new MySQLiteDB(this);

        cursor = mySQLiteDB.readProductsTableData();
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                products.put("code", cursor.getString(0));
                products.put("name", cursor.getString(1));
                products.put("grade", cursor.getString(2));
                products.put("nova_grade", cursor.getInt(3));
                products.put("ingredients", cursor.getString(4));
                products.put("nutrients", cursor.getString(5));
                products.put("product_image", cursor.getString(6));

                firestore.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_COLLECTION)
                        .document(cursor.getString(0)).set(products);
            }
        }

        cursor = mySQLiteDB.readListsTableData();
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                lists.put("id", cursor.getInt(0));
                lists.put("list_name", cursor.getString(1));
                lists.put("list_description", cursor.getString(2));
                lists.put("list_colour", cursor.getInt(3));

                firestore.collection(ROOT_COLLECTION).document(USER_ID).collection(LISTS_COLLECTION)
                        .document(cursor.getString(0)).set(lists);
            }
        }

        cursor = mySQLiteDB.readProductsToListsTableData();
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                productsToLists.put("product_code", cursor.getInt(0));
                productsToLists.put("list_id", cursor.getInt(1));

                firestore.collection(ROOT_COLLECTION).document(USER_ID).collection(PRODUCTS_TO_LISTS_COLLECTION)
                        .document(cursor.getString(0) +"_"+ cursor.getInt(1)).set(productsToLists);
            }
        }


    }
}