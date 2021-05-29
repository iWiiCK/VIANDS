package com.example.viands5;

import android.database.Cursor;
import java.util.ArrayList;

/**
 * 1 - This class is responsible for handling products in list
 * 2 - This class will define the products to be displayed in a list when the list id is given
 */
public class ProductsInListHandler
{
    private ArrayList<String> barcodeInList;
    private final ArrayList<String> NAME;
    private final ArrayList<String> GRADE;
    private final ArrayList<String> INGREDIENTS;
    private final ArrayList<String> NUTRIENTS;
    private final ArrayList<Integer> NOVA_GROUP;
    private final ArrayList<byte[]> PRODUCT_IMAGE;
    private final int LIST_ID;

    public ProductsInListHandler(int listID)
    {
        this.LIST_ID = listID;
        this.barcodeInList = new ArrayList<>();
        this.NAME = new ArrayList<>();
        this.GRADE = new ArrayList<>();
        this.NOVA_GROUP = new ArrayList<>();
        this.INGREDIENTS = new ArrayList<>();
        this.NUTRIENTS = new ArrayList<>();
        this.PRODUCT_IMAGE = new ArrayList<>();
    }

    public ArrayList<String> getBarcodeInList() {
        return barcodeInList;
    }

    public ArrayList<String> getNAME() {
        return NAME;
    }

    public ArrayList<String> getGRADE() {
        return GRADE;
    }

    public ArrayList<Integer> getNOVA_GROUP() {return NOVA_GROUP;}

    public ArrayList<String> getINGREDIENTS() {return INGREDIENTS;}

    public ArrayList<String> getNUTRIENTS() { return NUTRIENTS;}

    public ArrayList<byte[]> getPRODUCT_IMAGE() {
        return PRODUCT_IMAGE;
    }

    //taking data from the SQLite database and storing in arrayLists based on the list id provided
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadList(MySQLiteDB mySQLiteDB)
    {
        Cursor productsCursor = mySQLiteDB.readProductsTableData();
        barcodeInList = mySQLiteDB.readBarcodeInList(LIST_ID);

        if(productsCursor.getCount() != 0)
        {
            while (productsCursor.moveToNext())
            {
                for(int i = 0 ; i < barcodeInList.size() ; i++)
                {
                    if(productsCursor.getString(0).compareTo(barcodeInList.get(i)) == 0)
                    {
                        NAME.add(productsCursor.getString(1));
                        GRADE.add(productsCursor.getString(2));
                        NOVA_GROUP.add(productsCursor.getInt(3));
                        INGREDIENTS.add(productsCursor.getString(4));
                        NUTRIENTS.add((productsCursor.getString(5)));
                        PRODUCT_IMAGE.add(productsCursor.getBlob(6));
                    }
                }
            }
        }

    }
}
