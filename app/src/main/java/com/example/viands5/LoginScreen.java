package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
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
    private FirestoreHandler firestoreHandler;
    private MySQLiteDB mySQLiteDB;

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
        mySQLiteDB = new MySQLiteDB(this);
        firestoreHandler = new FirestoreHandler(this);


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
        Picasso.get().load(user.getPhotoUrl()).into(googleAccountImage);
        userName.setText(user.getDisplayName());

        backupDataButton.setOnClickListener(v->
        {
            displayBackupOverrideAlert();

        });

        restoreBackupButton.setOnClickListener(v->
        {
            displayRestoreAlert();
        });
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
        });

        builder.setNegativeButton("NO", (dialog, which) -> {});

        builder.setCancelable(true);
        builder.create().show();
    }
}