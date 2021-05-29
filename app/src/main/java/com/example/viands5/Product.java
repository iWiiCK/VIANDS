package com.example.viands5;

import android.util.Log;
import androidx.annotation.Nullable;

/**
 * 1 - This class handles creation of a Product object
 */
public class Product
{
    private final String CODE;
    private String name;
    private final String GRADE;
    private final String INGREDIENTS;
    private final String NUTRIENTS;
    private final int NOVA_GROUP;
    private final byte[] PRODUCT_IMAGE;

    public Product(String code, String name, String grade, int nova_group, String ingredients, String nutrients, @Nullable byte[] productImage)
    {
        this.CODE = code;
        this.name = name;
        this.GRADE = grade;
        this.NOVA_GROUP = nova_group;
        this.INGREDIENTS = ingredients;
        this.NUTRIENTS = nutrients;
        this.PRODUCT_IMAGE = productImage;
    }

    public Product(String code, String name, String grade, String nova_group, String ingredients, String nutrients, @Nullable byte[] productImage)
    {
        this.CODE = code;
        this.name = name;
        this.GRADE = grade;
        this.NOVA_GROUP = Integer.parseInt(nova_group);
        this.INGREDIENTS = ingredients;
        this.NUTRIENTS = nutrients;
        this.PRODUCT_IMAGE = productImage;

        Log.d("product", "Nova Group" + " => " + nova_group);
    }


    public String getCODE()
    {
        return CODE;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGRADE()
    {
        return GRADE;
    }

    public String getINGREDIENTS()

    {
        return INGREDIENTS;
    }

    public String getNUTRIENTS()
    {
        return NUTRIENTS;
    }

    public int getNOVA_GROUP()
    {
        return NOVA_GROUP;
    }

    public byte[] getPRODUCT_IMAGE() {
        return PRODUCT_IMAGE;
    }
}
