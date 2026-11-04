package com.db;

import com.db.model.Column;
import com.db.model.DataType;
import com.db.model.Row;
import javax.xml.crypto.Data;
import com.db.model.Table;
import com.db.model.DataBase;

public class Main {
    public static void main(String[] args) {
        System.out.println("-------------------------------------");
        System.out.println(" NICE TO GREET YOU IN OUR DATA BASE");
        System.out.println("-------------------------------------");
        System.out.println("Loading...");
        System.out.println("Success!");
        System.out.println("-------------------------------------");


        Column id = new Column(
                "id",
                DataType.INTEGER,
                true,
                false
        );

        //check of column.java
        System.out.println("Name: " + id.GetName());
        System.out.println("Type: " + id.GetDataType());
        System.out.println("Primary key: " + id.IsPrimaryKey());
        System.out.println("Can it be empty: " + id.IsNullable());
        System.out.println("-------------------------------------");

        // check of row.java
        Row row = new Row();
        row.SetValue("id", 1);
        row.SetValue("brand", "Toyota");
        row.SetValue("price", 250000.5);
        row.SetValue("year", 2022);
        row.SetValue("available", true);

        System.out.println();
        System.out.println("Automobile's data: ");

        System.out.println("ID: " + row.GetValue("id"));
        System.out.println("Brand: " + row.GetValue("brand"));
        System.out.println("Price: " + row.GetValue("price"));
        System.out.println("Year: " + row.GetValue("year"));
        System.out.println("Available: " + row.GetValue("available"));

        System.out.println("-------------------------------------");

        // check of table.java
        System.out.println("Creating a new table...");
        Table cars = new Table("Cars");
        cars.AddColumn(new Column("id", DataType.INTEGER, true, false));
        cars.AddColumn(new Column("brand", DataType.STRING, false, false));
        cars.AddColumn(new Column("price", DataType.DOUBLE, false, false));
        cars.AddColumn(new Column("year", DataType.INTEGER, false, false));
        cars.AddColumn(new Column("available", DataType.BOOLEAN, false, false));

        Row car1 = new Row();
        car1.SetValue("id", 1);
        car1.SetValue("brand", "Toyota");
        car1.SetValue("price", 25000);
        car1.SetValue("year", 2024);
        car1.SetValue("available", true);

        Row car2 = new Row();
        car2.SetValue("id", 2);
        car2.SetValue("brand", "BMW");
        car2.SetValue("price", 35000);
        car2.SetValue("year", 2025);
        car2.SetValue("available", true);

        cars.insert(car1);
        cars.insert(car2);

        System.out.println("Таблица: " + cars.GetName());
        System.out.println("Количество столбцов: " + cars.GetColumns().size());
        System.out.println("Количество записей: " + cars.GetRows().size());

        System.out.println("\nCars:");

        for (Row currentRow : cars.GetRows()) {
            System.out.println(
                    currentRow.GetValue("id") + " | " +
                            currentRow.GetValue("brand") + " | " +
                            currentRow.GetValue("price") + " | " +
                            currentRow.GetValue("year") + " | " +
                            currentRow.GetValue("available")
            );
        }

        System.out.println("-------------------------------------");
        // check of table.java
        DataBase database = new DataBase("CarDealer");

        database.AddTable(cars);

        System.out.println("Data base: " + database.GetName());
        System.out.println("Quantity of tables: " + database.GetTables().size());
        System.out.println("Table was found: "
                + database.GetTable("Cars").GetName());
    }
}

