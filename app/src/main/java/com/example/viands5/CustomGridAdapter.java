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

public class CustomGridAdapter extends RecyclerView.Adapter<CustomGridAdapter.MyViewHolder>
{
    private final Activity activity;
    private final Context context;
    private final ArrayList<Integer> listId;
    private final ArrayList<Integer> listColour;
    private final ArrayList<Integer> listIdsWithTheProductInIt;
    private final ArrayList<String> listName;
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
        this.activity = activity;
        this.context = context;
        this.isMoving = isMoving;
        this.movingBarcode = movingBarcode;
        this.listId = listId;
        this.listName = listName;
        this.listDescription = listDescription;
        this.listColour = listColour;
        this.numOfItemsInList = numOfItemsInList;

        mySQLiteDB = new MySQLiteDB(context);
        listIdsWithTheProductInIt = mySQLiteDB.readListsWithTheSameBarcode(movingBarcode);
    }

    public ArrayList<Integer> getListId() {
        return listId;
    }

    public ArrayList<String> getListName() {
        return listName;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_grid_item, parent, false);


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        //Setting the List colour in each List item
        switch ((int)listColour.get(position))
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


        holder.listName.setText(String.valueOf(listName.get(position)));
        holder.numOfItemsInList.setText(String.valueOf(numOfItemsInList.get(position)));

        //Finding lists with the product already in it
        for (int i = 0; i < listIdsWithTheProductInIt.size(); i++)
        {
            if (listId.get(position).equals(listIdsWithTheProductInIt.get(i)))
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
                getConfirmation(holder, (String)listName.get(position), position);
            }

            else
            {
                Intent i = new Intent(context, ClickedList.class);
                i.putExtra("LIST_ID", (Integer) listId.get(position));
                i.putExtra("LIST_NAME", (String) listName.get(position));
                i.putExtra("LIST_DESCRIPTION", (String) listDescription.get(position));
                i.putExtra("LIST_COLOUR", (Integer)listColour.get(position));
                activity.startActivityForResult(i, 1);
            }
        });

    }


    @Override
    public int getItemCount() {
        return listName.size();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Moving Product !");
        builder.setMessage("Are you sure you want to Move this product to " + listName + " ?");

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            mySQLiteDB.addProductsToLists(movingBarcode, (Integer) listId.get(position));
            Toast.makeText(context, "Added To " + listName, Toast.LENGTH_SHORT).show();
            holder.cardTop.setBackgroundColor(Color.GRAY);
            holder.numOfItemsInList.setText(R.string.product_exists);
            holder.itemView.setEnabled(false);
            listIdsWithTheProductInIt.add(listId.get(position));

        });

        builder.setNegativeButton("NO", (dialog, which) -> {});
        builder.create().show();
    }

    //Removing a list by taking the list position
    //////////////////////////////////////////////////
    public void removeList(int position)
    {
        mySQLiteDB.deleteList(listId.get(position).toString());

        listId.remove(position);
        listName.remove(position);
        listDescription.remove(position);
        listColour.remove(position);

        notifyDataSetChanged();
    }

    //Removing all custom lists from the database
    ////////////////////////////////////////////////
    public void removeAllCustomLists()
    {
        mySQLiteDB.deleteAllCustomLists();

        listId.clear();
        listName.clear();
        listDescription.clear();
        listColour.clear();

        notifyDataSetChanged();
    }
}
