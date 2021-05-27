package com.example.viands5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class ClickedList extends AppCompatActivity
{
    private String movingProductBarcode = "";

    private int listId, listColour;
    private String listName, listDescription;
    private MySQLiteDB mySQLiteDB = new MySQLiteDB(ClickedList.this);
    private CustomLinearAdapter customLinearAdapter;
    private RecyclerView recyclerView;
    private TextView listDescriptionLabel;

    private CardView hiddenEditMenu;
    private TextView listNamePlaintext, listDescriptionPlainText;
    private Button cancelEditingButton, saveChangesButton;
    private RadioGroup listColourRadioGroup;
    private RadioButton cyanRadioButton, purpleRadioButton, pinkRadioButton, redRadioButton, greenRadioButton, yellowRadioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicked_list);

        hiddenEditMenu = findViewById(R.id.hiddenEditMenu);
        listNamePlaintext = findViewById(R.id.listNamePlaintext);
        listDescriptionPlainText = findViewById(R.id.listDescriptionPlainText);
        cancelEditingButton = findViewById(R.id.cancelEditingButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        listColourRadioGroup = findViewById(R.id.listColourRadioGroup);

        cyanRadioButton = findViewById(R.id.cyanRadioButton);
        purpleRadioButton = findViewById(R.id.purpleRadioButton);
        pinkRadioButton = findViewById(R.id.pinkRadioButton);
        redRadioButton = findViewById(R.id.redRadioButton);
        greenRadioButton = findViewById(R.id.greenRadioButton);
        yellowRadioButton = findViewById(R.id.yellowRadioButton);

        createList();
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(listName);
        populateRecyclerView(listId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        recreate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(ClickedList.this, ManageCustomListsActivity.class);
        startActivity(i);
    }

    private void createList()
    {
        listDescriptionLabel = findViewById(R.id.listDescriptionLabel);

        Bundle extras = getIntent().getExtras();

        listId = extras.getInt("LIST_ID");
        listName = extras.getString("LIST_NAME");
        listDescription = extras.getString("LIST_DESCRIPTION");
        listColour = extras.getInt("LIST_COLOUR");


        listDescriptionLabel.setText(listDescription);
    }


    //Populating the Recycler View
    /////////////////////////////////
    private void populateRecyclerView(int listId)
    {
        ProductsInListHandler productInList = new ProductsInListHandler(listId);
        productInList.loadList(mySQLiteDB);


        //Populating the Recycler View
        ///////////////////////////////////////////////////////////////////////////////////////////
        customLinearAdapter = new CustomLinearAdapter(ClickedList.this, this,
                true,
                listId,
                productInList.getBarcodeInList(),
                productInList.getName(),
                productInList.getGrade(),
                productInList.getNovaGroup(),
                productInList.getIngredients(),
                productInList.getNutrients(),
                productInList.getProductImage());

        recyclerView = findViewById(R.id.clickedListRecyclerView);
        recyclerView.setAdapter(customLinearAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ClickedList.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);

    }

    ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
    {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        //TODO: DELETING ISSUE
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
        {
            alertDeleteProductFromList(
                    (String)customLinearAdapter.getName().get(viewHolder.getAbsoluteAdapterPosition()), viewHolder.getAbsoluteAdapterPosition());
        }


        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
        {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(ClickedList.this, R.color.swipe_bg_color))
                    .addActionIcon(R.drawable.ic_baseline_delete_filled_24)
                    .addSwipeRightLabel("DELETE")
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        if(listId == 0)
            inflater.inflate(R.menu.delete_all_menu, menu);
        else
            inflater.inflate(R.menu.clicked_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.delete_button)
        {
            if(customLinearAdapter.getBarcode().size() > 0)
                displayDeleteAllOptions();
        }

        else if(item.getItemId() == R.id.edit_button)
        {
            displayEditOptions();
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayEditOptions()
    {
        ActionBar ab = getSupportActionBar();
        assert ab != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Changes");
        builder.setMessage("Save Changes Of The Current List ?" );

        switch (listColour)
        {
            case 1:
                purpleRadioButton.setChecked(true);
                break;

            case 2:
                pinkRadioButton.setChecked(true);
                break;

            case 3:
                redRadioButton.setChecked(true);
                break;

            case 4:
                greenRadioButton.setChecked(true);
                break;

            case 5:
                yellowRadioButton.setChecked(true);
                break;

            default:
                cyanRadioButton.setChecked(true);
        }

        hiddenEditMenu.setVisibility(View.VISIBLE);

        saveChangesButton.setOnClickListener(v ->
        {
            final int checkedColour = listColourRadioGroup.indexOfChild(findViewById(listColourRadioGroup.getCheckedRadioButtonId()));

            if(!listNamePlaintext.getText().toString().isEmpty() && !listDescriptionPlainText.getText().toString().isEmpty())
            {
                builder.setPositiveButton("YES", (dialog, which) ->
                {
                    ab.setTitle(listNamePlaintext.getText().toString());
                    listDescriptionLabel.setText(listDescriptionPlainText.getText().toString());
                    mySQLiteDB.updateList(listId, listNamePlaintext.getText().toString(), listDescriptionPlainText.getText().toString(), checkedColour);

                    listNamePlaintext.setText("");
                    listDescriptionPlainText.setText("");
                    hiddenEditMenu.setVisibility(View.GONE);
                });

                builder.setNegativeButton("NO", (dialog, which) -> {});

                builder.setCancelable(true);
                builder.create().show();
            }

            else if(!listNamePlaintext.getText().toString().isEmpty())
            {
                builder.setPositiveButton("YES", (dialog, which) ->
                {
                    ab.setTitle(listNamePlaintext.getText().toString());
                    mySQLiteDB.updateList(listId, listNamePlaintext.getText().toString(), null, checkedColour);

                    listNamePlaintext.setText("");
                    hiddenEditMenu.setVisibility(View.GONE);
                });

                builder.setNegativeButton("NO", (dialog, which) -> {});

                builder.setCancelable(true);
                builder.create().show();
            }

            else if(!listDescriptionPlainText.getText().toString().isEmpty())
            {
                builder.setPositiveButton("YES", (dialog, which) ->
                {
                    listDescriptionLabel.setText(listDescriptionPlainText.getText().toString());
                    mySQLiteDB.updateList(listId, null, listDescriptionPlainText.getText().toString(),checkedColour);

                    listDescriptionPlainText.setText("");
                    hiddenEditMenu.setVisibility(View.GONE);
                });

                builder.setNegativeButton("NO", (dialog, which) -> {});

                builder.setCancelable(true);
                builder.create().show();
            }

            else
            {
                builder.setPositiveButton("YES", (dialog, which) ->
                {
                    mySQLiteDB.updateList(listId, null, null,checkedColour);
                    hiddenEditMenu.setVisibility(View.GONE);
                });

                builder.setNegativeButton("NO", (dialog, which) -> {});

                builder.setCancelable(true);
                builder.create().show();
            }

            forceHideKeyboard(this, listNamePlaintext);
        });

        cancelEditingButton.setOnClickListener(v->
        {
            listNamePlaintext.setText("");
            listDescriptionPlainText.setText("");
            hiddenEditMenu.setVisibility(View.GONE);

            forceHideKeyboard(this, listNamePlaintext);
        });
    }

    private void displayDeleteAllOptions()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear List?");
        builder.setMessage("Are you sure you want to Clear all the data in " + listName + " ?" );

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            customLinearAdapter.clearAllProducts();
            Intent i = new Intent(ClickedList.this, ManageCustomListsActivity.class);
            startActivity(i);
        });

        builder.setNegativeButton("NO", (dialog, which) -> {});

        builder.setCancelable(true);
        builder.create().show();
    }


    private void alertDeleteProductFromList(String name, int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Product From List");
        builder.setMessage("Are you sure you want to Remove " + name + " from " + listName + " ?" );

        builder.setPositiveButton("YES", (dialog, which) ->
        {
            customLinearAdapter.removeProduct(position);

        });

        builder.setNegativeButton("NO", (dialog, which) ->
        {
            customLinearAdapter.notifyItemChanged(position);
        });

        builder.setCancelable(true);
        builder.create().show();
    }

    //Force Hiding the Keyboard
    public static void forceHideKeyboard(@NonNull Activity activity, @NonNull TextView editText) {
        if (activity.getCurrentFocus() == null || !(activity.getCurrentFocus() instanceof EditText)) {
            editText.requestFocus();
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

