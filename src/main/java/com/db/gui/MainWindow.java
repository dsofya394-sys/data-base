
package com.db.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    // Цветовая палитра Sea Glass
    private final Color BACKGROUND = Color.decode("#F6FAFA");
    private final Color WHITE = Color.decode("#FFFFFF");
    private final Color PANEL_COLOR = Color.decode("#E4EFF0");
    private final Color ACCENT = Color.decode("#426D70");
    private final Color TEXT = Color.decode("#344C5A");
    private final Color BORDER = Color.decode("#DCE9ED");

    public MainWindow() {

        setTitle("DataBase");
        //setSize(1100, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        //setLocationRelativeTo(null);
        //setLayout(new BorderLayout(10, 10));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setBackground(BACKGROUND);


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND);
        topPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel title = new JLabel("DataBase");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(TEXT);

        topPanel.add(title, BorderLayout.WEST);




        JPanel leftPanel = new RoundedPanel(new BorderLayout(5,10), WHITE, 20);
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(WHITE);
        leftPanel.setBorder(new EmptyBorder(15, 12, 15, 12));

        JLabel tablesLabel = new JLabel("Tables");
        tablesLabel.setFont(new Font("Arial", Font.BOLD, 17));
        tablesLabel.setForeground(TEXT);

        DefaultListModel<String> tableListModel = new DefaultListModel<>();
        JList<String> tableList = new JList<>(tableListModel);

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





        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonsPanel.setBackground(BACKGROUND);

        JButton addButton = createButton("Add");
        JButton deleteButton = createButton("Delete");
        JButton editButton = createButton("Edit");

        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(editButton);




        JPanel dataPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        dataPanel.setBackground(WHITE);
        dataPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel dataLabel = new JLabel("Table's data");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 17));
        dataLabel.setForeground(TEXT);

        String[] columns = {"Data will be shown after choosing the table"};
        JTable dataTable = new JTable(new Object[0][1], columns);

        dataTable.setFont(new Font("Arial", Font.PLAIN, 13));
        dataTable.setRowHeight(28);
        dataTable.setGridColor(BORDER);
        dataTable.setBackground(WHITE);
        dataTable.setForeground(TEXT);
        dataTable.getTableHeader().setBackground(PANEL_COLOR);
        dataTable.getTableHeader().setForeground(TEXT);
        dataTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        dataPanel.add(dataLabel, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        dataPanel.add(tableScroll, BorderLayout.CENTER);




        JPanel sqlPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        sqlPanel.setBackground(WHITE);
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

        JPanel executePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        executePanel.setBackground(WHITE);
        executePanel.add(executeButton);

        sqlPanel.add(sqlLabel, BorderLayout.NORTH);
        JScrollPane sqlScroll = new JScrollPane(sqlEditor);
        sqlScroll.setBorder(BorderFactory.createEmptyBorder());

        sqlPanel.add(sqlScroll, BorderLayout.CENTER);
        sqlPanel.add(executePanel, BorderLayout.SOUTH);




        JPanel resultPanel = new RoundedPanel(new BorderLayout(5, 10), WHITE, 20);
        resultPanel.setBackground(WHITE);
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
                JSplitPane.HORIZONTAL_SPLIT, sqlPanel, resultPanel);
        bottomPanel.setResizeWeight(0.6);
        bottomPanel.setBorder(null);
        bottomPanel.setDividerSize(8);
        bottomPanel.setBackground(BACKGROUND);




        JPanel centerPanel = new JPanel(new BorderLayout(5, 10));
        centerPanel.setBackground(BACKGROUND);
        centerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, dataPanel, bottomPanel);

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




    private JButton createButton(String text) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        //button.setBorder(new EmptyBorder(9, 18, 9, 18));
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

            UIManager.put("Panel.background", Color.decode("#F6FAFA"));
            UIManager.put("Label.foreground", Color.decode("#344C5A"));
            UIManager.put("Component.focusColor", Color.decode("#6D9D98"));
            UIManager.put("Component.borderColor", Color.decode("#DCE9ED"));

            MainWindow window = new MainWindow();
            window.setVisible(true);
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
