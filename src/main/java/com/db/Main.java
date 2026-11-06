
package com.db;

import com.db.model.Column;
import com.db.model.DataType;
import com.db.model.Row;
import com.db.model.Table;
import com.db.model.DataBase;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("-------------------------------------");
        System.out.println(" NICE TO GREET YOU IN OUR DATABASE");
        System.out.println("-------------------------------------");
        System.out.println("Loading...");
        System.out.println("Success!");
        System.out.println("-------------------------------------");

        // Check of Column.java
        Column id = new Column(
                "id",
                DataType.INTEGER,
                true,
                false
        );

        System.out.println("Name: " + id.getName());
        System.out.println("Type: " + id.getDataType());
        System.out.println("Primary key: " + id.isPrimaryKey());
        System.out.println("Can it be empty: " + id.isNullable());

        System.out.println("-------------------------------------");

        // Check of Row.java
        Row row = new Row();

        row.setValue("id", 1);
        row.setValue("brand", "Toyota");
        row.setValue("price", 250000.5);
        row.setValue("year", 2022);
        row.setValue("available", true);

        System.out.println();
        System.out.println("Automobile's data:");

        System.out.println("ID: " + row.getValue("id"));
        System.out.println("Brand: " + row.getValue("brand"));
        System.out.println("Price: " + row.getValue("price"));
        System.out.println("Year: " + row.getValue("year"));
        System.out.println("Available: " + row.getValue("available"));

        System.out.println("-------------------------------------");

        // Check of Table.java
        /*System.out.println("Creating a new table: ");

        Table cars = new Table("Cars");

        cars.addColumn(new Column("id", DataType.INTEGER, true, false));
        cars.addColumn(new Column("brand", DataType.STRING, false, false));
        cars.addColumn(new Column("price", DataType.DOUBLE, false, false));
        cars.addColumn(new Column("year", DataType.INTEGER, false, false));
        cars.addColumn(new Column("available", DataType.BOOLEAN, false, false));

        Row car1 = new Row();
        car1.setValue("id", 1);
        car1.setValue("brand", "Toyota");
        car1.setValue("price", 25000.0);
        car1.setValue("year", 2024);
        car1.setValue("available", true);

        Row car2 = new Row();
        car2.setValue("id", 2);
        car2.setValue("brand", "BMW");
        car2.setValue("price", 35000.0);
        car2.setValue("year", 2025);
        car2.setValue("available", true);

        cars.insert(car1);
        cars.insert(car2);

        System.out.println("Table: " + cars.getName());
        System.out.println("Number of columns: " + cars.getColumns().size());
        System.out.println("Number of records: " + cars.getRows().size());

        System.out.println("\nCars:");

        for (Row currentRow : cars.getRows()) {
            System.out.println(
                    currentRow.getValue("id") + " | " +
                            currentRow.getValue("brand") + " | " +
                            currentRow.getValue("price") + " | " +
                            currentRow.getValue("year") + " | " +
                            currentRow.getValue("available")
            );
        }

        System.out.println("-------------------------------------");

        // Check of DataBase.java
        DataBase database = new DataBase("CarDealer");

        database.addTable(cars);*/


// Stage 3: Saving and loading database

        String filePath = "data/CarDealer.db";

        DataBase database;
        Table cars;

        if (Files.exists(Path.of(filePath))) {

            System.out.println("Loading existing database");

            database = FileStorage.load(filePath);
            cars = database.getTable("Cars");

            if (cars == null) {
                throw new IllegalStateException(
                        "Cars table not found in database"
                );
            }

        } else {
            System.out.println("Creating a new database");

            database = new DataBase("CarDealer");

            cars = new Table("Cars");

            cars.addColumn(new Column("id", DataType.INTEGER, true, false));
            cars.addColumn(new Column("brand", DataType.STRING, false, false));
            cars.addColumn(new Column("price", DataType.DOUBLE, false, false));
            cars.addColumn(new Column("year", DataType.INTEGER, false, false));
            cars.addColumn(new Column("available", DataType.BOOLEAN, false, false));

            Row car1 = new Row();
            car1.setValue("id", 1);
            car1.setValue("brand", "Toyota");
            car1.setValue("price", 25000.0);
            car1.setValue("year", 2024);
            car1.setValue("available", true);

            Row car2 = new Row();
            car2.setValue("id", 2);
            car2.setValue("brand", "BMW");
            car2.setValue("price", 35000.0);
            car2.setValue("year", 2025);
            car2.setValue("available", true);

            cars.insert(car1);
            cars.insert(car2);

            database.addTable(cars);

            FileStorage.save(database, filePath);
        }

        System.out.println("Table: " + cars.getName());
        System.out.println("Number of columns: " + cars.getColumns().size());
        System.out.println("Number of records: " + cars.getRows().size());

        System.out.println("\nCars:");
        cars.printRows(cars.select());

        System.out.println("-------------------------------------");


        System.out.println("Database: " + database.getName());
        System.out.println("Number of tables: " + database.getTables().size());

        System.out.println("Table was found: " +
                database.getTable("Cars").getName());

        System.out.println("-------------------------------------");

        // Check of DataBase.java and Table.java after select delete update
        List<Row> result = cars.select();
        /*for(Row selectedRow : result){
            System.out.println(row.getValues());
        }*/
        cars.printRows(result);
    }
}
