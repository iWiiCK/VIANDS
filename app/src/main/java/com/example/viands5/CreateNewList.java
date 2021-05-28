package com.example.viands5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CreateNewList extends AppCompatActivity
{
    RadioGroup listColourRadioGroup;
    RadioButton cyanRadioButton, purpleRadioButton, pinkRadioButton, redRadioButton, greenRadioButton, yellowRadioButton;
    TextView listNamePlaintext, listDescriptionPlainText;
    Button saveListButton,cancelButton;
    MySQLiteDB mySQLiteDB = new MySQLiteDB(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_list);
        listColourRadioGroup = findViewById(R.id.listColourRadioGroup);

        cyanRadioButton = findViewById(R.id.cyanRadioButton);
        purpleRadioButton = findViewById(R.id.purpleRadioButton);
        pinkRadioButton = findViewById(R.id.pinkRadioButton);
        redRadioButton = findViewById(R.id.redRadioButton);
        greenRadioButton = findViewById(R.id.greenRadioButton);
        yellowRadioButton = findViewById(R.id.yellowRadioButton);

        //Default colour always checked when the activity is started
        cyanRadioButton.setChecked(true);



        listNamePlaintext = findViewById(R.id.listNamePlaintext);
        listDescriptionPlainText = findViewById(R.id.listDescriptionPlainText);
        saveListButton = findViewById(R.id.saveListButton);
        cancelButton = findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(v->
                finish());

        saveListButton.setOnClickListener(v->
        {
            if(!listNamePlaintext.getText().toString().isEmpty() && !listDescriptionPlainText.getText().toString().isEmpty())
            {
                int index = listColourRadioGroup.indexOfChild(findViewById(listColourRadioGroup.getCheckedRadioButtonId()));

                createList(listNamePlaintext.getText().toString(), listDescriptionPlainText.getText().toString(), index);
                //finish();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("REFRESH",true);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }


    private void createList(String name, String description, int colour)
    {
        int listId = mySQLiteDB.readNumOfLists();
        mySQLiteDB.addList(new List(listId, name, description, colour));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}