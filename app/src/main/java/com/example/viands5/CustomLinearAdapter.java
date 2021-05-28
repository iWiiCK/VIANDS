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

public class CustomLinearAdapter extends RecyclerView.Adapter<CustomLinearAdapter.MyViewHolder >
{
    private final int listId;
    private final Activity activity;
    private final Context context;
    private final ArrayList<String> barcode;
    private final ArrayList<String> name;
    private final ArrayList<String> grade;
    private final ArrayList<String> ingredients;
    private final ArrayList<String> nutrients ;
    private final ArrayList<Integer> novaGroup;
    private final ArrayList<byte[]> productImage;
    private final boolean enableInteractions;
    private final MySQLiteDB mySQLiteDB;

    public ArrayList<String> getBarcode() {
        return barcode;
    }

    public ArrayList<String> getName() {
        return name;
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
        this.activity = activity;
        this.context = context;
        this.enableInteractions = enableInteractions;
        this.listId = listId;
        this.barcode = barcode;
        this.name = name;
        this.grade = grade;
        this.novaGroup= novaGroup;
        this.ingredients = ingredients;
        this.nutrients = nutrients;
        this.productImage = productImage;

        mySQLiteDB = new MySQLiteDB(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        if(productImage.get(position) == null) {
            holder.placeHolderImage.setImageResource(default_food_img);
        }
        else
        {
            ByteArrayInputStream imageStream = new ByteArrayInputStream((byte[]) productImage.get(position));
            Bitmap bitmap= BitmapFactory.decodeStream(imageStream);

            holder.placeHolderImage.setImageBitmap(bitmap);
        }

        holder.indexTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(String.valueOf(name.get(position)));
        holder.gradeTextView.setText(String.valueOf(grade.get(position)).toUpperCase());

        switch (grade.get(position)) {
            case "a":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_a));
                break;
            case "b":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_b));
                break;
            case "c":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_c));
                break;
            case "d":
                holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_d));
                break;
            default:
                holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_e));
                break;
        }


        if(enableInteractions)
        {
            holder.itemView.setOnClickListener(v ->
            {
                Intent i = new Intent(context, DisplayingProductDetails.class);
                i.putExtra("PRODUCT_BARCODE", barcode.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_NAME", name.get(holder.getBindingAdapterPosition()));
                i.putExtra("LOADED_FROM_LIST", true);
                i.putExtra("LIST_ID", this.listId);

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

                if (novaGroup.get(holder.getBindingAdapterPosition()).equals(1))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_1);
                else if (novaGroup.get(holder.getBindingAdapterPosition()).equals(2))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_2);
                else if (novaGroup.get(holder.getBindingAdapterPosition()).equals(3))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_3);
                else
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_4);


                i.putExtra("PRODUCT_INGREDIENTS", ingredients.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_NUTRIENTS", nutrients.get(holder.getBindingAdapterPosition()));
                i.putExtra("PRODUCT_IMAGE", (byte[]) productImage.get(holder.getBindingAdapterPosition()));
                activity.startActivityForResult(i, 1);
            });
        }
    }

    @Override
    public int getItemCount() {
        return name.size();
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
        mySQLiteDB.deleteProductFromList(barcode.get(position), listId);
        int count = mySQLiteDB.numOfSameProductsInDifferentLists(barcode.get(position));

        if(count == 0)
            mySQLiteDB.removeProduct(barcode.get(position));

        barcode.remove(position);
        name.remove(position);
        grade.remove(position);
        novaGroup.remove(position);
        ingredients.remove(position);
        nutrients.remove(position);
        productImage.remove(position);

        notifyDataSetChanged();
    }

    //Clear all products from the database
    //////////////////////////////////////////
    public void clearAllProducts()
    {
        int count;
        mySQLiteDB.clearAllProductsFromList(listId);

        for(int i = 0 ; i < barcode.size() ; i++)
        {
            count = mySQLiteDB.numOfSameProductsInDifferentLists(barcode.get(i));
            if(count == 0)
                mySQLiteDB.removeProduct(barcode.get(i));
        }

        barcode.clear();
        name.clear();
        grade.clear();
        novaGroup.clear();
        ingredients.clear();
        nutrients.clear();
        productImage.clear();

        notifyDataSetChanged();
    }
}
