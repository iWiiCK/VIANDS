package com.example.viands5;

import android.content.Context;

public class DummyDBData
{
 public DummyDBData() {}

   public void loadDummyData(MySQLiteDB localDB)
   {
       //Creating the dummy products and adding to the db
       Product dummyProduct1 = new Product("13674827236784", "Milk Powder and I'm just testing big names", "a", 3, "Chupacabra, Pinapple, Pasta", "Proteins, Carbohydrates, Fat", null);
       Product dummyProduct2 = new Product("15464762534948", "Russion Potatoes", "c", 2, "Acid, Gelepeenoes, Carrots, Zombie meet", "Fat, Protein", null);
       Product dummyProduct3 = new Product("76489476768948", "Demon Eggs", "e", 2, "Sugar, spice, Everything nice", "Lipids, Fat, Carbohydrates", null);
       Product dummyProduct4 = new Product("46537694636684", "Chimmy Changas", "d", 1, "Salads, Cows, Pork", "fat, Lips, Carbohydrates, Alien Protes", null);
       Product dummyProduct5 = new Product("34763656768448", "Eggos Chumps", "c", 4, "Sugar, Pepper, Salt, Coal, Cyanide", "Protein, death", null);

       localDB.addProduct(dummyProduct1);
       localDB.addProduct(dummyProduct2);
       localDB.addProduct(dummyProduct3);
       localDB.addProduct(dummyProduct4);
       localDB.addProduct(dummyProduct5);

       //Creating and Adding Dummy List to the dp
       List dummyList = new List(0, "Dummy Recent Products", "This is a list that is used for the testing of this Application", 0);
       List dummyList2 = new List( 1, "Dummy Custom List 2", "This is a list that is used for the testing of this Application", 1);
       List dummyList3 = new List( 2, "Dummy Custom List 3", "This is a list that is used for the testing of this Application", 2);
       List dummyList4 = new List( 3, "Dummy Custom List 4", "This is a list that is used for the testing of this Application", 3);
       List dummyList5 = new List( 4, "Dummy Custom List 5", "This is a list that is used for the testing of this Application", 4);
       List dummyList6 = new List( 5, "Dummy Custom List 6", "This is a list that is used for the testing of this Application", 5);
       List dummyList7 = new List( 6, "Dummy Custom List 7", "This is a list that is used for the testing of this Application", 2);
       List dummyList8 = new List( 7, "Dummy Custom List 8", "This is a list that is used for the testing of this Application", 4);
       List dummyList9 = new List( 8, "Dummy Custom List 9", "This is a list that is used for the testing of this Application", 2);
       List dummyList10 = new List( 9, "Dummy Custom List 10", "This is a list that is used for the testing of this Application", 3);
       List dummyList11 = new List( 10, "Dummy Custom List 11", "This is a list that is used for the testing of this Application", 1);
       List dummyList12 = new List( 11, "Dummy Custom List 12", "This is a list that is used for the testing of this Application", 5);
       List dummyList13 = new List( 12, "Dummy Custom List 13", "This is a list that is used for the testing of this Application", 2);
       localDB.addList(dummyList);
       localDB.addList(dummyList2);
       localDB.addList(dummyList3);
       localDB.addList(dummyList4);
       localDB.addList(dummyList5);
       localDB.addList(dummyList6);
       localDB.addList(dummyList7);
       localDB.addList(dummyList8);
       localDB.addList(dummyList9);
       localDB.addList(dummyList10);
       localDB.addList(dummyList11);
       localDB.addList(dummyList12);
       localDB.addList(dummyList13);

       //Map dummy products to dummy list
       localDB.addProductsToLists(dummyProduct1, dummyList);
       localDB.addProductsToLists(dummyProduct2, dummyList);
       localDB.addProductsToLists(dummyProduct3, dummyList);
       localDB.addProductsToLists(dummyProduct4, dummyList);
       localDB.addProductsToLists(dummyProduct5, dummyList);
       localDB.addProductsToLists(dummyProduct5, dummyList);

       localDB.addProductsToLists(dummyProduct1, dummyList2);
       localDB.addProductsToLists(dummyProduct4, dummyList2);
       localDB.addProductsToLists(dummyProduct2, dummyList8);
       localDB.addProductsToLists(dummyProduct5, dummyList2);
       localDB.addProductsToLists(dummyProduct3, dummyList9);

       localDB.addProductsToLists(dummyProduct1, dummyList3);
       localDB.addProductsToLists(dummyProduct2, dummyList3);
       localDB.addProductsToLists(dummyProduct3, dummyList4);
       localDB.addProductsToLists(dummyProduct4, dummyList6);
       localDB.addProductsToLists(dummyProduct5, dummyList7);
       localDB.addProductsToLists(dummyProduct1, dummyList10);
       localDB.addProductsToLists(dummyProduct2, dummyList10);
       localDB.addProductsToLists(dummyProduct3, dummyList10);
   }

   public void clearDummyData(MySQLiteDB localDB)
   {
       localDB.deleteAllTableData();
   }
}
