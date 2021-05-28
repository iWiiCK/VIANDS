package com.example.viands5;

/**
 * 1 - This is a class dedicated to handling the creation of the a List object
 */
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

    public List(String id, String name, String description, String listColour)
    {
        this.id = Integer.parseInt(id);
        this.name = name;
        this.description = description;
        this.listColour = Integer.parseInt(listColour);
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
