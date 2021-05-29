package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

/**
 * 1 - This activity is there to handle the custom lists activities.
 * 2 - When clicking on a lists, the mapped data of the products to the lists are accessed and all the
 *      products on that particular list is displayed.
 */
public class ManageCustomListsActivity extends AppCompatActivity
{
    private ArrayList<String> listName, listDescription, numOfItemsInList;
    private ArrayList<Integer> listId, listColour;

    private final MySQLiteDB MY_SQLITE_DB = new MySQLiteDB(ManageCustomListsActivity.this);
    private CustomGridAdapter customGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_custom_lists);

        //Populating the Recycler View
        loadDataFromDB();
        customGridAdapter = new CustomGridAdapter(ManageCustomListsActivity.this,
                this,
                false,
                null,
                listId,
                listName,
                listDescription,
                listColour,
                numOfItemsInList);

        RecyclerView recyclerView = findViewById(R.id.customListsRecyclerView);
        recyclerView.setAdapter(customGridAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.scrollToPosition(customGridAdapter.getItemCount());

        //Adding a decorated divider to the recycler view items
        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.recycler_view_divider));
        recyclerView.addItemDecoration(itemDecorator);

        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);


        //Creating a new List activity
        CardView createNewListsClickable = findViewById(R.id.createNewListsClickable);
        createNewListsClickable.setOnClickListener(v ->
        {
            Intent i = new Intent(ManageCustomListsActivity.this, CreateNewList.class);
            startActivity(i);
        });

        //Viewing recent products list
        Button recentProductsListButton = findViewById(R.id.recentProductsListButton);
        recentProductsListButton.setOnClickListener(v ->
        {
            Intent i = new Intent(this, ClickedList.class);
            i.putExtra("LIST_ID",  0);
            i.putExtra("LIST_NAME", "Recent Products" );
            i.putExtra("LIST_DESCRIPTION", "This is list of products previously scanned by you");
            startActivity(i);
        });

    }

    //Updating the custom lists when the activity is restarted
    //////////////////////////////////////////////////////////////
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    //This method loads all the data from the SQLite database
    ////////////////////////////////////////////////////////////
    private void loadDataFromDB()
    {
        int index = 1;
        int count;
        listId = new ArrayList<>();
        listName = new ArrayList<>();
        listDescription = new ArrayList<>();
        listColour = new ArrayList<>();
        numOfItemsInList = new ArrayList<>();

        Cursor cursor = MY_SQLITE_DB.readListsTableData();

        if(cursor.getCount() != 0)
        {
            cursor.moveToNext();

            while (cursor.moveToNext())
            {
                listId.add(cursor.getInt(0));
                listName.add(cursor.getString(1));
                listDescription.add(cursor.getString(2));
                listColour.add(cursor.getInt(3));
                count = MY_SQLITE_DB.numOfProductsInList(index);
                numOfItemsInList.add(count + " Items");
                index++;
            }
        }

        //reversing the list since the grid layout start scroll position can't be manipulated when displaying on reverse
        Collections.reverse(listId);
        Collections.reverse(listName);
        Collections.reverse(listDescription);
        Collections.reverse(listColour);
        Collections.reverse(numOfItemsInList);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
    {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
        {
            alertDeleteList((String)customGridAdapter.getListName().get(viewHolder.getAbsoluteAdapterPosition()), viewHolder.getLayoutPosition());
        }

        //handling onswipe animations
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(ManageCustomListsActivity.this, R.color.swipe_bg_color))
                    .addActionIcon(R.drawable.ic_baseline_delete_filled_24)
                    .addSwipeRightLabel("DELETE")
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    //Alert dialog when the user swipes to delete a list item
    /////////////////////////////////////////////////////////////
    private void alertDeleteList(String name, int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove List");
        builder.setMessage("Are you sure you want to Remove " + name + " ?" );

        builder.setPositiveButton("YES", (dialog, which) ->
                customGridAdapter.removeList(position));

        builder.setNegativeButton("NO", (dialog, which) ->
                customGridAdapter.notifyItemChanged(position));

        builder.setCancelable(true);
        builder.create().show();
    }


    //Alert dialog for when the user bacth deletes all the data ina list
    //////////////////////////////////////////////////////////////////////
    private void displayDeleteAllOptions()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear List?");
        builder.setMessage("Are you sure you want to Clear all Custom Lists ?" );

        builder.setPositiveButton("YES", (dialog, which) ->
                customGridAdapter.removeAllCustomLists());

        builder.setNegativeButton("NO", (dialog, which) -> {});

        builder.setCancelable(true);
        builder.create().show();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_all_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.delete_button)
        {
            if(customGridAdapter.getLIST_IDS().size() > 0)
                displayDeleteAllOptions();
        }
        return super.onOptionsItemSelected(item);
    }
}