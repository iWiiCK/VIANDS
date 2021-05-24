package com.example.viands5;

import android.database.Cursor;

import java.util.ArrayList;

public class ProductsInListHandler
{
    private ArrayList<String> barcodeInList, name, grade, ingredients, nutrients ;
    private ArrayList<Integer> novaGroup;
    private ArrayList<byte[]> productImage;
    private int listID;

    public ProductsInListHandler(int listID)
    {
        this.listID = listID;
        this.barcodeInList = new ArrayList<>();
        this.name = new ArrayList<>();
        this.grade = new ArrayList<>();
        this.novaGroup = new ArrayList<>();
        this.ingredients = new ArrayList<>();
        this.nutrients = new ArrayList<>();
        this.productImage = new ArrayList<>();
    }

    public ArrayList<String> getBarcodeInList() {
        return barcodeInList;
    }

    public ArrayList<String> getName() {
        return name;
    }

    public ArrayList<String> getGrade() {
        return grade;
    }

    public ArrayList<Integer> getNovaGroup() {return novaGroup;}

    public ArrayList<String> getIngredients() {return ingredients;}

    public ArrayList<String> getNutrients() { return nutrients;}

    public ArrayList<byte[]> getProductImage() {
        return productImage;
    }

    public void loadList(MySQLiteDB mySQLiteDB)
    {
        Cursor productsCursor = mySQLiteDB.readProductsTableData();
        barcodeInList = mySQLiteDB.readBarcodeInList(listID);

        if(productsCursor.getCount() != 0)
        {
            while (productsCursor.moveToNext())
            {
                for(int i = 0 ; i < barcodeInList.size() ; i++)
                {
                    if(productsCursor.getString(0).compareTo(barcodeInList.get(i)) == 0)
                    {
                        name.add(productsCursor.getString(1));
                        grade.add(productsCursor.getString(2));
                        novaGroup.add(productsCursor.getInt(3));
                        ingredients.add(productsCursor.getString(4));
                        nutrients.add((productsCursor.getString(5)));
                        productImage.add(productsCursor.getBlob(6));
                    }
                }
            }
        }

    }
}
