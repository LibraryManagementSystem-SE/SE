package com.library;

import com.library.gui.LibraryGUI;
import com.library.system.LibraryApplication;
import com.library.system.LibraryEnvironment;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Check if the user wants to use the GUI or CLI
        if (args.length > 0 && "--cli".equals(args[0])) {
            // Run the command-line interface
            LibraryApplication.main(new String[]{});
        } else {
            // Run the graphical user interface
            try {
                // Set the look and feel to the system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Initialize the environment
                LibraryEnvironment environment = LibraryEnvironment.bootstrap();
                
                // Start the GUI
                SwingUtilities.invokeLater(() -> new LibraryGUI(environment));
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    "حدث خطأ أثناء تشغيل واجهة المستخدم الرسومية: " + e.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE
                );
                
                // Fall back to CLI if GUI fails
                System.err.println("Falling back to command-line interface...");
                LibraryApplication.main(new String[]{});
            }
        }
    }
}
