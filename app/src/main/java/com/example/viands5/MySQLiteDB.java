package com.example.viands5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MySQLiteDB extends SQLiteOpenHelper
{
    private Context context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "localDB.db";

    //Products Table
    public static final String PRODUCTS_TABLE =  "products";
    public static final String COLUMN_CODE =  "code";
    public static final String COLUMN_PRODUCT_NAME =  "name";
    public static final String COLUMN_GRADE =  "grade";
    public static final String COLUMN_NOVA_GROUP =  "nova_group";
    public static final String COLUMN_INGREDIENTS =  "ingredients";
    public static final String COLUMN_NUTRIENTS=  "nutrients";
    public static final String COLUMN_PRODUCT_IMAGE=  "product_image";

    //Lists Table
    public static final String LISTS_TABLE =  "lists";
    public static final String COLUMN_ID =  "id";
    public static final String COLUMN_LIST_NAME =  "name";
    public static final String COLUMN_DESCRIPTION =  "description";
    public static final String COLUMN_LIST_COLOUR =  "colour";

    //Products_to_list Table
    public static final String PRODUCTS_TO_LISTS_TABLE =  "products_to_lists";
    public static final String COLUMN_PRODUCT_CODE =  "product_code";
    public static final String COLUMN_LIST_ID =  "list_id";

    //User Preferences
    public static final String USER_PREFERENCES_TABLE =  "user_preferences";
    public static final String COLUMN_USER_ID =  "user_id";
    public static final String COLUMN_ENABLE_AUTO_BACKUP =  "enable_auto_backup";
    public static final String COLUMN_REMEMBER_LOGIN =  "remember_login";

    public MySQLiteDB(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private void autoBackupIfEnabled()
    {
        try
        {
            FirestoreHandler firestoreHandler = new FirestoreHandler(context);
            Cursor cursor = getUserPreferences(firestoreHandler.getUserID());
            if(cursor != null && cursor.getInt(1) == 1)
            {
                firestoreHandler.backUpLocalStorage();
            }
        }

        catch (Exception e)
        {

        }

    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createProductsTableQuery = "CREATE TABLE " + PRODUCTS_TABLE + "(" +
                COLUMN_CODE + " TEXT PRIMARY KEY ," +
                COLUMN_PRODUCT_NAME + " TEXT ," +
                COLUMN_GRADE + " TEXT ," +
                COLUMN_NOVA_GROUP + " INTEGER ," +
                COLUMN_INGREDIENTS + " TEXT ," +
                COLUMN_NUTRIENTS + " TEXT ," +
                COLUMN_PRODUCT_IMAGE + " BLOB " +
                ");";

        String createListsTableQuery = "CREATE TABLE " + LISTS_TABLE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
                COLUMN_LIST_NAME + " TEXT ," +
                COLUMN_DESCRIPTION + " TEXT ," +
                COLUMN_LIST_COLOUR + " INTEGER " +
                ");";

        String createProductsToListsTableQuery = "CREATE TABLE " + PRODUCTS_TO_LISTS_TABLE + "(" +
                COLUMN_PRODUCT_CODE + " TEXT ," +
                COLUMN_LIST_ID+ " INTEGER ," +
                " PRIMARY KEY ( " + COLUMN_PRODUCT_CODE + ", " + COLUMN_LIST_ID + ")" +
                ");";


        String createUserPreferencesTableQuery = "CREATE TABLE " + USER_PREFERENCES_TABLE + "(" +
                COLUMN_USER_ID + " TEXT PRIMARY KEY ," +
                COLUMN_ENABLE_AUTO_BACKUP+ " INTEGER ," +
                COLUMN_REMEMBER_LOGIN + " INTEGER " +
                ");";

        db.execSQL(createProductsTableQuery);
        db.execSQL(createListsTableQuery);
        db.execSQL(createProductsToListsTableQuery);
        db.execSQL(createUserPreferencesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LISTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TO_LISTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_PREFERENCES_TABLE);

        onCreate(db);
    }

    public void addUser(String userId, int enableAutoBackup, int rememberLogin)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_ENABLE_AUTO_BACKUP, enableAutoBackup);
        cv.put(COLUMN_REMEMBER_LOGIN, rememberLogin);

        db.insert(USER_PREFERENCES_TABLE, null, cv);
    }

    //Return the preferences of a specific user
    /////////////////////////////////////////////
    public Cursor getUserPreferences(String userId)
    {
        Cursor cursor = readUserPreferencesTableData();

        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                if(cursor.getString(0).equals(userId))
                    return cursor;
            }

            return cursor;
        }

        else
            return cursor;

    }

    //This method checks if the user exists in the local database
    ///////////////////////////////////////////////////////////////
    public boolean userExists(String userID)
    {
        Cursor cursor = readUserPreferencesTableData();

        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                if(cursor.getString(0).equals(userID))
                    return true;
            }

            return false;
        }

        else
            return false;

    }

    public void updateUserPreferences(String userID, int enableAutoBackup, int rememberLogin)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ENABLE_AUTO_BACKUP, enableAutoBackup);
        cv.put(COLUMN_REMEMBER_LOGIN, rememberLogin);

        db.update(USER_PREFERENCES_TABLE, cv, COLUMN_USER_ID + "=?", new String[]{userID});
    }


    //Returns the number of products ina list when the list id is given
    /////////////////////////////////////////////////////////////////////
    int numOfProductsInList(int listID)
    {
        int count = 0;
        String query = "SELECT * FROM " + PRODUCTS_TO_LISTS_TABLE + " WHERE " + COLUMN_LIST_ID + " = " + listID;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        if(cursor.getCount() != 0)
        {
            while (cursor.moveToNext())
            {
                count++;
            }
        }

        return count;
    }

    //Returns an ArrayList with the barcodes when a list Index is given
    ////////////////////////////////////////////////////////////////////
    ArrayList readBarcodeInList(int listID)
    {
        ArrayList<String> barcodeInList = new ArrayList<>();
        String query = "SELECT " + COLUMN_PRODUCT_CODE + " FROM " + PRODUCTS_TO_LISTS_TABLE + " WHERE " + COLUMN_LIST_ID + " = " + listID;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        while (cursor.moveToNext())
        {
            barcodeInList.add(cursor.getString(0));
        }

        return barcodeInList;
    }

    ArrayList readListsWithTheSameBarcode(String barcode)
    {
        ArrayList<Integer> listIds = new ArrayList<>();
        String query = "SELECT " + COLUMN_LIST_ID + " FROM " + PRODUCTS_TO_LISTS_TABLE + " WHERE " + COLUMN_PRODUCT_CODE + " = \"" + barcode + "\";";
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        while (cursor.moveToNext())
        {
            listIds.add(cursor.getInt(0));
        }

        return listIds;
    }

    //This method reads all the data from the User Preferences Table
    ////////////////////////////////////////////////////////
    Cursor readUserPreferencesTableData()
    {
        String query = "SELECT * FROM " + USER_PREFERENCES_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    //This method reads all the data from the Products Table
    ////////////////////////////////////////////////////////
    Cursor readProductsTableData()
    {
        String query = "SELECT * FROM " + PRODUCTS_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }


    //This method reads all the data from the Lists Table
    ////////////////////////////////////////////////////////
    Cursor readListsTableData()
    {
        String query = "SELECT * FROM " + LISTS_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    //This method reads all the data from the Lists Table
    ////////////////////////////////////////////////////////
    Cursor readProductsToListsTableData()
    {
        String query = "SELECT * FROM " + PRODUCTS_TO_LISTS_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = null;

        if(db != null)
        {
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    //Adding a product to the database
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean productAlreadySaved(String code)
    {
        Cursor cursor = readProductsTableData();

        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                if(cursor.getString(0).equals(code))
                    return true;
            }

            return false;
        }

        else
            return false;
    }

    public void addProduct(Product product)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CODE, product.getCode());
        cv.put(COLUMN_PRODUCT_NAME, product.getName());
        cv.put(COLUMN_GRADE, product.getGrade());
        cv.put(COLUMN_NOVA_GROUP, product.getNova_group());
        cv.put(COLUMN_INGREDIENTS, product.getIngredients());
        cv.put(COLUMN_NUTRIENTS, product.getNutrients());
        cv.put(COLUMN_PRODUCT_IMAGE, product.getProductImage());

        db.insert(PRODUCTS_TABLE, null, cv);

        addProductsToLists(product.getCode(), 0);

        autoBackupIfEnabled();
    }

    public void updateProduct(Product updatedProduct)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PRODUCT_NAME, updatedProduct.getName());
        cv.put(COLUMN_GRADE, updatedProduct.getGrade());
        cv.put(COLUMN_NOVA_GROUP, updatedProduct.getNova_group());
        cv.put(COLUMN_INGREDIENTS, updatedProduct.getIngredients());
        cv.put(COLUMN_NUTRIENTS, updatedProduct.getNutrients());
        cv.put(COLUMN_PRODUCT_IMAGE, updatedProduct.getProductImage());

        db.update(PRODUCTS_TABLE, cv, COLUMN_CODE + "=?", new String[]{updatedProduct.getCode()});

        autoBackupIfEnabled();
    }

    public void removeProduct(String barcode)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PRODUCTS_TABLE, COLUMN_CODE + " =? ", new String[]{barcode});

        autoBackupIfEnabled();
    }

    //Adding/Deleting a List to the database
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public int readNumOfLists() {
        String query = "SELECT  * FROM " + LISTS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean listExists(int listId)
    {
        Cursor cursor  = readListsTableData();

        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                if(cursor.getInt(0) == listId)
                    return true;
            }
        }

        return false;
    }

    public void updateList(int listId, @Nullable String newName, @Nullable String newDescription, int colour)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        if(newName != null)
            cv.put(COLUMN_LIST_NAME, newName);

        if(newDescription != null)
            cv.put(COLUMN_DESCRIPTION, newDescription);

        cv.put(COLUMN_LIST_COLOUR, colour);

        db.update(LISTS_TABLE, cv, COLUMN_ID + "=?", new String[]{Integer.toString(listId)});
        autoBackupIfEnabled();
    }


    public void addList(List list)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();


        cv.put(COLUMN_ID, list.getId());
        cv.put(COLUMN_LIST_NAME, list.getName());
        cv.put(COLUMN_DESCRIPTION, list.getDescription());
        cv.put(COLUMN_LIST_COLOUR, list.getListColour());

        db.insert(LISTS_TABLE, null, cv);
        autoBackupIfEnabled();
    }

    public void deleteAllCustomLists()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(LISTS_TABLE, COLUMN_ID + "!=?", new String[]{Integer.toString(0)});
        db.delete(PRODUCTS_TO_LISTS_TABLE, COLUMN_LIST_ID + "!=?", new String[]{Integer.toString(0)});
        autoBackupIfEnabled();
    }

    public void deleteList(String listID)
    {
        //Deleting the list from the lists table
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + LISTS_TABLE + " WHERE " + COLUMN_ID + " =\"" + listID + "\" ;");

        //Deleting the entry from products to lists table
        //Deleting a list will delete it's references in the products to list table as well
        db.execSQL("DELETE FROM " + PRODUCTS_TO_LISTS_TABLE + " WHERE " + COLUMN_LIST_ID + " =\"" + listID + "\" ;");

        autoBackupIfEnabled();
    }


    //Adding/Delete a product to  List
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public int numOfSameProductsInDifferentLists(String barcode)
    {
        int count = 0;
        Cursor cursor = readProductsToListsTableData();

        if(cursor != null)
        {
            while(cursor.moveToNext())
            {
                if(cursor.getString(0).equals(barcode))
                {
                    count++;
                }
            }
        }

        return count;
    }

    public void addProductsToLists(Product product, List list)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PRODUCT_CODE, product.getCode());
        cv.put(COLUMN_LIST_ID, list.getId());

        db.insert(PRODUCTS_TO_LISTS_TABLE, null, cv);
        autoBackupIfEnabled();
    }

    public void addProductsToLists(String productCode, int listId)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PRODUCT_CODE, productCode);
        cv.put(COLUMN_LIST_ID, listId);

        db.insert(PRODUCTS_TO_LISTS_TABLE, null, cv);
        autoBackupIfEnabled();

    }

    public void addProductsToLists(String productCode, String listId)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PRODUCT_CODE, productCode);
        cv.put(COLUMN_LIST_ID, Integer.parseInt(listId));

        db.insert(PRODUCTS_TO_LISTS_TABLE, null, cv);
        autoBackupIfEnabled();

    }

    public void deleteProductFromList(String productCode, int listId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PRODUCTS_TO_LISTS_TABLE, "product_code=? AND list_id=?", new String[]{productCode, Integer.toString(listId)});
        autoBackupIfEnabled();
    }

    public void clearProductsFromList(int listId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PRODUCTS_TO_LISTS_TABLE, "list_id=?", new String[]{Integer.toString(listId)});
        autoBackupIfEnabled();
    }


    //Delete all table rows from all the tables.
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void factoryResetDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + PRODUCTS_TABLE);
        //db.delete(LISTS_TABLE, COLUMN_ID + "!=?", new String[]{Integer.toString(0)});
        db.execSQL("DELETE FROM " + LISTS_TABLE);
        db.execSQL("DELETE FROM " + PRODUCTS_TO_LISTS_TABLE);

        //autoBackupIfEnabled();
    }
}
