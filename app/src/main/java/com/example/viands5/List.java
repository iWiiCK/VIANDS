package com.example.viands5;

public class List
{
    private int id, listColour;
    private String name, description;

    public List(int id, String name, String description, int listColour)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.listColour = listColour;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getListColour() {
        return listColour;
    }
}
