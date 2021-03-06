package com.example.viands5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * 1 - This class is responsible for creating user defined new lists.
 * 2 - The default list which is the recent products list is created when the user starts the
 *      application for the first time
 */
public class CreateNewList extends AppCompatActivity
{
    private RadioGroup listColourRadioGroup;
    private TextView listNamePlaintext, listDescriptionPlainText;
    private final MySQLiteDB MY_SQLITE_DB = new MySQLiteDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_list);
        listColourRadioGroup = findViewById(R.id.listColourRadioGroup);

        RadioButton cyanRadioButton = findViewById(R.id.cyanRadioButton);
        //Default colour always checked when the activity is started
        cyanRadioButton.setChecked(true);


        listNamePlaintext = findViewById(R.id.listNamePlaintext);
        listDescriptionPlainText = findViewById(R.id.listDescriptionPlainText);
        Button saveListButton = findViewById(R.id.saveListButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v->
                finish());

        saveListButton.setOnClickListener(v->
        {
            if(!listNamePlaintext.getText().toString().isEmpty() && !listDescriptionPlainText.getText().toString().isEmpty())
            {
                int index = listColourRadioGroup.indexOfChild(findViewById(listColourRadioGroup.getCheckedRadioButtonId()));

                createList(listNamePlaintext.getText().toString(), listDescriptionPlainText.getText().toString(), index);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("REFRESH",true);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }


    //Create the new list in the SQLite database
    /////////////////////////////////////////////////////
    private void createList(String name, String description, int colour)
    {
        int listId = MY_SQLITE_DB.readNumOfLists();
        MY_SQLITE_DB.addList(new List(listId, name, description, colour));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}