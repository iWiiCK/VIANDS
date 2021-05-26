package com.example.viands5;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Product
{
    private String code, name, grade, ingredients, nutrients;
    private int nova_group;
    private byte[] productImage;

    public Product(String code, String name, String grade, int nova_group, String ingredients, String nutrients, @Nullable byte[] productImage)
    {
        this.code = code;
        this.name = name;
        this.grade = grade;
        this.nova_group = nova_group;
        this.ingredients = ingredients;
        this.nutrients = nutrients;
        this.productImage = productImage;
    }

    public Product(String code, String name, String grade, String nova_group, String ingredients, String nutrients, @Nullable byte[] productImage)
    {
        this.code = code;
        this.name = name;
        this.grade = grade;
        this.nova_group = Integer.parseInt(nova_group);
        this.ingredients = ingredients;
        this.nutrients = nutrients;
        this.productImage = productImage;

        Log.d("product", "Nova Group" + " => " + nova_group);
    }


    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getGrade()
    {
        return grade;
    }

    public void setGrade(String grade)
    {
        this.grade = grade;
    }

    public String getIngredients()

    {
        return ingredients;
    }

    public void setIngredients(String ingredients)
    {
        this.ingredients = ingredients;
    }

    public String getNutrients()
    {
        return nutrients;
    }

    public void setNutrients(String nutrients)
    {
        this.nutrients = nutrients;
    }

    public int getNova_group()
    {
        return nova_group;
    }

    public void setNova_group(int nova_group)
    {
        this.nova_group = nova_group;
    }

    public byte[] getProductImage() {
        return productImage;
    }
}
