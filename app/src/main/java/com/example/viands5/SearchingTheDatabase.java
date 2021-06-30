package com.example.viands5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 1 - This class handles the result of a API call.
 * 2 - If the product exists, the data taken as JSON format are passed here.
 * 3 - As for the terms of using the 'Open Food Facts API', a disclaimer message is also displayed to the user
 *      before the user decided to view the results of a barcode scan.
 */
public class SearchingTheDatabase extends AppCompatActivity
{
    TextView nameLabel, descriptionText;
    Button viewResultsButton;
    OpenFoodFactsAPIHandler apiHandler;

    boolean refreshingProduct = false;
    boolean complete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_the_databse);

        nameLabel = findViewById(R.id.nameLabel);
        viewResultsButton = findViewById(R.id.viewResultButton);
        descriptionText = findViewById(R.id.descriptionText);

        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            apiHandler = new OpenFoodFactsAPIHandler(this,this, extras.getString("PRODUCT_BARCODE"));
            refreshingProduct = extras.getBoolean("PRODUCT_REFRESHED");
        }

        viewResultsButton.setOnClickListener(v->
        {
            if(apiHandler.getStatusCode() == -1 || apiHandler.getStatusCode() == 0 )
            {
                Toast.makeText(this, "Ops!, Invalid Barcode", Toast.LENGTH_SHORT).show();
                finish();
            }

            else
                displayProductDetails();

        });
    }

    //If the product exists, the details about the product are taken
    ///////////////////////////////////////////////////////////////////
    private void displayProductDetails()
    {
        Intent i = new Intent(SearchingTheDatabase.this, DisplayingProductDetails.class);
        i.putExtra("API_CODE", apiHandler.getBarcode());
        i.putExtra("API_NAME", apiHandler.getName());
        i.putExtra("API_GRADE", apiHandler.getGrade());

        if(apiHandler.getGrade() == null)
            i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_unknown);
        else
        {
                switch (apiHandler.getGrade()) {
                    case "a":
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_a);
                        break;
                    case "b":
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_b);
                        break;
                    case "c":
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_c);
                        break;
                    case "d":
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_d);
                        break;
                    case "e":
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_e);
                        break;
                    default:
                        i.putExtra("API_GRADE_DRAWABLE", R.drawable.nutritional_value_unknown);
                        break;
                }
        }

        if(apiHandler.getNovaGrade() == 1)
            i.putExtra("API_NOVA_GROUP_DRAWABLE", R.drawable.nova_grade_1);
        else if(apiHandler.getNovaGrade() == 2)
            i.putExtra("API_NOVA_GROUP_DRAWABLE", R.drawable.nova_grade_2);
        else if(apiHandler.getNovaGrade() == 3)
            i.putExtra("API_NOVA_GROUP_DRAWABLE", R.drawable.nova_grade_3);
        else if(apiHandler.getNovaGrade() == 4)
            i.putExtra("API_NOVA_GROUP_DRAWABLE", R.drawable.nova_grade_4);
        else
            i.putExtra("API_NOVA_GROUP_DRAWABLE", R.drawable.nova_grade_unknown);

        i.putExtra("API_NOVA_GROUP", apiHandler.getNovaGrade());
        i.putExtra("API_INGREDIENTS", apiHandler.getIngredients());
        i.putExtra("API_NUTRIENTS", apiHandler.getNutrients());
        i.putExtra("PRODUCT_IMAGE_URL", apiHandler.getProductImageUrl());

        i.putExtra("PRODUCT_REFRESHED", refreshingProduct);

        if(apiHandler.getGrade() != null && apiHandler.getNovaGrade() >= 1 && apiHandler.getNovaGrade() <= 4)
            complete = true;
        else
            complete = false;

        i.putExtra("COMPLETE", complete);
        startActivity(i);
        finish();
    }
}