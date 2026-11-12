package com.db.gui;
import com.db.Storage.FileStorage;
import com.db.Model.DataBase;
import com.db.Model.Table;
import com.db.Model.Column;
import com.db.Model.Row;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {

    private final Color BACKGROUND = Color.decode("#F6FAFA");
    private final Color WHITE = Color.decode("#FFFFFF");
    private final Color PANEL_COLOR = Color.decode("#E4EFF0");
    private final Color ACCENT = Color.decode("#426D70");
    private final Color TEXT = Color.decode("#344C5A");
    private final Color BORDER = Color.decode("#DCE9ED");
    private static final Set<String> openedDatabases = new HashSet<>();

    private DataBase database;
    private final String filePath;

    private final DefaultListModel<String> tableListModel =
            new DefaultListModel<>();

    private JList<String> tableList;
    private JTable dataTable;
    private JLabel dataLabel;


    public MainWindow(DataBase database, String filePath) {

        this.database = database;
        this.filePath = filePath;
        try {
            openedDatabases.add(new File(filePath).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    openedDatabases.remove(new File(filePath).getCanonicalPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        setTitle("DataBase");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND);
        topPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel title = new JLabel("DataBase");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(TEXT);

        topPanel.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topButtons.setBackground(BACKGROUND);

        JButton openDatabaseButton = createButton("Open database");
        JButton createDatabaseButton = createButton("Create database");
        JButton exitButton = createButton("Exit");
        JButton deleteDatabaseButton = createButton("Delete database");

        openDatabaseButton.addActionListener(e -> openAnotherDatabase());
        createDatabaseButton.addActionListener(e -> createAnotherDatabase());
        exitButton.addActionListener(e -> exitApplication());
        deleteDatabaseButton.addActionListener(e -> deleteCurrentDatabase());

        topButtons.add(openDatabaseButton);
        topButtons.add(createDatabaseButton);
        topButtons.add(deleteDatabaseButton);
        topButtons.add(exitButton);


        topPanel.add(topButtons, BorderLayout.EAST);



        JPanel leftPanel = new RoundedPanel(
                new BorderLayout(5, 10), WHITE, 20);

        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(new EmptyBorder(15, 12, 15, 12));

        JLabel tablesLabel = new JLabel("Tables");
        tablesLabel.setFont(new Font("Arial", Font.BOLD, 17));
        tablesLabel.setForeground(TEXT);

        tableList = new JList<>(tableListModel);

        tableList.setFont(new Font("Arial", Font.PLAIN, 14));
        tableList.setBackground(WHITE);
        tableList.setForeground(TEXT);
        tableList.setSelectionBackground(PANEL_COLOR);
        tableList.setSelectionForeground(TEXT);
        tableList.setBorder(new EmptyBorder(5, 5, 5, 5));




        leftPanel.add(tablesLabel, BorderLayout.NORTH);

        JScrollPane listScroll = new JScrollPane(tableList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());

        leftPanel.add(listScroll, BorderLayout.CENTER);

        JPanel tableButtonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        tableButtonsPanel.setBackground(WHITE);

        JButton createTableButton = createButton("Create table");
        JButton deleteTableButton = createButton("Delete table");

        tableButtonsPanel.add(createTableButton);
        tableButtonsPanel.add(deleteTableButton);

        leftPanel.add(tableButtonsPanel, BorderLayout.SOUTH);

        createTableButton.addActionListener(e -> createTable());
        deleteTableButton.addActionListener(e -> deleteTable());


        JPanel buttonsPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10));

        buttonsPanel.setBackground(BACKGROUND);

        JButton addButton = createButton("Add");
        JButton deleteButton = createButton("Delete");
        JButton editButton = createButton("Edit");

        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(editButton);
        addButton.addActionListener(e -> addRow());
        deleteButton.addActionListener(e -> deleteRow());
        editButton.addActionListener(e -> editRow());



        JPanel dataPanel = new RoundedPanel(
                new BorderLayout(5, 10), WHITE, 20);

        dataPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        dataLabel = new JLabel("Table's data");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 17));
        dataLabel.setForeground(TEXT);

        String[] columns = {"Choose a table"};
        dataTable = new JTable(new Object[0][1], columns);

        configureDataTable();

        dataPanel.add(dataLabel, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        dataPanel.add(tableScroll, BorderLayout.CENTER);




        JPanel sqlPanel = new RoundedPanel(
                new BorderLayout(5, 10), WHITE, 20);

        sqlPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel sqlLabel = new JLabel("SQL-request");
        sqlLabel.setFont(new Font("Arial", Font.BOLD, 17));
        sqlLabel.setForeground(TEXT);

        JTextArea sqlEditor = new JTextArea(5, 40);
        sqlEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sqlEditor.setForeground(TEXT);
        sqlEditor.setBackground(BACKGROUND);
        sqlEditor.setBorder(new EmptyBorder(10, 10, 10, 10));
        sqlEditor.setLineWrap(true);
        sqlEditor.setWrapStyleWord(true);

        JButton executeButton = createButton("Complete a request");

        JPanel executePanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        executePanel.setBackground(WHITE);
        executePanel.add(executeButton);

        sqlPanel.add(sqlLabel, BorderLayout.NORTH);

        JScrollPane sqlScroll = new JScrollPane(sqlEditor);
        sqlScroll.setBorder(BorderFactory.createEmptyBorder());

        sqlPanel.add(sqlScroll, BorderLayout.CENTER);
        sqlPanel.add(executePanel, BorderLayout.SOUTH);




        JPanel resultPanel = new RoundedPanel(
                new BorderLayout(5, 10), WHITE, 20);

        resultPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel resultLabel = new JLabel("Result of the process");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 17));
        resultLabel.setForeground(TEXT);

        JTextArea resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setForeground(TEXT);
        resultArea.setBackground(BACKGROUND);
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        resultPanel.add(resultLabel, BorderLayout.NORTH);

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createEmptyBorder());

        resultPanel.add(resultScroll, BorderLayout.CENTER);



        JSplitPane bottomPanel = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                sqlPanel,
                resultPanel
        );

        bottomPanel.setResizeWeight(0.6);
        bottomPanel.setBorder(null);
        bottomPanel.setDividerSize(8);
        bottomPanel.setBackground(BACKGROUND);




        JPanel centerPanel = new JPanel(new BorderLayout(5, 10));
        centerPanel.setBackground(BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                dataPanel,
                bottomPanel
        );

        mainSplit.setResizeWeight(0.6);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(8);
        mainSplit.setBackground(BACKGROUND);

        centerPanel.add(buttonsPanel, BorderLayout.NORTH);
        centerPanel.add(mainSplit, BorderLayout.CENTER);



        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);




        tableList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selectedTable = tableList.getSelectedValue();

                if (selectedTable != null) {
                    showTableData(selectedTable);
                }
            }
        });
        List<Table> tables = database.getTables();

        tableListModel.clear();

        for (Table table : tables) {
            tableListModel.addElement(table.getName());
        }

        if (!tableListModel.isEmpty()) {
            tableList.setSelectedIndex(0);
        }

        setTitle("MiniDB — " + database.getName());
    }



    private void openAnotherDatabase() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose database");
        chooser.setFileFilter(
                new FileNameExtensionFilter("MiniDB files (*.db)", "db")
        );

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            String selectedPath = file.getCanonicalPath();

            if (openedDatabases.contains(selectedPath)) {
                JOptionPane.showMessageDialog(
                        this,
                        "This database is already open!",
                        "Database already open",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            DataBase newDatabase = FileStorage.load(selectedPath);

            MainWindow newWindow = new MainWindow(
                    newDatabase,
                    selectedPath
            );

            newWindow.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to open database:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        try {
            DataBase newDatabase = FileStorage.load(file.getAbsolutePath());

            MainWindow newWindow = new MainWindow(
                    newDatabase,
                    file.getAbsolutePath()
            );

            newWindow.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to open database:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void createAnotherDatabase() {

        String name = JOptionPane.showInputDialog(
                this,
                "Enter database name:",
                "Create database",
                JOptionPane.PLAIN_MESSAGE
        );

        if (name == null) return;

        name = name.trim();

        if (!name.matches("[a-zA-Zа-яА-ЯёЁ0-9_-]+")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Use only letters, numbers, _ and -"
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save new database");
        chooser.setSelectedFile(new File(name + ".db"));
        chooser.setFileFilter(
                new FileNameExtensionFilter("MiniDB files (*.db)", "db")
        );

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        String filePath = file.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".db")) {
            filePath += ".db";
            file = new File(filePath);
        }

        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "This file already exists. Overwrite it?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer != JOptionPane.YES_OPTION) return;
        }

        try {
            DataBase newDatabase = new DataBase(name);

            FileStorage.save(newDatabase, filePath);

            MainWindow newWindow = new MainWindow(
                    newDatabase,
                    filePath
            );

            newWindow.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to create database:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exitApplication() {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            FileStorage.save(database, filePath);
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save database:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }



    private void configureDataTable() {

        dataTable.setFont(new Font("Arial", Font.PLAIN, 13));
        dataTable.setRowHeight(28);
        dataTable.setGridColor(BORDER);
        dataTable.setBackground(WHITE);
        dataTable.setForeground(TEXT);

        dataTable.getTableHeader().setBackground(PANEL_COLOR);
        dataTable.getTableHeader().setForeground(TEXT);
        dataTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));
    }






    private void showTableData(String tableName) {

        Table table = database.getTable(tableName);

        if (table == null) {
            return;
        }


        List<Column> columns = table.getColumns();

        String[] columnNames = new String[columns.size()];

        for (int i = 0; i < columns.size(); i++) {
            columnNames[i] = columns.get(i).getName();
        }


        List<Row> rows = table.select();

        Object[][] data = new Object[rows.size()][columns.size()];

        for (int i = 0; i < rows.size(); i++) {

            Row row = rows.get(i);

            for (int j = 0; j < columns.size(); j++) {

                String columnName = columnNames[j];

                data[i][j] = row.getValue(columnName);
            }
        }


        DefaultTableModel model = new DefaultTableModel(
                data,
                columnNames
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dataTable.setModel(model);
        configureDataTable();

        dataLabel.setText("Table's data: " + tableName);
    }





    private void addRow() {

        String tableName = tableList.getSelectedValue();

        if (tableName == null) {
            JOptionPane.showMessageDialog(this, "Choose a table first!");
            return;
        }

        Table table = database.getTable(tableName);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.setBackground(WHITE);

        java.util.Map<String, JComponent> fields = new java.util.LinkedHashMap<>();

        for (Column column : table.getColumns()) {

            JLabel label = new JLabel(column.getName());
            label.setForeground(TEXT);

            JComponent input;

            if (column.getDataType() == com.db.Model.DataType.BOOLEAN) {
                JComboBox<String> comboBox =
                        new JComboBox<>(new String[]{"true", "false"});
                input = comboBox;

            } else {
                JTextField textField = new JTextField(15);

                if (column.getDataType() == com.db.Model.DataType.DATE) {
                    textField.setToolTipText("Format: yyyy-MM-dd");
                }

                input = textField;
            }

            fields.put(column.getName(), input);

            form.add(label);
            form.add(input);
        }

        while (true) {

            int option = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Add record to " + tableName,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            Row newRow = new Row();
            java.util.List<String> errors = new java.util.ArrayList<>();

            for (Column column : table.getColumns()) {

                String columnName = column.getName();
                JComponent input = fields.get(columnName);

                String value;

                if (input instanceof JComboBox<?> comboBox) {
                    value = (String) comboBox.getSelectedItem();
                } else {
                    value = ((JTextField) input).getText().trim();
                }

                try {

                    Object parsedValue;

                    if (value.isEmpty()) {

                        if (column.isNullable()) {
                            parsedValue = null;
                        } else {
                            errors.add("Field '" + columnName + "' cannot be empty.");
                            continue;
                        }

                    } else {

                        switch (column.getDataType()) {

                            case INTEGER:
                                int intValue = Integer.parseInt(value);

                                if (columnName.equalsIgnoreCase("year")
                                        && intValue > java.time.LocalDate.now().getYear()) {

                                    errors.add(
                                            "Field '" + columnName
                                                    + "': year cannot be greater than the current year."
                                    );
                                    continue;
                                }

                                parsedValue = intValue;
                                break;

                            case DOUBLE:
                                double doubleValue = Double.parseDouble(value);

                                if (!Double.isFinite(doubleValue)) {
                                    errors.add(
                                            "Field '" + columnName + "': enter a finite number."
                                    );
                                    continue;
                                }

                                parsedValue = doubleValue;
                                break;

                            case STRING:
                                parsedValue = value;
                                break;

                            case DATE:
                                java.time.LocalDate date =
                                        java.time.LocalDate.parse(value);

                                if (date.isAfter(java.time.LocalDate.now())) {
                                    errors.add(
                                            "Field '" + columnName
                                                    + "': date cannot be later than today."
                                    );
                                    continue;
                                }

                                parsedValue = date;
                                break;

                            case BOOLEAN:
                                if (!value.equals("true") && !value.equals("false")) {
                                    errors.add(
                                            "Field '" + columnName
                                                    + "': enter true or false."
                                    );
                                    continue;
                                }

                                parsedValue = Boolean.valueOf(value);
                                break;

                            default:
                                errors.add(
                                        "Field '" + columnName + "': unsupported data type."
                                );
                                continue;
                        }
                    }

                    newRow.setValue(columnName, parsedValue);

                } catch (NumberFormatException e) {

                    errors.add(
                            "Field '" + columnName + "': invalid number."
                    );

                } catch (java.time.format.DateTimeParseException e) {

                    errors.add(
                            "Field '" + columnName
                                    + "': invalid date. Use yyyy-MM-dd."
                    );

                } catch (Exception e) {

                    errors.add(
                            "Field '" + columnName + "': " + e.getMessage()
                    );
                }
            }

            if (!errors.isEmpty()) {

                String message = "Please fix the following errors:\n\n"
                        + String.join("\n", errors);

                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Validation errors",
                        JOptionPane.ERROR_MESSAGE
                );

                continue;
            }

            try {
                database.insert(tableName, newRow);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to add record:\n" + e.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE
                );

                continue;
            }

            try {
                FileStorage.save(database, filePath);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(
                        this,
                        "Record was added, but could not be saved:\n"
                                + e.getMessage(),
                        "Save error",
                        JOptionPane.ERROR_MESSAGE
                );

                showTableData(tableName);
                return;
            }

            showTableData(tableName);

            JOptionPane.showMessageDialog(
                    this,
                    "Record successfully added!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }
    }


    private void openTable() {

        String tableName = tableList.getSelectedValue();

        if (tableName == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Choose a table first!"
            );
            return;
        }

        showTableData(tableName);
    }


    private void createTable() {

        String tableName;

        while (true) {
            tableName = JOptionPane.showInputDialog(
                    this, "Enter table name:", "Create table",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (tableName == null) return;

            tableName = tableName.trim();

            if (!tableName.matches("[a-zA-Zа-яА-ЯёЁ0-9_]+")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid table name. Use letters, numbers and _");
                continue;
            }

            if (database.getTable(tableName) != null) {
                JOptionPane.showMessageDialog(this,
                        "A table with this name already exists!");
                continue;
            }

            break;
        }

        int count;

        while (true) {
            String countText = JOptionPane.showInputDialog(
                    this, "Enter number of columns:"
            );

            if (countText == null) return;

            try {
                count = Integer.parseInt(countText);

                if (count < 1) {
                    JOptionPane.showMessageDialog(this,
                            "At least one column is required!");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid number!");
            }
        }

        Table newTable = new Table(tableName);
        java.util.Set<String> columnNames = new java.util.HashSet<>();
        boolean hasPrimaryKey = false;

        String[] types = {"INTEGER", "DOUBLE", "STRING", "DATE", "BOOLEAN"};

        for (int i = 0; i < count; i++) {

            String columnName;

            while (true) {
                columnName = JOptionPane.showInputDialog(
                        this,
                        "Name of column " + (i + 1) + ":"
                );

                if (columnName == null) return;

                columnName = columnName.trim();

                if (!columnName.matches("[a-zA-Zа-яА-ЯёЁ0-9_]+")) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid column name. Use letters, numbers and _");
                    continue;
                }

                if (columnNames.contains(columnName)) {
                    JOptionPane.showMessageDialog(this,
                            "This column already exists!");
                    continue;
                }

                break;
            }

            String selectedType = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose type for column " + columnName + ":",
                    "Column type",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    types,
                    types[0]
            );

            if (selectedType == null) return;

            com.db.Model.DataType type =
                    com.db.Model.DataType.valueOf(selectedType);

            boolean primaryKey;

            while (true) {
                primaryKey = JOptionPane.showConfirmDialog(
                        this,
                        "Is " + columnName + " a primary key?",
                        "Primary key",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION;

                if (primaryKey && hasPrimaryKey) {
                    JOptionPane.showMessageDialog(this,
                            "The table already has a primary key!");
                    continue;
                }

                if (i == count - 1 && !hasPrimaryKey && !primaryKey) {
                    JOptionPane.showMessageDialog(this,
                            "The table must have a primary key. Choose YES.");
                    continue;
                }

                break;
            }

            boolean nullable = false;

            if (!primaryKey) {
                nullable = JOptionPane.showConfirmDialog(
                        this,
                        "Can column " + columnName + " be empty?",
                        "Nullable",
                        JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION;
            }

            try {
                newTable.addColumn(
                        new Column(columnName, type, primaryKey, nullable)
                );

                columnNames.add(columnName);

                if (primaryKey) {
                    hasPrimaryKey = true;
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage()
                                + "\nPlease try again.");
                i--;
            }
        }

        try {
            database.addTable(newTable);
            FileStorage.save(database, filePath);

            tableListModel.addElement(tableName);
            tableList.setSelectedValue(tableName, true);

            JOptionPane.showMessageDialog(
                    this,
                    "Table successfully created!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to create table:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }



    private void deleteTable() {

        String tableName = tableList.getSelectedValue();

        if (tableName == null) {
            JOptionPane.showMessageDialog(this, "Choose a table first!");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete table '" + tableName + "'?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            database.removeTable(tableName);
            FileStorage.save(database, filePath);

            tableListModel.removeElement(tableName);

            if (!tableListModel.isEmpty()) {
                tableList.setSelectedIndex(0);
            } else {
                dataTable.setModel(new DefaultTableModel(
                        new Object[0][1],
                        new String[]{"Choose a table"}
                ));
                dataLabel.setText("Table's data");
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Table successfully deleted!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete table:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editRow() {

        String tableName = tableList.getSelectedValue();

        if (tableName == null) {
            JOptionPane.showMessageDialog(this, "Choose a table first!");
            return;
        }

        int selectedRow = dataTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a record to edit!"
            );
            return;
        }

        Table table = database.getTable(tableName);

        int modelRow = dataTable.convertRowIndexToModel(selectedRow);

        Column primaryKey = null;

        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }

        if (primaryKey == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primary key not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Object primaryKeyValue = dataTable.getModel().getValueAt(
                modelRow,
                table.getColumns().indexOf(primaryKey)
        );

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.setBackground(WHITE);

        java.util.Map<String, JComponent> fields =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < table.getColumns().size(); i++) {

            Column column = table.getColumns().get(i);
            String columnName = column.getName();

            Object currentValue =
                    dataTable.getModel().getValueAt(modelRow, i);

            JLabel label = new JLabel(columnName);
            label.setForeground(TEXT);

            JComponent input;

            if (column.getDataType() ==
                    com.db.Model.DataType.BOOLEAN) {

                JComboBox<String> comboBox =
                        new JComboBox<>(new String[]{"true", "false", ""});

                comboBox.setSelectedItem(
                        currentValue == null
                                ? ""
                                : currentValue.toString()
                );

                input = comboBox;

            } else {

                JTextField textField = new JTextField(15);

                textField.setText(
                        currentValue == null
                                ? ""
                                : currentValue.toString()
                );

                if (column.getDataType() ==
                        com.db.Model.DataType.DATE) {
                    textField.setToolTipText("Format: yyyy-MM-dd");
                }

                input = textField;
            }

            if (column.isPrimaryKey()) {
                input.setEnabled(false);
            }

            fields.put(columnName, input);

            form.add(label);
            form.add(input);
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit record in " + tableName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            java.util.Map<String, Object> updatedValues =
                    new java.util.LinkedHashMap<>();

            for (Column column : table.getColumns()) {

                if (column.isPrimaryKey()) {
                    continue;
                }

                String columnName = column.getName();
                JComponent input = fields.get(columnName);

                String value;

                if (input instanceof JComboBox<?> comboBox) {
                    value = (String) comboBox.getSelectedItem();
                } else {
                    value = ((JTextField) input).getText().trim();
                }

                Object parsedValue;

                if (value.isEmpty() && column.isNullable()) {

                    parsedValue = null;

                } else {

                    if (value.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Field '" + columnName +
                                        "' cannot be empty!"
                        );
                    }

                    switch (column.getDataType()) {

                        case INTEGER:
                            int yearValue = Integer.parseInt(value);

                            if (column.getName().equalsIgnoreCase("year")
                                    && yearValue > java.time.LocalDate.now().getYear()) {

                                throw new IllegalArgumentException(
                                        "Year cannot be greater than the current year!"
                                );
                            }

                            parsedValue = yearValue;
                            break;

                        case DOUBLE:
                            parsedValue = Double.valueOf(value);
                            break;

                        case STRING:
                            parsedValue = value;
                            break;

                        case DATE:
                            java.time.LocalDate date = java.time.LocalDate.parse(value);

                            if (date.isAfter(java.time.LocalDate.now())) {
                                throw new IllegalArgumentException(
                                        "Date cannot be later than today!"
                                );
                            }

                            parsedValue = date;
                            break;

                        case BOOLEAN:
                            if (!value.equals("true") &&
                                    !value.equals("false")) {
                                throw new IllegalArgumentException(
                                        "Enter true or false in " + columnName
                                );
                            }

                            parsedValue = Boolean.valueOf(value);
                            break;

                        default:
                            throw new IllegalArgumentException(
                                    "Unsupported data type: " +
                                            column.getDataType()
                            );
                    }
                }

                updatedValues.put(columnName, parsedValue);
            }

            int updated = database.update(
                    tableName,
                    primaryKey.getName(),
                    primaryKeyValue,
                    updatedValues
            );

            if (updated == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Record was not found!"
                );
                return;
            }

            FileStorage.save(database, filePath);

            showTableData(tableName);

            JOptionPane.showMessageDialog(
                    this,
                    "Record successfully updated!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to edit record:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void deleteRow() {

        String tableName = tableList.getSelectedValue();

        if (tableName == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Choose a table first!"
            );
            return;
        }

        int selectedRow = dataTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a record to delete!"
            );
            return;
        }

        Table table = database.getTable(tableName);

        Column primaryKey = null;

        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }

        if (primaryKey == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primary key not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int modelRow = dataTable.convertRowIndexToModel(selectedRow);

        int primaryKeyColumn = -1;

        for (int i = 0; i < table.getColumns().size(); i++) {
            if (table.getColumns().get(i).getName()
                    .equals(primaryKey.getName())) {
                primaryKeyColumn = i;
                break;
            }
        }

        Object primaryKeyValue =
                dataTable.getModel().getValueAt(modelRow, primaryKeyColumn);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete record with ID "
                        + primaryKeyValue + "?",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int deleted = database.delete(
                    tableName,
                    primaryKey.getName(),
                    primaryKeyValue
            );

            if (deleted == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Record was not found!"
                );
                return;
            }

            FileStorage.save(database, filePath);

            showTableData(tableName);

            JOptionPane.showMessageDialog(
                    this,
                    "Record successfully deleted!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete record:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void deleteCurrentDatabase() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this database?\n"
                        + "All its tables and records will be lost!",
                "Delete database",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            File file = new File(filePath);

            if (!file.delete()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to delete database file!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            openedDatabases.remove(file.getCanonicalPath());

            JOptionPane.showMessageDialog(
                    this,
                    "Database deleted successfully!"
            );

            dispose();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(9, 18, 9, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            com.formdev.flatlaf.FlatLightLaf.setup();

            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);

            UIManager.put("Panel.background",
                    Color.decode("#F6FAFA"));

            UIManager.put("Label.foreground",
                    Color.decode("#344C5A"));

            UIManager.put("Component.focusColor",
                    Color.decode("#6D9D98"));

            UIManager.put("Component.borderColor",
                    Color.decode("#DCE9ED"));

            StartWindow window = new StartWindow();
            window.setVisible(true);
        });
    }



    private static class RoundedPanel extends JPanel {

        private final int radius;

        public RoundedPanel(
                LayoutManager layout,
                Color color,
                int radius) {

            super(layout);

            this.radius = radius;

            setBackground(color);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(getBackground());

            g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    radius, radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
