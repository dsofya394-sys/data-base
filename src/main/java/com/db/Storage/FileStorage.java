package com.db.Storage;
import com.db.Model.DataBase;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorage {
    public static void save(DataBase database, String filePath){
        try{
            Path path = Path.of(filePath);
            Files.createDirectories(path.getParent());

            try(ObjectOutputStream output =
                        new ObjectOutputStream(
                                new FileOutputStream(path.toFile()))){
                output.writeObject(database);
            }
            System.out.println("Database has saved");

        } catch (IOException e){
            throw new RuntimeException(
                    "Error while saving database: " + e.getMessage(),e
            );
        }
    }

    public static DataBase load(String filePath) {

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(filePath))) {

            Object object = input.readObject();

            if (!(object instanceof DataBase)) {
                throw new IOException("There is no database in files");
            }

            System.out.println("Database has loaded");

            return (DataBase) object;

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(
                    "Error while loading database: " + e.getMessage(), e
            );
        }
    }
}
