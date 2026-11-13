package com.db;

import com.db.Gui.StartWindow;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Main {
    static void main() {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            StartWindow window = new StartWindow();
            window.setVisible(true);
        });
    }
}