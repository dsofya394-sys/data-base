package com.db.Gui;

import com.db.Model.DataBase;
import com.db.Storage.FileStorage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

public class DatabaseActions {
    private final MainWindow window;

    public DatabaseActions(MainWindow window) {
        this.window = window;
    }

    public void openAnotherDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose database");
        chooser.setFileFilter(new FileNameExtensionFilter("MiniDB files (*.db)", "db"));

        if (chooser.showOpenDialog(window) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        try {
            String selectedPath = file.getCanonicalPath();
            if (MainWindow.getOpenedDatabases().contains(selectedPath)) {
                JOptionPane.showMessageDialog(window, "This database is already open!", "Database already open", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DataBase newDatabase = FileStorage.load(selectedPath);
            new MainWindow(newDatabase, selectedPath).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to open database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createAnotherDatabase() {
        String name = JOptionPane.showInputDialog(window, "Enter database name:", "Create database", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;

        name = name.trim();
        if (!name.matches("[a-zA-Zа-яА-ЯёЁ0-9_-]+")) {
            JOptionPane.showMessageDialog(window, "Use only letters, numbers, _ and -");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save new database");
        chooser.setSelectedFile(new File(name + ".db"));
        chooser.setFileFilter(new FileNameExtensionFilter("MiniDB files (*.db)", "db"));

        if (chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        String filePath = file.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".db")) {
            filePath += ".db";
            file = new File(filePath);
        }

        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(window, "This file already exists. Overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
        }

        try {
            DataBase newDatabase = new DataBase(name);
            FileStorage.save(newDatabase, filePath);
            new MainWindow(newDatabase, filePath).setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to create database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void exitApplication() {
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            FileStorage.save(window.getDatabase(), window.getFilePath());
            window.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Failed to save database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteCurrentDatabase() {
        int result = JOptionPane.showConfirmDialog(window, "Are you sure you want to permanently delete this database?\nAll its tables and records will be lost!", "Delete database", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) return;

        try {
            File file = new File(window.getFilePath());
            if (!file.delete()) {
                JOptionPane.showMessageDialog(window, "Failed to delete database file!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            MainWindow.getOpenedDatabases().remove(file.getCanonicalPath());
            JOptionPane.showMessageDialog(window, "Database deleted successfully!");
            window.dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}