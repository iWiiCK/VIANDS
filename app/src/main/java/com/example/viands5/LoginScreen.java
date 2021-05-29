package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

/**
 * 1 - This class handles all the activities related to the google login screen.
 * 2 - This will also handle the user preferences interactions once a sucessful
 *      Authentication has been made
 */
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

    private FirestoreHandler firestoreHandler;

    private TextView lastBackupText;
    private com.google.android.material.switchmaterial.SwitchMaterial autoBackupSwitch, rememberLoginSwitch;
    private MySQLiteDB mySQLiteDB;
    private String userID;


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
        lastBackupText = findViewById(R.id.lastBackupText);
        autoBackupSwitch = findViewById(R.id.autoBackupSwitch);
        rememberLoginSwitch = findViewById(R.id.rememberLoginSwitch);
        mySQLiteDB = new MySQLiteDB(this);


        //Always disable the restore button till searching for a backup is done.
        restoreBackupButton.setEnabled(false);
        restoreBackupButton.setBackgroundColor(Color.GRAY);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();


        //Check whether the user is logged in or not
        if(user != null)
        {
            userID = user.getUid();
            displayAlreadyLoggedInScreen();
            firestoreHandler = new FirestoreHandler(this);
        }

        else
            displaySignInScreen();


    }

    //Display the sign in screen to the user
    private void displaySignInScreen()
    {
        signInLayout.setVisibility(View.VISIBLE);
        alreadySignedInLayout.setVisibility(View.GONE);

        goBackButton.setOnClickListener(v-> finish());
        googleSignInButton.setOnClickListener(v->
                signIn());
    }

    //if the user is already signed in, display this screen.
    private void displayAlreadyLoggedInScreen()
    {
        signInLayout.setVisibility(View.GONE);
        alreadySignedInLayout.setVisibility(View.VISIBLE);
        handleUserPreferencesInteractions();
        displayUserData();

        signOutButton.setOnClickListener(v->
        {
            auth.signOut();
            displaySignInScreen();

        });
    }

    private void handleUserPreferencesInteractions()
    {
        boolean enableAutoBackup = false;
        boolean rememberLogin = false;
        Cursor cursor =  mySQLiteDB.getUserPreferences(userID);

        if(cursor != null)
        {
            if(cursor.getInt(1) == 1)
                enableAutoBackup = true;

            if(cursor.getInt(2) == 1)
                rememberLogin = true;
        }

        autoBackupSwitch.setChecked(enableAutoBackup);
        rememberLoginSwitch.setChecked(rememberLogin);

        autoBackupSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if(isChecked)
            {
                firestoreHandler.backUpLocalStorage();
                backupDataButton.setBackgroundColor(Color.GRAY);
                backupDataButton.setEnabled(false);

                mySQLiteDB.enableAutoBackup(userID, true);
            }

            else
            {
                backupDataButton.setBackgroundColor(Color.WHITE);
                backupDataButton.setEnabled(true);

                mySQLiteDB.enableAutoBackup(userID, false);

            }
        });

        rememberLoginSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mySQLiteDB.rememberUserLogin(userID, isChecked));
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
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        user = auth.getCurrentUser();

                        if(!mySQLiteDB.userExists(user.getUid()))
                        {
                            mySQLiteDB.addUser(user.getUid(), 0, 0);
                        }

                        recreate();
                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void displayUserData()
    {
        Picasso.get().load(user.getPhotoUrl()).into(googleAccountImage);
        userName.setText(user.getDisplayName());

        if(autoBackupSwitch.isChecked())
        {
            backupDataButton.setBackgroundColor(Color.GRAY);
            backupDataButton.setEnabled(false);
        }


        DocumentReference docRef = firestore.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {
                    if(document.getString("last_scanned") != null)
                    {
                        lastBackupText.setText(String.valueOf(Objects.requireNonNull(document.getData()).get("last_scanned")));
                        restoreBackupButton.setEnabled(true);
                        restoreBackupButton.setBackgroundColor(Color.WHITE);
                    }

                    else
                        lastBackupText.setText(R.string.no_backups_created);
                }
                else
                    lastBackupText.setText(R.string.no_backups_created);
            }
            else
            {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

        backupDataButton.setOnClickListener(v->
                displayBackupOverrideAlert());

        restoreBackupButton.setOnClickListener(v->
                displayRestoreAlert());
    }

    //Restoring an already created backup
    private void displayRestoreAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.restore_button_logo);
        builder.setTitle("RESTORING !");
        builder.setMessage("Restoring a Backup will change your current database with the latest backup you have created. This action is final" );

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            if(autoBackupSwitch.isChecked())
            {
                mySQLiteDB.enableAutoBackup(userID, false);
                autoBackupSwitch.setChecked(false);
                backupDataButton.setEnabled(true);
                backupDataButton.setBackgroundColor(Color.WHITE);
            }
            firestoreHandler.restoreBackup();
        });

        builder.setNegativeButton("NO", (dialog, which) -> {});

        builder.setCancelable(true);
        builder.create().show();
    }

    //Creating a backup
    private void displayBackupOverrideAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.backup_button_logo);
        builder.setTitle("NEW BACKUP !");
        builder.setMessage("A new backup will override any backups you have created earlier. Would you like to Override your previous Backup and create this new one ?" );

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            firestoreHandler.backUpLocalStorage();
            lastBackupText.setText(String.valueOf(Calendar.getInstance().getTime()));

            if(!restoreBackupButton.isEnabled())
            {
                restoreBackupButton.setEnabled(true);
                restoreBackupButton.setBackgroundColor(Color.WHITE);
            }
        });

        builder.setNegativeButton("NO", (dialog, which) -> {});

        builder.setCancelable(true);
        builder.create().show();
    }
}