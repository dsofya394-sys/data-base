package com.db.Gui;

import com.db.Model.DataBase;
import com.db.Storage.FileStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class StartWindow extends JFrame {

    private final Color BACKGROUND = Color.decode("#F6FAFA");
    private final Color WHITE = Color.decode("#FFFFFF");
    private final Color ACCENT = Color.decode("#426D70");
    private final Color TEXT = Color.decode("#344C5A");

    public StartWindow() {
        setTitle("MiniDB");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 430);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(BACKGROUND);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(35, 20, 20, 20));

        JLabel title = new JLabel("MiniDB");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Database Management System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(TEXT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(BACKGROUND);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBorder(new EmptyBorder(15, 70, 30, 70));

        JButton openButton = createButton("Open existing database");
        JButton createButton = createButton("Create new database");
        JButton exitButton = createButton("Exit");

        buttonsPanel.add(openButton);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(createButton);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(exitButton);

        JLabel footer = new JLabel("Sea Glass  |  MiniDB");
        footer.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.setForeground(TEXT);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(new EmptyBorder(10, 0, 15, 0));

        add(header, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        openButton.addActionListener(e -> openDatabase());
        createButton.addActionListener(e -> createDatabase());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void openDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose database");
        chooser.setFileFilter(new FileNameExtensionFilter("MiniDB files (*.db)", "db"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        try {
            DataBase database = FileStorage.load(file.getAbsolutePath());
            openMainWindow(database, file.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDatabase() {
        String name = JOptionPane.showInputDialog(this, "Enter database name:", "Create database", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;

        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Database name cannot be empty!");
            return;
        }

        if (!name.matches("[a-zA-Zа-яА-ЯёЁ0-9_-]+")) {
            JOptionPane.showMessageDialog(this, "Use only letters, numbers, _ and -");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save new database");
        chooser.setSelectedFile(new File(name + ".db"));
        chooser.setFileFilter(new FileNameExtensionFilter("MiniDB files (*.db)", "db"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        String filePath = file.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".db")) {
            filePath += ".db";
            file = new File(filePath);
        }

        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this, "This file already exists. Overwrite it?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        try {
            DataBase database = new DataBase(name);
            FileStorage.save(database, filePath);
            openMainWindow(database, filePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create database:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMainWindow(DataBase database, String filePath) {
        new MainWindow(database, filePath).setVisible(true);
        dispose();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(360, 50));
        button.setPreferredSize(new Dimension(360, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}