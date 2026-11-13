package com.db.Gui;

import com.db.Model.Column;
import com.db.Model.DataBase;
import com.db.Model.Row;
import com.db.Model.Table;
import com.db.SqlParser.QueryExecutor;
import com.db.Storage.FileStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainWindow extends JFrame {

    private final Color BACKGROUND = Color.decode("#F6FAFA");
    private final Color WHITE = Color.decode("#FFFFFF");
    private final Color PANEL_COLOR = Color.decode("#E4EFF0");
    private final Color ACCENT = Color.decode("#426D70");
    private final Color TEXT = Color.decode("#344C5A");
    private final Color BORDER = Color.decode("#DCE9ED");
    private static final Set<String> openedDatabases = new HashSet<>();

    private final DataBase database;
    private final String filePath;
    private final QueryExecutor executor;
    private final TableActions tableActions;
    private final DatabaseActions dbActions;

    private final DefaultListModel<String> tableListModel = new DefaultListModel<>();
    private JList<String> tableList;
    private JTable dataTable;
    private JLabel dataLabel;
    private JTextArea resultArea;
    private JTextArea sqlEditor;

    public MainWindow(DataBase database, String filePath) {
        this.database = database;
        this.filePath = filePath;
        this.executor = new QueryExecutor(database);
        this.tableActions = new TableActions(this);
        this.dbActions = new DatabaseActions(this);

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

        setTitle("MiniDB — " + database.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        buildUI();

        tableList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selectedTable = tableList.getSelectedValue();
                if (selectedTable != null) {
                    showTableData(selectedTable);
                }
            }
        });

        refreshTableList();
    }

    private void buildUI() {
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

        openDatabaseButton.addActionListener(e -> dbActions.openAnotherDatabase());
        createDatabaseButton.addActionListener(e -> dbActions.createAnotherDatabase());
        exitButton.addActionListener(e -> dbActions.exitApplication());
        deleteDatabaseButton.addActionListener(e -> dbActions.deleteCurrentDatabase());

        topButtons.add(openDatabaseButton);
        topButtons.add(createDatabaseButton);
        topButtons.add(deleteDatabaseButton);
        topButtons.add(exitButton);
        topPanel.add(topButtons, BorderLayout.EAST);

        JPanel leftPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
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

        createTableButton.addActionListener(e -> tableActions.createTable());
        deleteTableButton.addActionListener(e -> tableActions.deleteTable());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonsPanel.setBackground(BACKGROUND);

        JButton addButton = createButton("Add");
        JButton deleteButton = createButton("Delete");
        JButton editButton = createButton("Edit");
        JButton indexButton = createButton("Create Index");

        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(indexButton);

        addButton.addActionListener(e -> tableActions.addRow());
        deleteButton.addActionListener(e -> tableActions.deleteRow());
        editButton.addActionListener(e -> tableActions.editRow());
        indexButton.addActionListener(e -> tableActions.createIndex());

        JPanel dataPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        dataPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        dataLabel = new JLabel("Table's data");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 17));
        dataLabel.setForeground(TEXT);

        dataTable = new JTable(new Object[0][1], new String[]{"Choose a table"});
        configureDataTable();

        dataPanel.add(dataLabel, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        dataPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel sqlPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        sqlPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel sqlLabel = new JLabel("SQL-request");
        sqlLabel.setFont(new Font("Arial", Font.BOLD, 17));
        sqlLabel.setForeground(TEXT);

        sqlEditor = new JTextArea(5, 40);
        sqlEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sqlEditor.setForeground(TEXT);
        sqlEditor.setBackground(BACKGROUND);
        sqlEditor.setBorder(new EmptyBorder(10, 10, 10, 10));
        sqlEditor.setLineWrap(true);
        sqlEditor.setWrapStyleWord(true);

        JButton executeButton = createButton("Complete a request");
        executeButton.addActionListener(e -> executeSql());

        JPanel executePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        executePanel.setBackground(WHITE);
        executePanel.add(executeButton);

        sqlPanel.add(sqlLabel, BorderLayout.NORTH);
        JScrollPane sqlScroll = new JScrollPane(sqlEditor);
        sqlScroll.setBorder(BorderFactory.createEmptyBorder());
        sqlPanel.add(sqlScroll, BorderLayout.CENTER);
        sqlPanel.add(executePanel, BorderLayout.SOUTH);

        JPanel resultPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        resultPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel resultLabel = new JLabel("Result of the process");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 17));
        resultLabel.setForeground(TEXT);

        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setForeground(TEXT);
        resultArea.setBackground(BACKGROUND);
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        resultPanel.add(resultLabel, BorderLayout.NORTH);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createEmptyBorder());
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        JSplitPane bottomPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sqlPanel, resultPanel);
        bottomPanel.setResizeWeight(0.6);
        bottomPanel.setBorder(null);
        bottomPanel.setDividerSize(8);
        bottomPanel.setBackground(BACKGROUND);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 10));
        centerPanel.setBackground(BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataPanel, bottomPanel);
        mainSplit.setResizeWeight(0.6);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(8);
        mainSplit.setBackground(BACKGROUND);

        centerPanel.add(buttonsPanel, BorderLayout.NORTH);
        centerPanel.add(mainSplit, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void executeSql() {
        String sql = sqlEditor.getText().trim();
        if (sql.isEmpty()) return;

        resultArea.append("sql> " + sql + "\n");
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));

        try {
            executor.execute(sql);
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        resultArea.append(buffer.toString());
        resultArea.append("-----------------\n\n");
        refreshTableList();
        saveDatabase();
    }

    public void refreshTableList() {
        String selected = tableList.getSelectedValue();
        tableListModel.clear();
        for (Table table : database.getTables()) {
            tableListModel.addElement(table.getName());
        }
        if (selected != null && database.getTable(selected) != null) {
            tableList.setSelectedValue(selected, true);
        } else if (!tableListModel.isEmpty()) {
            tableList.setSelectedIndex(0);
        }
    }

    public void showTableData(String tableName) {
        Table table = database.getTable(tableName);
        if (table == null) return;

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
                data[i][j] = row.getValue(columnNames[j]);
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        dataTable.setModel(model);
        configureDataTable();
        dataLabel.setText("Table's data: " + tableName);
    }

    private void configureDataTable() {
        dataTable.setFont(new Font("Arial", Font.PLAIN, 13));
        dataTable.setRowHeight(28);
        dataTable.setGridColor(BORDER);
        dataTable.setBackground(WHITE);
        dataTable.setForeground(TEXT);
        dataTable.getTableHeader().setBackground(PANEL_COLOR);
        dataTable.getTableHeader().setForeground(TEXT);
        dataTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
    }

    public void saveDatabase() {
        try {
            FileStorage.save(database, filePath);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
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

    public DataBase getDatabase() { return database; }
    public String getFilePath() { return filePath; }
    public JList<String> getTableList() { return tableList; }
    public DefaultListModel<String> getTableListModel() { return tableListModel; }
    public JTable getDataTable() { return dataTable; }
    public JLabel getDataLabel() { return dataLabel; }
    public static Set<String> getOpenedDatabases() { return openedDatabases; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("Panel.background", Color.decode("#F6FAFA"));
            UIManager.put("Label.foreground", Color.decode("#344C5A"));
            UIManager.put("Component.focusColor", Color.decode("#6D9D98"));
            UIManager.put("Component.borderColor", Color.decode("#DCE9ED"));
            new StartWindow().setVisible(true);
        });
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(LayoutManager layout, Color color, int radius) {
            super(layout);
            this.radius = radius;
            setBackground(color);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}