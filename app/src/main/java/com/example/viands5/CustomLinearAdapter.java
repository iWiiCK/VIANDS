package com.example.viands5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import static com.example.viands5.R.drawable.default_food_img;

/**
 * 1 - This class is used for displaying the details of food products to the user in a recycler view.
 * 2 - Clicking a product will display a new activity with detailed descriptions of each object.
 * 3 - Swiping the recycler view elements will prompt a delete alert.
 */

public class CustomLinearAdapter extends RecyclerView.Adapter<CustomLinearAdapter.MyViewHolder >
{
    private final int LIST_ID;
    private final Activity ACTIVITY;
    private final Context CONTEXT;
    private final ArrayList<String> BARCODE;
    private final ArrayList<String> NAMES;
    private final ArrayList<String> grade;
    private final ArrayList<String> INGREDIENTS;
    private final ArrayList<String> NUTRIENTS;
    private final ArrayList<Integer> NOVA_GROUPS;
    private final ArrayList<byte[]> PRODUCT_IMAGES;
    private final boolean ENABLE_INTERACTIONS;
    private final MySQLiteDB MY_SQLITE_DB;

    public ArrayList<String> getBARCODE() {
        return BARCODE;
    }

    public ArrayList<String> getNAMES() {
        return NAMES;
    }

    public CustomLinearAdapter(Activity activity, Context context, boolean enableInteractions , int listId,
                               ArrayList<String> barcode,
                               ArrayList<String> name,
                               ArrayList<String> grade,
                               ArrayList<Integer> novaGroup,
                               ArrayList<String> ingredients,
                               ArrayList<String> nutrients,
                               ArrayList<byte[]> productImage)
    {
        this.ACTIVITY = activity;
        this.CONTEXT = context;
        this.ENABLE_INTERACTIONS = enableInteractions;
        this.LIST_ID = listId;
        this.BARCODE = barcode;
        this.NAMES = name;
        this.grade = grade;
        this.NOVA_GROUPS = novaGroup;
        this.INGREDIENTS = ingredients;
        this.NUTRIENTS = nutrients;
        this.PRODUCT_IMAGES = productImage;

        MY_SQLITE_DB = new MySQLiteDB(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(CONTEXT);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        if(PRODUCT_IMAGES.get(position) == null) {
            holder.placeHolderImage.setImageResource(default_food_img);
        }
        else
        {
            ByteArrayInputStream imageStream = new ByteArrayInputStream(PRODUCT_IMAGES.get(position));
            Bitmap bitmap= BitmapFactory.decodeStream(imageStream);

            holder.placeHolderImage.setImageBitmap(bitmap);
        }

        holder.indexTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(String.valueOf(NAMES.get(position)));
        holder.gradeTextView.setText(String.valueOf(grade.get(position)).toUpperCase());

        switch (grade.get(position)) {
            case "a":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(CONTEXT, R.color.grade_a));
                break;
            case "b":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(CONTEXT, R.color.grade_b));
                break;
            case "c":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(CONTEXT, R.color.grade_c));
                break;
            case "d":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(CONTEXT, R.color.grade_d));
                break;
            default:
                holder.gradeTextView.setTextColor(ContextCompat.getColor(CONTEXT, R.color.grade_e));
                break;
        }


        //Enabling interactions everywhere except the main screen
        if(ENABLE_INTERACTIONS)
        {
            holder.itemView.setOnClickListener(v ->
            {
                Intent i = new Intent(CONTEXT, DisplayingProductDetails.class);
                i.putExtra("PRODUCT_BARCODE", BARCODE.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_NAME", NAMES.get(holder.getBindingAdapterPosition()));
                i.putExtra("LOADED_FROM_LIST", true);
                i.putExtra("LIST_ID", this.LIST_ID);

                switch (grade.get(holder.getBindingAdapterPosition())) {
                    case "a":
                        i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_a);
                        break;
                    case "b":
                        i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_b);
                        break;
                    case "c":
                        i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_c);
                        break;
                    case "d":
                        i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_d);
                        break;
                    default:
                        i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_e);
                        break;
                }

                if (NOVA_GROUPS.get(holder.getBindingAdapterPosition()).equals(1))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_1);
                else if (NOVA_GROUPS.get(holder.getBindingAdapterPosition()).equals(2))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_2);
                else if (NOVA_GROUPS.get(holder.getBindingAdapterPosition()).equals(3))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_3);
                else
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_4);


                i.putExtra("PRODUCT_INGREDIENTS", INGREDIENTS.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_NUTRIENTS", NUTRIENTS.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_IMAGE", PRODUCT_IMAGES.get(holder.getBindingAdapterPosition()));
                ACTIVITY.startActivityForResult(i, 1);
            });
        }
    }

    @Override
    public int getItemCount() {
        return NAMES.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView indexTextView, nameTextView, gradeTextView;
        ImageView placeHolderImage;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            indexTextView = itemView.findViewById(R.id.index);
            nameTextView = itemView.findViewById(R.id.productName);
            gradeTextView = itemView.findViewById(R.id.nutritionalValue);
            placeHolderImage = itemView.findViewById(R.id.placeHolderImage);
        }


    }

    //Remove Product from the database
    ///////////////////////////////////////
    public  void removeProduct(int position)
    {
        MY_SQLITE_DB.deleteProductFromList(BARCODE.get(position), LIST_ID);
        int count = MY_SQLITE_DB.numOfSameProductsInDifferentLists(BARCODE.get(position));

        if(count == 0)
            MY_SQLITE_DB.removeProduct(BARCODE.get(position));

        BARCODE.remove(position);
        NAMES.remove(position);
        grade.remove(position);
        NOVA_GROUPS.remove(position);
        INGREDIENTS.remove(position);
        NUTRIENTS.remove(position);
        PRODUCT_IMAGES.remove(position);

        notifyDataSetChanged();
    }

    //Clear all products from the database
    //////////////////////////////////////////
    public void clearAllProducts()
    {
        int count;
        MY_SQLITE_DB.clearAllProductsFromList(LIST_ID);

        for(int i = 0; i < BARCODE.size() ; i++)
        {
            count = MY_SQLITE_DB.numOfSameProductsInDifferentLists(BARCODE.get(i));
            if(count == 0)
                MY_SQLITE_DB.removeProduct(BARCODE.get(i));
        }

        BARCODE.clear();
        NAMES.clear();
        grade.clear();
        NOVA_GROUPS.clear();
        INGREDIENTS.clear();
        NUTRIENTS.clear();
        PRODUCT_IMAGES.clear();

        notifyDataSetChanged();
    }
}
