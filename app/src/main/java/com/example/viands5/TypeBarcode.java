package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TypeBarcode extends AppCompatActivity
{
    private Button cancelButton, searchButton;
    private TextView barcodePlainText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_barcode);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        //ab.

        barcodePlainText = findViewById(R.id.barcodePlainText);
        cancelButton = findViewById(R.id.typeBarcodeButton);

        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v->
        {
            if(!(barcodePlainText.getText().toString().isEmpty()))
            {
                searchProduct(barcodePlainText.getText().toString());
                barcodePlainText.setText("");
            }
        });



        cancelButton.setOnClickListener(v ->
        {
            finish();
        });

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    private void searchProduct(String barcode)
    {
        Intent i = new Intent(TypeBarcode.this, SearchingTheDatabse.class);
        i.putExtra("PRODUCT_BARCODE", barcode);
        startActivity(i);
    }
}