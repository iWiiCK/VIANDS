package com.example.viands5;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * 1 - This class handles the typing of a barcode
 * 2 - When the user type a barcode. The types barcode is passed to the OpenFoodFactsApiHandler class
 */
public class TypeBarcode extends AppCompatActivity
{
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
        Button cancelButton = findViewById(R.id.typeBarcodeButton);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v->
        {
            if(!(barcodePlainText.getText().toString().isEmpty()))
            {
                searchProduct(barcodePlainText.getText().toString());
                barcodePlainText.setText("");
            }
        });

        cancelButton.setOnClickListener(v ->
                finish());

    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    //This method passes the user typed barcode
    ////////////////////////////////////////////////
    private void searchProduct(String barcode)
    {
        Intent i = new Intent(TypeBarcode.this, SearchingTheDatabase.class);
        i.putExtra("PRODUCT_BARCODE", barcode);
        startActivity(i);
    }
}