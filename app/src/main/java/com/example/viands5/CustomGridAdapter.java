package com.example.viands5;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * 1 - This class is the custom grid adapter for all the recycler views with Grid layouts.
 * 2 - The user defined lists are displayed to the user using this.
 * 3 - More over this class is also used to moving and copying products from one list to another.
 * 4 - Swiping individual items will prompt a delete alert to the user.
 */

public class CustomGridAdapter extends RecyclerView.Adapter<CustomGridAdapter.MyViewHolder>
{
    private final Activity ACTIVITY;
    private final Context CONTEXT;
    private final ArrayList<Integer> LIST_IDS;
    private final ArrayList<Integer> listColour;
    private final ArrayList<Integer> LIST_IDS_WITH_THE_PRODUCT_IN_IT;
    private final ArrayList<String> LIST_NAME;
    private final ArrayList<String> listDescription;
    private final ArrayList<String> numOfItemsInList;
    private final boolean isMoving;
    String movingBarcode;
    private final MySQLiteDB mySQLiteDB;


    public CustomGridAdapter(Activity activity, Context context, boolean isMoving, @Nullable String movingBarcode,
                             ArrayList<Integer> listId ,
                             ArrayList<String> listName,
                             ArrayList<String> listDescription,
                             ArrayList<Integer> listColour,
                             ArrayList<String> numOfItemsInList) {
        this.ACTIVITY = activity;
        this.CONTEXT = context;
        this.isMoving = isMoving;
        this.movingBarcode = movingBarcode;
        this.LIST_IDS = listId;
        this.LIST_NAME = listName;
        this.listDescription = listDescription;
        this.listColour = listColour;
        this.numOfItemsInList = numOfItemsInList;

        mySQLiteDB = new MySQLiteDB(context);
        LIST_IDS_WITH_THE_PRODUCT_IN_IT = mySQLiteDB.readListsWithTheSameBarcode(movingBarcode);
    }

    public ArrayList<Integer> getLIST_IDS() {
        return LIST_IDS;
    }

    public ArrayList<String> getListName() {
        return LIST_NAME;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(CONTEXT);
        View view = inflater.inflate(R.layout.recycler_view_grid_item, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        //Setting the List colour in each List item
        switch (listColour.get(position))
        {
            case 1:
                holder.cardTop.setBackgroundResource(R.color.list_colour_purple);
                break;

            case 2:
                holder.cardTop.setBackgroundResource(R.color.list_colour_pink);
                break;

            case 3:
                holder.cardTop.setBackgroundResource(R.color.list_colour_red);
                break;

            case 4:
                holder.cardTop.setBackgroundResource(R.color.list_colour_green);
                break;

            case 5:
                holder.cardTop.setBackgroundResource(R.color.list_colour_yellow);
                break;

            default:
                holder.cardTop.setBackgroundResource(R.color.list_colour_default);
        }


        holder.listName.setText(String.valueOf(LIST_NAME.get(position)));
        holder.numOfItemsInList.setText(String.valueOf(numOfItemsInList.get(position)));

        //Finding lists with the product already in it
        for (int i = 0; i < LIST_IDS_WITH_THE_PRODUCT_IN_IT.size(); i++)
        {
            if (LIST_IDS.get(position).equals(LIST_IDS_WITH_THE_PRODUCT_IN_IT.get(i)))
            {
                holder.cardTop.setBackgroundColor(Color.GRAY);
                holder.numOfItemsInList.setText(R.string.product_exists);
                holder.itemView.setEnabled(false);
            }
        }

        holder.itemView.setOnClickListener(v ->
        {
            if(isMoving && movingBarcode != null)
            {
                getConfirmation(holder, LIST_NAME.get(position), position);
            }

            else
            {
                Intent i = new Intent(CONTEXT, ClickedList.class);
                i.putExtra("LIST_ID", LIST_IDS.get(position));
                i.putExtra("LIST_NAME", LIST_NAME.get(position));
                i.putExtra("LIST_DESCRIPTION", listDescription.get(position));
                i.putExtra("LIST_COLOUR", listColour.get(position));
                ACTIVITY.startActivityForResult(i, 1);
            }
        });

    }


    @Override
    public int getItemCount() {
        return LIST_NAME.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView listName, numOfItemsInList;
        CardView cardTop, singleListItem;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            listName = itemView.findViewById(R.id.listName);
            numOfItemsInList = itemView.findViewById(R.id.numOfItemsInList);
            cardTop = itemView.findViewById(R.id.cardTop);
            singleListItem = itemView.findViewById(R.id.singleListItem);
        }
    }

    //Getting the confirmation before moving a product
    ////////////////////////////////////////////////////////////////////////////////////
    private void getConfirmation(MyViewHolder holder, String listName, int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
        builder.setTitle("Moving Product !");
        builder.setMessage("Are you sure you want to Move this product to " + listName + " ?");

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            mySQLiteDB.addProductsToLists(movingBarcode, LIST_IDS.get(position));
            Toast.makeText(CONTEXT, "Added To " + listName, Toast.LENGTH_SHORT).show();
            holder.cardTop.setBackgroundColor(Color.GRAY);
            holder.numOfItemsInList.setText(R.string.product_exists);
            holder.itemView.setEnabled(false);
            LIST_IDS_WITH_THE_PRODUCT_IN_IT.add(LIST_IDS.get(position));

        });

        builder.setNegativeButton("NO", (dialog, which) -> {});
        builder.create().show();
    }

    //Removing a list by taking the list position
    //////////////////////////////////////////////////
    public void removeList(int position)
    {
        mySQLiteDB.deleteList(LIST_IDS.get(position).toString());

        LIST_IDS.remove(position);
        LIST_NAME.remove(position);
        listDescription.remove(position);
        listColour.remove(position);

        notifyDataSetChanged();
    }

    //Removing all custom lists from the database
    ////////////////////////////////////////////////
    public void removeAllCustomLists()
    {
        mySQLiteDB.deleteAllCustomLists();

        LIST_IDS.clear();
        LIST_NAME.clear();
        listDescription.clear();
        listColour.clear();

        notifyDataSetChanged();
    }
}
