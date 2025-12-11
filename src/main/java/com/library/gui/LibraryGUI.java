package com.library.gui;
import com.library.domain.User;
import com.library.domain.UserRole;
import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.Media;
import com.library.domain.MediaType;
import com.library.domain.Loan;
import com.library.service.LibraryException;
import com.library.service.BorrowService;
import com.library.repository.LoanRepository;
import com.library.repository.MediaRepository;
import com.library.system.LibraryEnvironment;
import com.library.support.DateProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LibraryGUI {
    private final LibraryEnvironment environment;
    private JFrame frame;
    private JPanel currentPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    /**
     * Creates the graphical user interface for the Library Management System.
     *
     * @param environment the application environment containing services and repositories
     */


    public LibraryGUI(LibraryEnvironment environment) {
        this.environment = environment;
        initialize();
    }
    /**
     * Application entry point. Initializes the environment and launches the GUI.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Set the application to use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the environment (same as in the CLI version)
                LibraryEnvironment environment = LibraryEnvironment.bootstrap();
                new LibraryGUI(environment);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to initialize the application: " + e.getMessage(),
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Initializes the main application window and displays the login screen.
     * Sets basic styling and prepares the main JFrame.
     */
   
    private void initialize() {
        try {
            // Set system look and feel for a native appearance
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the main window
        frame = new JFrame("Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        // Set application icon
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/library-icon.png"));
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Icon not found, using default");
        }

        showLoginPanel();

        frame.setVisible(true);
    }
    /**
     * Displays the login screen where users can enter their credentials.
     * Replaces any currently visible panel.
     */
    private void showLoginPanel() {
        if (currentPanel != null) {
            frame.remove(currentPanel);
        }

        // Create the main panel with card layout
        currentPanel = new JPanel(new CardLayout());
        currentPanel.setBackground(new Color(240, 240, 245));

        // Create the login card
        JPanel loginCard = new JPanel(new GridBagLayout());
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 60, 60, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(titleLabel, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(10, 0, 5, 0);
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        loginCard.add(userLabel, gbc);

        gbc.gridy = 2;
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        loginCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 5, 0);
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(52, 73, 94));
        loginCard.add(passLabel, gbc);

        gbc.gridy = 4;
        passwordField = new JPasswordField(20);
        stylePasswordField(passwordField);
        loginCard.add(passwordField, gbc);

        // Login Button
        gbc.gridy = 5;
        gbc.insets = new Insets(30, 0, 10, 0);
        JButton loginButton = new JButton("SIGN IN");
        styleButton(loginButton, new Color(52, 152, 219), Color.WHITE);
        loginButton.addActionListener(e -> attemptLogin());
        loginCard.add(loginButton, gbc);

        // Sign Up Button
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 15, 0);
        JButton signUpButton = new JButton("CREATE NEW ACCOUNT");
        styleButton(signUpButton, new Color(46, 204, 113), Color.WHITE);
        signUpButton.addActionListener(e -> showSignUpPanel());
        loginCard.add(signUpButton, gbc);

        // Add the login card to the main panel
        currentPanel.add(loginCard, "login");
        frame.add(currentPanel);

        // Make sure the password field can be submitted with Enter key
        passwordField.addActionListener(e -> attemptLogin());

        // Refresh the frame
        frame.revalidate();
        frame.repaint();
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(300, 40));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void stylePasswordField(JPasswordField passwordField) {
        styleTextField(passwordField);
        passwordField.setEchoChar('•');
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 45));
    }

    private JButton createDashboardButton(String text, String icon) {
        JButton button = new JButton("<html><div style='text-align:center;'>" + 
            "<span style='font-size:24px;'>" + icon + "</span><br/>" + text + "</div>");
        
        // Base styling
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(44, 62, 80));
        button.setBackground(new Color(236, 240, 241));
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));
        
        // Hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 233, 235));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(236, 240, 241));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        // Click effect
        button.addChangeListener(e -> {
            if (button.getModel().isPressed()) {
                button.setBackground(new Color(220, 223, 225));
            } else {
                button.setBackground(new Color(236, 240, 241));
            }
        });
        
        // Make the button non-opaque for better hover effects
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        
        return button;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(frame,
                "Please enter both username and password.",
                "Login Failed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Authenticate the user
            User user = environment.getAuthService().login(username, new String(password));

            // Clear the password field for security
            passwordField.setText("");

            // Show success message
            JOptionPane.showMessageDialog(frame,
                "Login successful! Welcome, " + user.getName() + ".",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

            // Show main dashboard with user info
            showMainDashboard(user.getName(), user.isAdmin());

        } catch (LibraryException ex) {
            // Clear the password field on failed login
            passwordField.setText("");

            JOptionPane.showMessageDialog(frame,
                "Invalid username or password. Please try again.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Clear the password field on error
            passwordField.setText("");

            JOptionPane.showMessageDialog(frame,
                "An error occurred: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Displays the admin dashboard, which provides access to system management features
     * such as user management and media administration.
     *
     * @param username the admin user's name
     */


    private void showAdminDashboard(String username) {
        // Clear the current panel
        frame.remove(currentPanel);
        
        // Create main panel with border layout
        currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + username + " (Admin)");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Add logout button
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, new Color(231, 76, 60), Color.WHITE);
        logoutButton.addActionListener(e -> showLoginPanel());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(logoutButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create content panel with card layout for different views
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create main dashboard title
        JLabel titleLabel = new JLabel("Admin Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Create buttons panel with grid layout
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 40, 80));
        buttonsPanel.setOpaque(false);
        
        JButton manageUsersBtn = createDashboardButton("Manage Users", "👥");
        manageUsersBtn.addActionListener(e -> showManageUsersPanel());

        
        JButton addMediaBtn = createDashboardButton("Add Media", "➕");
        addMediaBtn.addActionListener(e -> {
            // Show dialog to add new media
            showAddMediaDialog();
        });
        
        JButton viewReportsBtn = createDashboardButton("View Reports", "📊");
        viewReportsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "View Reports functionality coming soon!");
        });
        
        JButton manageLoansBtn = createDashboardButton("Manage Loans", "📚");
        manageLoansBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Manage Loans functionality coming soon!");
        });
        
        JButton systemSettingsBtn = createDashboardButton("System Settings", "⚙️");
        systemSettingsBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "System Settings functionality coming soon!");
        });

        JButton listMediaBtn = createDashboardButton("List All Media", "📖");
        listMediaBtn.addActionListener(e -> showSearchMediaPanel());
        
        // Add buttons to panel
        buttonsPanel.add(manageUsersBtn);
        buttonsPanel.add(addMediaBtn);
        buttonsPanel.add(viewReportsBtn);
        buttonsPanel.add(manageLoansBtn);
        buttonsPanel.add(systemSettingsBtn);
        buttonsPanel.add(listMediaBtn);
        
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        currentPanel.add(headerPanel, BorderLayout.NORTH);
        currentPanel.add(contentPanel, BorderLayout.CENTER);
        
        frame.add(currentPanel);
        frame.revalidate();
        frame.repaint();
    }
    
    private void showAddMediaDialog() {
        JDialog dialog = new JDialog(frame, "Add New Media", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(frame);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Media Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Media Type:"), gbc);
        
        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "CD"});
        formPanel.add(typeCombo, gbc);
        
        // Title
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        formPanel.add(titleField, gbc);
        
        // Author/Artist
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Author/Artist:"), gbc);
        
        gbc.gridx = 1;
        JTextField creatorField = new JTextField(20);
        formPanel.add(creatorField, gbc);
        
        // ISBN (for books)
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel isbnLabel = new JLabel("ISBN:");
        formPanel.add(isbnLabel, gbc);
        
        gbc.gridx = 1;
        JTextField isbnField = new JTextField(20);
        formPanel.add(isbnField, gbc);
        
        // Update fields based on media type selection
        typeCombo.addActionListener(e -> {
            boolean isBook = typeCombo.getSelectedItem().equals("Book");
            isbnLabel.setVisible(isBook);
            isbnField.setVisible(isBook);
            formPanel.revalidate();
            formPanel.repaint();
            dialog.pack();
        });
        
        // Set initial state
        isbnLabel.setVisible(true);
        isbnField.setVisible(true);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        addButton.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String title = titleField.getText().trim();
            String creator = creatorField.getText().trim();
            String isbn = isbnField.getText().trim();
            
            if (title.isEmpty() || creator.isEmpty() || (type.equals("Book") && isbn.isEmpty())) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fill in all required fields.", 
                    "Incomplete Information", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
         // Replace the TODO section in showAddMediaDialog() with this code:
            try {
                if (type.equals("Book")) {
                    // Create a new book with the provided ISBN
                    Book book = new Book(
                        UUID.randomUUID().toString(), // Internal unique ID
                        title,
                        creator, // This is the author for books
                        isbn     // Using the ISBN from the input field
                    );
                    environment.getMediaRepository().save(book);
                } else {
                    // For CDs, we don't need ISBN
                    CD cd = new CD(
                        UUID.randomUUID().toString(), // Internal unique ID
                        title,
                        creator  // This is the artist for CDs
                    );
                    environment.getMediaRepository().save(cd);
                }

                JOptionPane.showMessageDialog(dialog, 
                    "Media added successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

                // Refresh the admin dashboard to show the new item
                if (currentPanel != null) {
                    showAdminDashboard(environment.getAuthService().getCurrentUser().get().getUsername());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error adding media: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // This will help us see any errors in the console
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setVisible(true);
    }
    /**
     * Displays the main dashboard for the currently logged-in user.
     * Shows different features based on whether the user is an admin or a standard user.
     *
     * @param username the name of the logged-in user
     * @param isAdmin whether the logged-in user has admin privileges
     */

    private void showMainDashboard(String username, boolean isAdmin) {
        // Clear the current panel
        frame.remove(currentPanel);

        // If user is admin, show admin dashboard
        if (isAdmin) {
            showAdminDashboard(username);
            return;
        }
        
        // Create main panel with border layout
        currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBackground(Color.WHITE);

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Add welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + username + (isAdmin ? " (Admin)" : " (User)"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Create content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create main dashboard title
        JLabel titleLabel = new JLabel("Library Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Create buttons panel with grid layout
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 40, 80));
        buttonsPanel.setOpaque(false);

        // Create and add buttons with full functionality
        JButton searchBooksBtn = createDashboardButton("Search Media", "🔍");
        searchBooksBtn.addActionListener(e -> showSearchMediaPanel());

        JButton borrowMediaBtn = createDashboardButton("Borrow Media", "📚");
        borrowMediaBtn.addActionListener(e -> showBorrowMediaPanel());

        JButton returnMediaBtn = createDashboardButton("Return Media", "↩️");
        returnMediaBtn.addActionListener(e -> showReturnMediaPanel());
        
        JButton myAccountBtn = createDashboardButton("My Account", "👤");
        myAccountBtn.addActionListener(e -> showMyAccountPanel());

        buttonsPanel.add(searchBooksBtn);
        buttonsPanel.add(borrowMediaBtn);
        buttonsPanel.add(returnMediaBtn);
        buttonsPanel.add(myAccountBtn);

        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(buttonsPanel, BorderLayout.CENTER);

        // Create bottom buttons panel (Contact Us, Pay Fine, Logout)
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomButtonPanel.setBackground(Color.WHITE);
        bottomButtonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));

        // Create and style the bottom buttons
        JButton contactUsBtn = new JButton("Contact Us");
        styleButton(contactUsBtn, new Color(155, 89, 182), Color.WHITE);
        contactUsBtn.addActionListener(e -> showContactUsDialog());
        
        JButton payFineBtn = new JButton("Pay Fine");
        styleButton(payFineBtn, new Color(243, 156, 18), Color.WHITE);
        payFineBtn.addActionListener(e -> showPayFineDialog());
        
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, new Color(231, 76, 60), Color.WHITE);
        logoutButton.addActionListener(e -> showLoginPanel());
        
        // Add buttons to bottom panel
        bottomButtonPanel.add(contactUsBtn);
        bottomButtonPanel.add(payFineBtn);
        bottomButtonPanel.add(logoutButton);

        // Add footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        footerPanel.setBackground(Color.WHITE);
        JLabel footerLabel = new JLabel("© 2025 Library Management System");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(100, 100, 100));
        footerPanel.add(footerLabel);

        // Create a container for the main content and bottom buttons
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(contentPanel, BorderLayout.CENTER);
        mainContentPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        // Add all components to the main panel
        currentPanel.add(headerPanel, BorderLayout.NORTH);
        currentPanel.add(mainContentPanel, BorderLayout.CENTER);
        currentPanel.add(footerPanel, BorderLayout.SOUTH);

        frame.add(currentPanel);
        frame.revalidate();
        frame.repaint();
    }
    private void showMyAccountPanel() {
        try {
            // Get current user
            User currentUser = environment.getAuthService().getCurrentUser()
                .orElseThrow(() -> new LibraryException("User not logged in"));
            
            // Create the main panel
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // User info panel
            JPanel infoPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 15);
            
            // Add user info
            gbc.gridx = 0; gbc.gridy = 0;
            infoPanel.add(new JLabel("<html><b>Username:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(currentUser.getUsername()), gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            infoPanel.add(new JLabel("<html><b>Full Name:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(currentUser.getName()), gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            infoPanel.add(new JLabel("<html><b>Account Type:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(currentUser.isAdmin() ? "Administrator" : "Standard User"), gbc);
            
            gbc.gridx = 0; gbc.gridy = 3;
            infoPanel.add(new JLabel("<html><b>Outstanding Fines:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel("$" + currentUser.getFineBalance()), gbc);  // Changed getFineAmount() to getFineBalance()
            
            // Create borrowed items table
            String[] columnNames = {"Title", "Type", "Borrowed On", "Due Date", "Status"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            // Get user's loans
            List<Loan> userLoans = environment.getLoanRepository().findActiveByUser(currentUser.getId());
            for (Loan loan : userLoans) {
                Optional<Media> mediaOpt = environment.getMediaRepository().findById(loan.getMediaId());
                if (mediaOpt.isPresent()) {
                    Media media = mediaOpt.get();
                    LocalDate today = environment.getDateProvider().today();
                    String status = loan.isOverdue(today) ? "Overdue" : "On Loan";
                    
                    model.addRow(new Object[]{
                        media.getTitle(),
                        media instanceof Book ? "Book" : "CD",
                        loan.getCheckoutDate().toString(),
                        loan.getDueDate().toString(),
                        status
                    });
                }
            }
            
            JTable loansTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(loansTable);
            
            // Add components to main panel
            mainPanel.add(infoPanel, BorderLayout.NORTH);
            mainPanel.add(new JLabel("Borrowed Items:", JLabel.LEFT), BorderLayout.CENTER);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Show in dialog
            JDialog dialog = new JDialog(frame, "My Account", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(mainPanel);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error loading account information: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    /**
     * Displays a searchable list of available media items that the user can borrow.
     * Allows filtering by type and keyword, and processes the borrowing action.
     */
    private void showBorrowMediaPanel() {

        // MAIN PANEL
        JPanel borrowPanel = new JPanel(new BorderLayout(10, 10));
        borrowPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // TITLE
        JLabel titleLabel = new JLabel("Available Media for Borrowing", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // -------------------------------
        // 🔍 SEARCH BAR + TYPE FILTER
        // -------------------------------
        JPanel searchBar = new JPanel(new BorderLayout(5, 5));
        JTextField searchField = new JTextField(20);
        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All", "Books", "CDs"});
        JButton searchButton = new JButton("Search");

        searchBar.add(new JLabel("Search:"), BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightSide.add(typeFilter);
        rightSide.add(searchButton);

        searchBar.add(rightSide, BorderLayout.EAST);

        // -------------------------------
        // TABLE MODEL
        // -------------------------------
        String[] columnNames = {"Title", "Type", "Author/Artist", "Availability", "Action"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Borrow button
            }
        };

        JTable mediaTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(mediaTable);

        // -------------------------------
        // BUTTON RENDERER + EDITOR
        // -------------------------------
        mediaTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                if (column == 4) {
                    JButton button = new JButton("Borrow");
                    button.setOpaque(true);
                    if (isSelected) {
                        button.setBackground(table.getSelectionBackground());
                        button.setForeground(table.getSelectionForeground());
                    } else {
                        button.setBackground(UIManager.getColor("Button.background"));
                        button.setForeground(UIManager.getColor("Button.foreground"));
                    }
                    // Informational tooltip: remaining copies, without changing layout.
                    Map<String, Integer> qtyMap =
                        (Map<String, Integer>) table.getClientProperty("quantityByTitle");
                    String mediaTitle = (String) table.getValueAt(row, 0);
                    int qty = qtyMap != null && qtyMap.get(mediaTitle) != null ? qtyMap.get(mediaTitle) : 0;
                    button.setToolTipText("Available copies: " + qty);
                    return button;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        mediaTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                JButton button = new JButton("Borrow");
                button.addActionListener(e -> {
                    try {
                        // End editing before mutating the table model to avoid index issues.
                        fireEditingStopped();

                        String mediaTitle = (String) model.getValueAt(row, 0);

                        User currentUser = environment.getAuthService().getCurrentUser()
                            .orElseThrow(() -> new LibraryException("User not logged in"));

                        Media media = environment.getMediaRepository().findAll().stream()
                            .filter(m -> m.getTitle().equals(mediaTitle))
                            .findFirst()
                            .orElseThrow(() -> new LibraryException("Media not found"));

                        if (!media.isAvailable()) {
                            throw new LibraryException("This item is not currently available");
                        }

                        String loanId = UUID.randomUUID().toString();
                        LocalDate checkoutDate = LocalDate.now();
                        LocalDate dueDate = checkoutDate.plusDays(28);

                        Loan loan = new Loan(
                            loanId,
                            currentUser.getId(),
                            media.getId(),
                            checkoutDate,
                            dueDate
                        );

                        environment.getLoanRepository().save(loan);
                        media.markUnavailable(); // decreases quantity and updates availability
                        environment.getMediaRepository().save(media); // persist updated quantity
                        currentUser.addLoan(loanId);
                        environment.getAuthService().updateCurrentUser(currentUser);

                        // Refresh the table so that availability is recalculated based on quantity.
                        // This keeps the same UI but ensures items disappear only when no copies remain.
                        ((DefaultTableModel) table.getModel()).setRowCount(0);
                        ((Runnable) table.getClientProperty("loadFilteredMedia")).run();

                        JOptionPane.showMessageDialog(
                            frame,
                            "Successfully borrowed: " + mediaTitle + "\nDue Date: " + dueDate,
                            "Borrow Successful",
                            JOptionPane.INFORMATION_MESSAGE
                        );

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Error borrowing media: " + ex.getMessage(),
                            "Borrow Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
                // Keep tooltip in editor as well (no UI changes).
                Map<String, Integer> qtyMap =
                    (Map<String, Integer>) table.getClientProperty("quantityByTitle");
                String mediaTitle = (String) table.getValueAt(row, 0);
                int qty = qtyMap != null && qtyMap.get(mediaTitle) != null ? qtyMap.get(mediaTitle) : 0;
                button.setToolTipText("Available copies: " + qty);
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                return "Borrow";
            }
        });

        // -------------------------------
        // FILTERED LOADER FUNCTION
        // -------------------------------
        Runnable loadFilteredMedia = () -> {
            model.setRowCount(0);

            String searchTerm = searchField.getText().trim().toLowerCase();
            String typeChoice = (String) typeFilter.getSelectedItem();

            try {
                Collection<Media> allMedia = environment.getMediaRepository().findAll();
                Map<String, Integer> qtyMap = new HashMap<>();

                for (Media media : allMedia) {

                    if (!media.isAvailable()) continue;

                    // TYPE FILTER
                    if (!typeChoice.equals("All")) {
                        if (typeChoice.equals("Books") && media.getType() != MediaType.BOOK) continue;
                        if (typeChoice.equals("CDs") && media.getType() != MediaType.CD) continue;
                    }

                    // SEARCH FILTER
                    String searchable = (
                        media.getTitle() + " " +
                        (media instanceof Book ? ((Book) media).getAuthor() : "") + " " +
                        (media instanceof CD ? ((CD) media).getArtist() : "")
                    ).toLowerCase();

                    if (!searchable.contains(searchTerm)) continue;

                    String creator = "";
                    if (media instanceof Book) creator = ((Book) media).getAuthor();
                    else if (media instanceof CD) creator = ((CD) media).getArtist();

                    qtyMap.put(media.getTitle(), media.getQuantity());

                    String availabilityText = "Available (" + media.getQuantity() + ")";

                    model.addRow(new Object[]{
                        media.getTitle(),
                        media.getType().toString(),
                        creator,
                        availabilityText,
                        "Borrow"
                    });
                }

                // Expose quantities for tooltips without altering the table layout.
                mediaTable.putClientProperty("quantityByTitle", qtyMap);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error loading media: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        };

        // Make the loader accessible from the cell editor without changing the UI structure.
        mediaTable.putClientProperty("loadFilteredMedia", loadFilteredMedia);

        // EVENTS
        searchButton.addActionListener(e -> loadFilteredMedia.run());
        searchField.addActionListener(e -> loadFilteredMedia.run());
        typeFilter.addActionListener(e -> loadFilteredMedia.run());

        borrowPanel.add(titleLabel, BorderLayout.NORTH);
        borrowPanel.add(searchBar, BorderLayout.SOUTH);  
        borrowPanel.add(scrollPane, BorderLayout.CENTER);

        JDialog dialog = new JDialog(frame, "Borrow Media", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(borrowPanel);
        dialog.setSize(750, 520);
        dialog.setLocationRelativeTo(frame);

        loadFilteredMedia.run(); 

        dialog.setVisible(true);
    }

    /**
     * Displays the media search interface, allowing users to look up books and CDs.
     * Supports keyword matching and filtering by media type.
     */

    private void showSearchMediaPanel() {
        // Create the main panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create search components
        JPanel searchBoxPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        JComboBox<String> searchType = new JComboBox<>(new String[]{"All", "Books", "CDs"});
        
        // Create results table
        String[] columnNames = {"Title", "Type", "Author/Artist", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the action column is editable
            }
        };
        JTable resultsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        
        // Method to update the table based on search criteria
        Runnable updateTable = () -> {
            String searchTerm = searchField.getText().trim().toLowerCase();
            String typeFilter = (String) searchType.getSelectedItem();
            
            // Clear existing rows
            model.setRowCount(0);
            
            try {
                List<Media> searchResults;
                if (searchTerm.isEmpty()) {
                    searchResults = new ArrayList<>(environment.getMediaRepository().findAll());
                } else {
                    searchResults = environment.getMediaRepository().search(searchTerm);
                }
                
                for (Media media : searchResults) {
                    // Apply type filter
                    if (!"All".equals(typeFilter)) {
                        if (typeFilter.equals("Books") && media.getType() != MediaType.BOOK) {
                            continue;
                        }
                        if (typeFilter.equals("CDs") && media.getType() != MediaType.CD) {
                            continue;
                        }
                    }
                    
                    String creator = "";
                    if (media instanceof Book) {
                        creator = ((Book) media).getAuthor();
                    } else if (media instanceof CD) {
                        creator = ((CD) media).getArtist();
                    }
                    
                    model.addRow(new Object[]{
                        media.getTitle(),
                        media.getType().toString(),
                        creator,
                        media.isAvailable() ? "Available" : "Borrowed",
                        media.isAvailable() ? "Borrow" : "N/A"
                    });
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error loading media: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        };
        
        searchButton.addActionListener(e -> updateTable.run());
        
        searchField.addActionListener(e -> updateTable.run());
        
        searchType.addActionListener(e -> updateTable.run());
        
        searchBoxPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchBoxPanel.add(searchType, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);
        
        searchPanel.add(searchBoxPanel, BorderLayout.NORTH);
        searchPanel.add(buttonPanel, BorderLayout.SOUTH);
        searchPanel.add(scrollPane, BorderLayout.CENTER);
        
        JDialog dialog = new JDialog(frame, "Search Media", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(searchPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        
        updateTable.run();
        
        dialog.setVisible(true);
    }

	/**
     * Displays a dialog for paying fines
     * @return Always returns null
     */
    private Object showPayFineDialog() {
        try {
            User currentUser = environment.getAuthService().getCurrentUser()
                .orElseThrow(() -> new LibraryException("User not logged in"));
            
            // Get current fine amount
            BigDecimal currentFine = currentUser.getFineAmount();
            
            if (currentFine.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(
                    frame,
                    "You don't have any outstanding fines!",
                    "No Fines",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return null;
            }

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Current Fine Amount:"), gbc);
            
            gbc.gridx = 1;
            JLabel fineLabel = new JLabel("$" + currentFine);
            fineLabel.setFont(fineLabel.getFont().deriveFont(Font.BOLD));
            panel.add(fineLabel, gbc);

            // Payment amount
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Payment Amount ($):"), gbc);
            
            gbc.gridx = 1;
            JFormattedTextField amountField = new JFormattedTextField(NumberFormat.getNumberInstance());
            amountField.setColumns(10);
            amountField.setValue(currentFine); 
            panel.add(amountField, gbc);

         
            int result = JOptionPane.showConfirmDialog(
                frame,
                panel,
                "Pay Fine",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                try {
                    BigDecimal paymentAmount = new BigDecimal(amountField.getText().trim());
                    
                    if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException("Payment amount must be greater than zero");
                    }
                    
                    if (paymentAmount.compareTo(currentFine) > 0) {
                        throw new NumberFormatException("Payment amount cannot exceed the current fine");
                    }
                    
                    
                    BigDecimal newFineAmount = currentFine.subtract(paymentAmount);
                    currentUser.setFineAmount(newFineAmount);
                    
                    JOptionPane.showMessageDialog(
                        frame,
                        "Payment of $" + paymentAmount + " processed successfully!",
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    showMainDashboard(currentUser.getUsername(), currentUser.isAdmin());
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "Invalid payment amount: " + ex.getMessage(),
                        "Invalid Amount",
                        JOptionPane.ERROR_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "Error processing payment: " + ex.getMessage(),
                        "Payment Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        return null;
    }

	/**
     * Displays a contact information dialog to the user
     * @return Always returns null
     */
    private Object showContactUsDialog() {
        String message = "<html><div style='width: 300px;'>" +
            "<h2 style='color: #3498db; margin-top: 0;'>Contact Library Support</h2>" +
            "<p style='margin-bottom: 5px;'><b>Email:</b> support@library.com</p>" +
            "<p style='margin-bottom: 5px;'><b>Phone:</b> (123) 456-7890</p>" +
            "<p style='margin-bottom: 5px;'><b>Address:</b> 123 Library St, Bookville, 12345</p>" +
            "<hr style='margin: 15px 0;'>" +
            "<p><b>Support Hours:</b><br>" +
            "Monday - Friday: 9:00 AM - 6:00 PM<br>" +
            "Saturday: 10:00 AM - 4:00 PM<br>" +
            "Sunday: Closed</p>" +
            "<hr style='margin: 15px 0;'>" +
            "<p>For immediate assistance, please call our support line during business hours.</p>" +
            "</div></html>";

        // Create and customize the JOptionPane
        JLabel iconLabel = new JLabel();
        try {
            // Try to load a custom icon
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/contact-icon.png"));
            if (icon.getImage() != null) {
                // Resize the icon if needed
                Image image = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(image));
            }
        } catch (Exception e) {
            iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        }

        JOptionPane.showMessageDialog(
            frame,                               
            new Object[] {iconLabel, message},     
            "Contact Us",                          
            JOptionPane.INFORMATION_MESSAGE         
        );

        return null;  // Return value not used
    }
    /**
     * Displays the sign-up panel allowing new users to create an account.
     * Validates inputs before submitting registration.
     */

	private void showSignUpPanel() {
        JPanel signUpPanel = new JPanel(new GridBagLayout());
        signUpPanel.setBackground(Color.WHITE);
        signUpPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 30, 0);
        signUpPanel.add(titleLabel, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(10, 0, 5, 0);
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(userLabel, gbc);

        JTextField newUsernameField = new JTextField(20);
        styleTextField(newUsernameField);
        gbc.gridy = 2;
        signUpPanel.add(newUsernameField, gbc);

        // Name
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 5, 0);
        JLabel nameLabel = new JLabel("FULL NAME");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        styleTextField(nameField);
        gbc.gridy = 4;
        signUpPanel.add(nameField, gbc);

        // Password
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 5, 0);
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(passLabel, gbc);

        JPasswordField newPasswordField = new JPasswordField(20);
        stylePasswordField(newPasswordField);
        gbc.gridy = 6;
        signUpPanel.add(newPasswordField, gbc);

        // Confirm Password
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 0, 5, 0);
        JLabel confirmPassLabel = new JLabel("CONFIRM PASSWORD");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmPassLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(confirmPassLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(20);
        stylePasswordField(confirmPasswordField);
        gbc.gridy = 8;
        signUpPanel.add(confirmPasswordField, gbc);

        // Sign Up Button
        gbc.gridy = 9;
        gbc.insets = new Insets(30, 0, 10, 0);
        JButton signUpButton = new JButton("CREATE ACCOUNT");
        styleButton(signUpButton, new Color(46, 204, 113), Color.WHITE);
        signUpButton.addActionListener(e -> {
            String username = newUsernameField.getText().trim();
            String name = nameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Please fill in all fields.",
                    "Incomplete Information",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Passwords do not match.",
                    "Password Mismatch",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try {
                environment.getAuthService().register(username, name, password);
                JOptionPane.showMessageDialog(
                    frame,
                    "Account created successfully! Please log in.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                showLoginPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error creating account: " + ex.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        signUpPanel.add(signUpButton, gbc);

        // Back to Login Button
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 0, 0, 0);
        JButton backButton = new JButton("Back to Login");
        styleButton(backButton, new Color(149, 165, 166), Color.WHITE);
        backButton.addActionListener(e -> showLoginPanel());
        signUpPanel.add(backButton, gbc);

        // Clear the current panel and add the sign-up panel
        frame.remove(currentPanel);
        currentPanel = new JPanel(new BorderLayout());
        currentPanel.add(signUpPanel, BorderLayout.CENTER);
        frame.add(currentPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void showReturnMediaPanel() {
        try {
            // Get current user ID
            String currentUserId = environment.getAuthService().getCurrentUser()
                .orElseThrow(() -> new LibraryException("User not logged in"))
                .getId();

            // Get loan repository and find active loans
            LoanRepository loanRepo = environment.getLoanRepository();
            List<Loan> activeLoans = loanRepo.findActiveByUser(currentUserId);

            if (activeLoans.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "You don't have any items to return.",
                    "No Items to Return",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // Create the main panel
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.setPreferredSize(new Dimension(800, 400));

            // Create table model
            String[] columnNames = {"Title", "Type", "Borrowed On", "Due Date", "Status", "Action"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 5; // Only the action column is editable
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 5) return JButton.class;
                    return String.class;
                }
            };

            // Populate table with loan data
            MediaRepository mediaRepo = environment.getMediaRepository();
            DateProvider dateProvider = environment.getDateProvider();

            for (Loan loan : activeLoans) {
                Optional<Media> mediaOpt = mediaRepo.findById(loan.getMediaId());
                if (!mediaOpt.isPresent()) continue;

                Media media = mediaOpt.get();
                String mediaType = media instanceof Book ? "Book" : "CD";
                String status = loan.isOverdue(dateProvider.today()) ? "Overdue" : "On Time";

                model.addRow(new Object[]{
                    media.getTitle(),
                    mediaType,
                    loan.getCheckoutDate().toString(),
                    loan.getDueDate().toString(),
                    status,
                    "Return"
                });
            }

            // Create table with the model
            JTable table = new JTable(model);
            table.setRowHeight(40);
            table.setFillsViewportHeight(true);

            // Set up the button column
            TableColumn buttonColumn = table.getColumnModel().getColumn(5);
            buttonColumn.setCellRenderer(new ButtonRenderer());
            buttonColumn.setCellEditor(new ButtonEditor(new JCheckBox(), table));

            // Configure column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(200); // Title
            table.getColumnModel().getColumn(1).setPreferredWidth(50);  // Type
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Borrowed
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Due Date
            table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
            table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Action

            // Add table to scroll pane
            JScrollPane scrollPane = new JScrollPane(table);
            JLabel titleLabel = new JLabel("Your Borrowed Items:", JLabel.LEFT);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Show the dialog
            JDialog dialog = new JDialog(frame, "Return Media", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(mainPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                frame,
                "Error loading borrowed items: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Custom cell renderer for the button column
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
 // Custom cell editor for the Return button column
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button;
        private final JTable table;
        private int currentRow = -1;
        private String label;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            this.table = table;

            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            button.addActionListener(e -> {
                fireEditingStopped();  // must call before using row

                if (currentRow < 0) return; // prevent crash

                String mediaTitle = (String) table.getModel().getValueAt(currentRow, 0);

                returnMedia(mediaTitle);

                ((AbstractTableModel) table.getModel()).fireTableDataChanged();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            label = value == null ? "" : value.toString();
            button.setText(label);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

    private class UnregisterButtonEditor extends AbstractCellEditor implements TableCellEditor {
        protected JButton button;
        private JTable table;
        private int currentRow = -1;

        public UnregisterButtonEditor(JCheckBox checkBox, JTable table) {
            this.table = table;

            button = new JButton("Unregister");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            button.addActionListener(e -> {
                // Stop editing first, then safely resolve the stored row index.
                fireEditingStopped();
                if (currentRow < 0) {
                    return; // No active row; avoid AIOOB.
                }
                int modelRow = table.convertRowIndexToModel(currentRow);
                String username = (String) table.getModel().getValueAt(modelRow, 0);

                unregisterUser(username);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            String status = (String) table.getModel().getValueAt(row, 3);
            // Disable for admins and any non-unregisterable status
            button.setEnabled("Can Unregister".equals(status));
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Unregister";
        }
    }

    private class UserDetailsButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;
        private final JTable table;
        private int currentRow = -1;

        public UserDetailsButtonEditor(JCheckBox checkBox, JTable table) {
            this.table = table;
            button = new JButton("Details");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            button.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow < 0) {
                    return;
                }
                int modelRow = table.convertRowIndexToModel(currentRow);
                String username = (String) table.getModel().getValueAt(modelRow, 0);
                showUserDetails(username);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Details";
        }
    }


    private void showManageUsersPanel() {
        try {
            // Create the main panel
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            mainPanel.setPreferredSize(new Dimension(800, 500));

            // Create search panel
            JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
            JTextField searchField = new JTextField(30);
            JButton searchButton = new JButton("Search");
            searchPanel.add(new JLabel("Search by username or name:"), BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);
            searchPanel.add(searchButton, BorderLayout.EAST);

            // Create table model
            String[] columnNames = {"Username", "Name", "Role", "Status", "Details", "Action"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 4 || column == 5; // Details and Action columns
                }
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return (columnIndex == 4 || columnIndex == 5) ? JButton.class : String.class;
                }
            };

            JTable usersTable = new JTable(model);
            usersTable.setRowHeight(40);
            usersTable.setFillsViewportHeight(true);

            TableColumn detailsColumn = usersTable.getColumnModel().getColumn(4);
            detailsColumn.setCellRenderer(new ButtonRenderer());
            detailsColumn.setCellEditor(new UserDetailsButtonEditor(new JCheckBox(), usersTable));

            TableColumn actionColumn = usersTable.getColumnModel().getColumn(5);
            actionColumn.setCellRenderer(new ButtonRenderer());
            actionColumn.setCellEditor(new UnregisterButtonEditor(new JCheckBox(), usersTable));

            JScrollPane scrollPane = new JScrollPane(usersTable);

            // ------------------------- FIXED: define loadUsers ---------------------------
            Runnable loadUsers = () -> {
                model.setRowCount(0); // clear table

                Collection<User> users = environment.getUserRepository().findAll();

                for (User u : users) {
                    String status;
                    if (u.getRole() == UserRole.ADMIN) {
                        status = "Admin";
                    } else if (!environment.getLoanRepository().findActiveByUser(u.getId()).isEmpty()) {
                        status = "Has Active Loans";
                    } else if (u.hasOutstandingFines()) {
                        status = "Has Unpaid Fines";
                    } else {
                        status = "Can Unregister";
                    }

                    model.addRow(new Object[]{
                        u.getUsername(),
                        u.getName(),
                        u.getRole().toString(),
                        status,
                        "Details",
                        "Unregister"
                    });
                }
            };

            // ------------------------- FIXED: define loadAllInto ---------------------------
            Runnable loadAllInto = () -> {
                model.setRowCount(0);
                String term = searchField.getText().trim().toLowerCase();

                Collection<User> users = environment.getUserRepository().findAll();

                for (User u : users) {
                    if (u.getUsername().toLowerCase().contains(term) ||
                        u.getName().toLowerCase().contains(term)) {

                        String status;
                        if (u.getRole() == UserRole.ADMIN) {
                            status = "Admin";
                        } else if (!environment.getLoanRepository().findActiveByUser(u.getId()).isEmpty()) {
                            status = "Has Active Loans";
                        } else if (u.hasOutstandingFines()) {
                            status = "Has Unpaid Fines";
                        } else {
                            status = "Can Unregister";
                        }

                        model.addRow(new Object[]{
                            u.getUsername(),
                            u.getName(),
                            u.getRole().toString(),
                            status,
                            "Details",
                            "Unregister"
                        });
                    }
                }
            };

            // Add action listeners
            searchButton.addActionListener(e -> loadAllInto.run());
            searchField.addActionListener(e -> loadAllInto.run());

            // Add components to panel
            mainPanel.add(searchPanel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Show the dialog
            JDialog dialog = new JDialog(frame, "Manage Users", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(mainPanel);

            loadUsers.run();  // initial load

            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                frame,
                "Error initializing user management: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }


    /**
     * Attempts to unregister a user after confirming the action.
     * Validates that the user has no active loans or unpaid fines.
     *
     * @param username the username of the account to unregister
     */
    private void unregisterUser(String username) {
        try {
            // Confirm unregistration
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to unregister user: " + username + "?",
                "Confirm Unregistration",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Get the user to unregister
            	Optional<User> userOpt = environment.getUserRepository().findByUsername(username);

                    
                if (!userOpt.isPresent()) {
                    throw new LibraryException("User not found: " + username);
                }

                // Let UserService handle the unregistration with all validations
                environment.getUserService().unregister(userOpt.get().getId());
                
                // Show success message
                JOptionPane.showMessageDialog(
                    frame,
                    "User '" + username + "' has been unregistered successfully.",
                    "Unregistration Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Close any open Manage Users dialog before reopening to avoid stacking.
                for (Window window : Window.getWindows()) {
                    if (window.isShowing() && window instanceof JDialog dialog
                        && "Manage Users".equals(dialog.getTitle())) {
                        dialog.dispose();
                        break;
                    }
                }
                // Refresh the user list in a fresh dialog.
                showManageUsersPanel();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error unregistering user: " + ex.getMessage(),
                "Unregistration Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showUserDetails(String username) {
        try {
            Optional<User> userOpt = environment.getUserRepository().findByUsername(username);
            if (userOpt.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame,
                    "User not found: " + username,
                    "User Details",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            User user = userOpt.get();

            JPanel infoPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 15);

            gbc.gridx = 0; gbc.gridy = 0;
            infoPanel.add(new JLabel("<html><b>Username:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(user.getUsername()), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            infoPanel.add(new JLabel("<html><b>Name:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(user.getName()), gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            infoPanel.add(new JLabel("<html><b>Role:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(user.getRole().name()), gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            infoPanel.add(new JLabel("<html><b>Outstanding Fines:</b></html>"), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel("$" + user.getFineBalance()), gbc);

            String[] cols = {"Title", "Type", "Borrowed On", "Due Date", "Status"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);

            List<Loan> loans = environment.getLoanRepository().findActiveByUser(user.getId());
            for (Loan loan : loans) {
                Optional<Media> mediaOpt = environment.getMediaRepository().findById(loan.getMediaId());
                if (mediaOpt.isEmpty()) {
                    continue;
                }
                Media media = mediaOpt.get();
                LocalDate today = environment.getDateProvider().today();
                String status = loan.isOverdue(today) ? "Overdue" : "On Loan";
                model.addRow(new Object[]{
                    media.getTitle(),
                    media.getType().name(),
                    loan.getCheckoutDate().toString(),
                    loan.getDueDate().toString(),
                    status
                });
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel main = new JPanel(new BorderLayout(10, 10));
            main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            main.add(infoPanel, BorderLayout.NORTH);
            main.add(scrollPane, BorderLayout.CENTER);

            JDialog dialog = new JDialog(frame, "User Details: " + username, true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(main);
            dialog.setSize(700, 450);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "Error showing user details: " + ex.getMessage(),
                "User Details",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * Processes the return of a borrowed media item.
     * Updates the loan status, media availability, and applies fines when overdue.
     *
     * @param mediaTitle the title of the media item being returned
     */
    private void returnMedia(String mediaTitle) {
        try {
            // Get current user ID
            String currentUserId = environment.getAuthService().getCurrentUser()
                .orElseThrow(() -> new LibraryException("User not logged in"))
                .getId();

            // Find the loan for this media title
            LoanRepository loanRepo = environment.getLoanRepository();
            List<Loan> userLoans = loanRepo.findActiveByUser(currentUserId);
            MediaRepository mediaRepo = environment.getMediaRepository();

            Optional<Loan> matchingLoan = userLoans.stream()
                .filter(loan -> {
                    Optional<Media> mediaOpt = mediaRepo.findById(loan.getMediaId());
                    return mediaOpt.isPresent() && mediaOpt.get().getTitle().equals(mediaTitle);
                })
                .findFirst();

            if (!matchingLoan.isPresent()) {
                throw new LibraryException("No active loan found for: " + mediaTitle);
            }

            Loan loan = matchingLoan.get();

            // Process return
            BorrowService borrowService = environment.getBorrowService();
            BigDecimal fine = borrowService.returnMedia(loan.getId());

            // Show success message
            String message = "\"" + mediaTitle + "\" returned successfully";
            if (fine.signum() > 0) {
                message += ".\nA fine of $" + fine + " has been added to your account.";
            }

            JOptionPane.showMessageDialog(
                frame,
                message,
                "Return Successful",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Close the current dialog and show an updated one
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window.isShowing() && window instanceof JDialog) {
                    JDialog dialog = (JDialog) window;
                    if ("Return Media".equals(dialog.getTitle())) {
                        dialog.dispose();
                        break;
                    }
                }
            }

            // Show updated list
            showReturnMediaPanel();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                frame,
                "Error returning item: " + ex.getMessage(),
                "Return Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
