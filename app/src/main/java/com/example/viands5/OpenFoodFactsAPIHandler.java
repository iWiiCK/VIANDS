package com.example.viands5;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 1 - This class handles all aspects of handling the open food facts api
 */
public class OpenFoodFactsAPIHandler
{
    private final Context CONTEXT;
    private final Activity ACTIVITY;
    private String barcode, name, grade, ingredients = "", nutrients = "",productImageUrl;
    private int statusCode = -1, novaGrade, complete;

    public OpenFoodFactsAPIHandler(Activity activity, Context context, String barcode)
    {
        this.CONTEXT = context;
        this.ACTIVITY = activity;
        parseApiData(barcode);
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getGrade() {
        return grade;
    }

    public int getNovaGrade() {
        return novaGrade;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getNutrients() {
        return nutrients;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getComplete() {return complete;}

    //This method takes product barcode as a parameter and fetches the data according to that.
    /////////////////////////////////////////////////////////////////////////////////////////////
    private void parseApiData(String productBarcode)
    {
        ProgressDialog progressDialog = new ProgressDialog(CONTEXT);
        progressDialog.setTitle("Connecting To the Database");
        progressDialog.setMessage("We are Still establishing a Connection to the Open Food Facts Database...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIcon(R.drawable.open_food_facts_symbol);

        //Concatenating the string to create the URL
        String url = "https://world.openfoodfacts.org/api/v0/product/";
        url += productBarcode + ".json";

        //Creating a string request for fetching the data
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response ->
        {
            progressDialog.dismiss();

            Log.e("res", response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject product = jsonObject.getJSONObject("product");
                JSONObject nutriments = product.getJSONObject("nutriments");
                JSONObject nutrientLevels = product.getJSONObject("nutrient_levels");

                //Getting the Status code. 1 = product Found
                statusCode = jsonObject.getInt("status");
                complete = product.getInt("complete");

                if (statusCode != 0 && complete != 0)
                {
                    //product barcode; - DONE
                    ///////////////////////////////////////////////////////////////////////////////////////
                    barcode = product.getString("_id");

                    //Product name - DONE
                    ///////////////////////////////////////////////////////////////////////////////////////
                    name = product.getString("product_name");

                    //product Nutrition Grade - DONE
                    ///////////////////////////////////////////////////////////////////////////////////////
                    grade = product.getString("nutriscore_grade");

                    //product Nova Group - DONE
                    ///////////////////////////////////////////////////////////////////////////////////////
                    novaGrade = product.getInt("nova_group");

                    //product Ingredients - DONE
                    ingredients = product.getString("ingredients_text_en");
                    ///////////////////////////////////////////////////////////////////////////////////////

                    //product Image Url - DONE
                    productImageUrl = product.getString("image_front_url");
                    ///////////////////////////////////////////////////////////////////////////////////////

                    //Nutrients
                    nutrients += "NUTRI-SCORE :" + product.getString("nutriscore_score") + "\n";
                    nutrients += "ENERGY :" + nutriments.getString("energy") + "\n\n";
                    nutrients += "\tFat - " + nutrientLevels.getString("fat") + "\n";
                    nutrients += "\tSalt - " + nutrientLevels.getString("salt") + "\n";
                    nutrients += "\tSaturated Fat - " + nutrientLevels.getString("saturated-fat") + "\n";
                    nutrients += "\tSugar - " + nutrientLevels.getString("sugars") + "\n\n";

                    nutrients += "NUTRIENTS PER 100g,\n\n";

                    nutrients += "\tCarbohydrates - " + nutriments.getString("carbohydrates_100g") + "g" + "\n";
                    nutrients += "\tEnergy(kcal) - " + nutriments.getString("energy-kcal_100g") + "kcal" + "\n";
                    nutrients += "\tEnergy(kJ) - " + nutriments.getString("energy-kj_100g") + "kJ" + "\n";
                    nutrients += "\tFat - " + nutriments.getString("fat_100g") + "g" + "\n";
                    nutrients += "\tSaturated Fat - " + nutriments.getString("saturated-fat_100g") + "g" + "\n";
                    nutrients += "\tProteins - " + nutriments.getString("proteins_100g") + "g" + "\n";
                    nutrients += "\tSalt - " + nutriments.getString("salt_100g") + "g" + "\n";
                    nutrients += "\tSodium - " + nutriments.getString("sodium_100g") + "g" + "\n";
                    nutrients += "\tSugar - " + nutriments.getString("sugars_100g") + "g" + "\n";
                }

            }
            catch (JSONException e)
            {
                progressDialog.dismiss();
                e.printStackTrace();
            }
            catch (Exception e)
            {
                progressDialog.dismiss();
                statusCode = 0;
                ACTIVITY.finish();
                Toast.makeText(CONTEXT, "Exception", Toast.LENGTH_SHORT).show();
            }

        }, volleyError -> {
            if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError)
            {
                Toast.makeText(CONTEXT, "Connection Error !", Toast.LENGTH_LONG).show();

            }
            else if (volleyError instanceof AuthFailureError)
            {
                Toast.makeText(CONTEXT, "Authentication/ Auth Error !", Toast.LENGTH_LONG).show();
            }
            else if (volleyError instanceof ServerError)
            {
                Toast.makeText(CONTEXT, "Server Error !", Toast.LENGTH_LONG).show();
            }
            else if (volleyError instanceof NetworkError)
            {
                Toast.makeText(CONTEXT, "Network Error !", Toast.LENGTH_LONG).show();
            }
            else if (volleyError instanceof ParseError)
            {
                Toast.makeText(CONTEXT, "Parse Error !", Toast.LENGTH_LONG).show();
            }

            ACTIVITY.finish();
        })
        {
            //Assigning the User Agent for the request
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("User-Agent:", "Viands - Android - Version 1.0");
                return params;
            }
        };

        //Creating a request queue using the Volley API
        RequestQueue requestQueue = Volley.newRequestQueue(CONTEXT);
        requestQueue.add(stringRequest);
        progressDialog.show();
    }
}

