package com.example.viands5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import static com.example.viands5.R.drawable.default_food_img;
import static com.example.viands5.R.drawable.open_food_facts_symbol;

public class CustomLinearAdapter extends RecyclerView.Adapter<CustomLinearAdapter.MyViewHolder >
{
    private int listId;
    private Activity activity;
    private Context context;
    private ArrayList barcode, name, grade, ingredients, nutrients ;
    private ArrayList novaGroup;
    private ArrayList productImage;
    private boolean enableInteractions;
    private MySQLiteDB mySQLiteDB;

    public ArrayList getBarcode() {
        return barcode;
    }

    public ArrayList getName() {
        return name;
    }

    public CustomLinearAdapter(Activity activity, Context context, boolean enableInteractions , int listId, ArrayList barcode, ArrayList name, ArrayList grade, ArrayList novaGroup, ArrayList ingredients, ArrayList nutrients, ArrayList productImage)
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

        if(grade.get(position).equals("a"))
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_a));
        else if(grade.get(position).equals("b"))
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_b));
        else if(grade.get(position).equals("c"))
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_c));
        else if(grade.get(position).equals("d"))
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_d));
        else
            holder.gradeTextView.setTextColor(ContextCompat.getColor(context, R.color.grade_e));


        if(enableInteractions)
        {
            holder.itemView.setOnClickListener(v ->
            {
                Intent i = new Intent(context, DisplayingProductDetails.class);
                i.putExtra("PRODUCT_BARCODE", barcode.get(holder.getBindingAdapterPosition()).toString());
                i.putExtra("PRODUCT_NAME", name.get(holder.getBindingAdapterPosition()).toString());
                i.putExtra("LOADED_FROM_LIST", true);
                i.putExtra("LIST_ID", this.listId);

                if (grade.get(holder.getBindingAdapterPosition()).equals("a"))
                    i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_a);
                else if (grade.get(holder.getBindingAdapterPosition()).equals("b"))
                    i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_b);
                else if (grade.get(holder.getBindingAdapterPosition()).equals("c"))
                    i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_c);
                else if (grade.get(holder.getBindingAdapterPosition()).equals("d"))
                    i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_d);
                else
                    i.putExtra("PRODUCT_GRADE", R.drawable.nutritional_value_e);

                if (novaGroup.get(holder.getBindingAdapterPosition()).equals(1))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_1);
                else if (novaGroup.get(holder.getBindingAdapterPosition()).equals(2))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_2);
                else if (novaGroup.get(holder.getBindingAdapterPosition()).equals(3))
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_3);
                else
                    i.putExtra("PRODUCT_NOVA_GROUP", R.drawable.nova_grade_4);


                i.putExtra("PRODUCT_INGREDIENTS", ingredients.get(holder.getBindingAdapterPosition()).toString());
                i.putExtra("PRODUCT_NUTRIENTS", nutrients.get(holder.getBindingAdapterPosition()).toString());
                i.putExtra("PRODUCT_IMAGE", (byte[]) productImage.get(holder.getBindingAdapterPosition()));
                activity.startActivityForResult(i, 1);
            });
        }
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
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

    public  void removeProduct(int position)
    {
        mySQLiteDB.deleteProductFromList(barcode.get(position).toString(), listId);
        int count = mySQLiteDB.numOfSameProductsInDifferentLists(barcode.get(position).toString());

        if(count == 0)
            mySQLiteDB.removeProduct(barcode.get(position).toString());

        barcode.remove(position);
        name.remove(position);
        grade.remove(position);
        novaGroup.remove(position);
        ingredients.remove(position);
        nutrients.remove(position);
        productImage.remove(position);

        notifyDataSetChanged();
    }

    public void clearAllProducts()
    {
        int count;
        mySQLiteDB.clearProductsFromList(listId);

        for(int i = 0 ; i < barcode.size() ; i++)
        {
            count = mySQLiteDB.numOfSameProductsInDifferentLists(barcode.get(i).toString());
            if(count == 0)
                mySQLiteDB.removeProduct(barcode.get(i).toString());
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
