package com.library.gui;
import com.library.domain.User;
import com.library.domain.Book;
import com.library.domain.CD;
import com.library.domain.Media;
import com.library.domain.Loan;
import com.library.service.LibraryException;
import com.library.system.LibraryEnvironment;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;
import java.awt.Font;


public class LibraryGUI {
    private final LibraryEnvironment environment;
    private JFrame frame;
    private JPanel currentPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LibraryGUI(LibraryEnvironment environment) {
        this.environment = environment;
        initialize();
    }
    
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
        
        // Show login panel by default
        showLoginPanel();
        
        // Make the window visible
        frame.setVisible(true);
    }
    
    private void showLoginPanel() {
        // Clear the current panel if it exists
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
        JLabel titleLabel = new JLabel("Library Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Create buttons panel with grid layout
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 40, 80));
        buttonsPanel.setOpaque(false);
        
        // Create and add buttons
        JButton searchBooksBtn = createDashboardButton("Search Media", "🔍");
        JButton borrowMediaBtn = createDashboardButton("Borrow Media", "📚");
        JButton returnMediaBtn = createDashboardButton("Return Media", "↩️");
        JButton payFineBtn = createDashboardButton("Pay Fine", "💳");
        JButton myProfileBtn = createDashboardButton("My Profile", "👤");
        JButton contactBtn = createDashboardButton("Contact Us", "📞");
        
        // Add action listeners
        searchBooksBtn.addActionListener(e -> {
            try {
                // Create search dialog
                JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
                searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Search field
                JTextField searchField = new JTextField(30);
                JButton searchButton = new JButton("Search");
                
                // Results area
                String[] columnNames = {"ID", "Type", "Title", "Author/Artist", "Available"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Make table non-editable
                    }
                };
                JTable resultsTable = new JTable(model);
                resultsTable.setFillsViewportHeight(true);
                JScrollPane scrollPane = new JScrollPane(resultsTable);
                
                // Add components to search panel
                JPanel searchBarPanel = new JPanel(new BorderLayout(10, 10));
                searchBarPanel.add(new JLabel("Search (title/author/ISBN):"), BorderLayout.WEST);
                searchBarPanel.add(searchField, BorderLayout.CENTER);
                searchBarPanel.add(searchButton, BorderLayout.EAST);
                
                searchPanel.add(searchBarPanel, BorderLayout.NORTH);
                searchPanel.add(scrollPane, BorderLayout.CENTER);
                
                // Search button action
                ActionListener searchAction = evt -> {
                    String query = searchField.getText().trim();
                    try {
                        // Clear previous results
                        model.setRowCount(0);
                        
                        // Search for media
                        List<Media> results = environment.getCatalogService().search(query);
                        
                        if (results.isEmpty()) {
                            JOptionPane.showMessageDialog(
                                frame,
                                "No matching media found.",
                                "No Results",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            return;
                        }
                        
                        // Add results to table
                        for (Media media : results) {
                            if (media instanceof Book) {
                                Book book = (Book) media;
                                model.addRow(new Object[]{
                                    book.getId(),
                                    "Book",
                                    book.getTitle(),
                                    book.getAuthor(),
                                    book.isAvailable() ? "Yes" : "No"
                                });
                            } else if (media instanceof CD) {
                                CD cd = (CD) media;
                                model.addRow(new Object[]{
                                    cd.getId(),
                                    "CD",
                                    cd.getTitle(),
                                    cd.getArtist(),
                                    cd.isAvailable() ? "Yes" : "No"
                                });
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Error searching media: " + ex.getMessage(),
                            "Search Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                };
                
                // Add action listeners
                searchButton.addActionListener(searchAction);
                searchField.addActionListener(searchAction);
                
                // Show search dialog
                JOptionPane.showOptionDialog(
                    frame,
                    searchPanel,
                    "Search Media",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{},
                    null
                );
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error initializing search: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace(); // Print stack trace for debugging
            }
        });
        borrowMediaBtn.addActionListener(e -> {
            try {
                // Create search dialog
                JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
                searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                // Search field
                JTextField searchField = new JTextField(30);
                JButton searchButton = new JButton("Search");
                
                // Results area
                String[] columnNames = {"ID", "Type", "Title", "Author/Artist", "Available"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Make table non-editable
                    }
                };
                JTable resultsTable = new JTable(model);
                resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                resultsTable.setFillsViewportHeight(true);
                JScrollPane scrollPane = new JScrollPane(resultsTable);
                
                // Create search bar panel
                JPanel searchBarPanel = new JPanel(new BorderLayout(10, 10));
                searchBarPanel.add(new JLabel("Search media to borrow:"), BorderLayout.WEST);
                searchBarPanel.add(searchField, BorderLayout.CENTER);
                searchBarPanel.add(searchButton, BorderLayout.EAST);
                
                // Create borrow button and its panel
                JButton borrowButton = new JButton("Borrow Selected");
                borrowButton.setEnabled(false);
                
                // Create button wrapper with proper alignment
                JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
                buttonWrapper.setOpaque(false);
                buttonWrapper.add(borrowButton);
                
                // Create main button panel
                JPanel buttonPanel1 = new JPanel(new java.awt.BorderLayout());
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                buttonPanel.setOpaque(false);
                buttonPanel.add(buttonWrapper, java.awt.BorderLayout.EAST);
                
                // Create bottom panel with border
                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
                bottomPanel.setBackground(Color.WHITE);
                bottomPanel.add(buttonPanel, BorderLayout.EAST);
                
                // Add all components to search panel
                searchPanel.add(searchBarPanel, BorderLayout.NORTH);
                searchPanel.add(scrollPane, BorderLayout.CENTER);
                searchPanel.add(bottomPanel, BorderLayout.SOUTH);
                
                // Add selection listener after button is created
                resultsTable.getSelectionModel().addListSelectionListener(event -> {
                    if (!event.getValueIsAdjusting()) {
                        borrowButton.setEnabled(resultsTable.getSelectedRow() != -1);
                    }
                });
                
                // Search button action
                ActionListener searchAction = evt -> {
                    String query = searchField.getText().trim();
                    try {
                        // Clear previous results
                        model.setRowCount(0);
                        
                        // Search for media
                        List<Media> results = environment.getCatalogService().search(query);
                        
                        if (results.isEmpty()) {
                            JOptionPane.showMessageDialog(
                                frame,
                                "No matching media found.",
                                "No Results",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            return;
                        }
                        
                        // Add results to table
                        for (Media media : results) {
                            if (media instanceof Book) {
                                Book book = (Book) media;
                                model.addRow(new Object[]{
                                    book.getId(),
                                    "Book",
                                    book.getTitle(),
                                    book.getAuthor(),
                                    book.isAvailable() ? "Yes" : "No"
                                });
                            } else if (media instanceof CD) {
                                CD cd = (CD) media;
                                model.addRow(new Object[]{
                                    cd.getId(),
                                    "CD",
                                    cd.getTitle(),
                                    cd.getArtist(),
                                    cd.isAvailable() ? "Yes" : "No"
                                });
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Error searching media: " + ex.getMessage(),
                            "Search Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                };
                
                // Borrow button action
                borrowButton.addActionListener(borrowEvt -> {
                    int selectedRow = resultsTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String mediaId = (String) model.getValueAt(selectedRow, 0);
                        String mediaType = (String) model.getValueAt(selectedRow, 1);
                        String mediaTitle = (String) model.getValueAt(selectedRow, 2);
                        
                        try {
                            // Get current user
                            User currentUser = environment.getAuthService().getCurrentUser()
                                .orElseThrow(() -> new LibraryException("User not logged in"));
                            
                            // Borrow the media
                            Loan loan = environment.getBorrowService().borrow(currentUser.getId(), mediaId);
                            
                            // Show success message with due date
                            String message = String.format(
                                "Successfully borrowed %s: %s\nDue Date: %s",
                                mediaType, mediaTitle, loan.getDueDate()
                            );
                            
                            JOptionPane.showMessageDialog(
                                frame,
                                message,
                                "Borrow Successful",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            // Refresh the search results
                            searchAction.actionPerformed(null);
                            
                        } catch (LibraryException ex) {
                            JOptionPane.showMessageDialog(
                                frame,
                                "Cannot borrow media: " + ex.getMessage(),
                                "Borrow Failed",
                                JOptionPane.ERROR_MESSAGE
                            );
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                frame,
                                "Error borrowing media: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                });
                
                // Add action listeners
                searchButton.addActionListener(searchAction);
                searchField.addActionListener(searchAction);
                
                // Show search dialog
                JOptionPane.showOptionDialog(
                    frame,
                    searchPanel,
                    "Borrow Media",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[]{},
                    null
                );
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error initializing borrow media: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        });
        returnMediaBtn.addActionListener(e -> showMessage("Opening Return Media"));
        payFineBtn.addActionListener(e -> handlePayFine());
        myProfileBtn.addActionListener(e -> showMessage("Opening Profile"));
        contactBtn.addActionListener(e -> {
            // Create a panel to hold contact information
            JPanel contactPanel = new JPanel(new GridBagLayout());
            contactPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Add contact information with icons
            JLabel titleLabel1 = new JLabel("Contact Information");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 15, 0);
            contactPanel.add(titleLabel, gbc);
            
            // Reset insets
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Email
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            contactPanel.add(new JLabel("📧"), gbc);
            
            gbc.gridx = 1;
            contactPanel.add(new JLabel("Email: support@librarysystem.com"), gbc);
            
            // Phone
            gbc.gridy++;
            gbc.gridx = 0;
            contactPanel.add(new JLabel("📞"), gbc);
            
            gbc.gridx = 1;
            contactPanel.add(new JLabel("Phone: +1 (555) 123-4567"), gbc);
            
            // Address
            gbc.gridy++;
            gbc.gridx = 0;
            contactPanel.add(new JLabel("🏢"), gbc);
            
            gbc.gridx = 1;
            contactPanel.add(new JLabel("<html>123 Library Lane<br>Bookville, BV 12345</html>"), gbc);
            
            // Hours
            gbc.gridy++;
            gbc.gridx = 0;
            contactPanel.add(new JLabel("🕒"), gbc);
            
            gbc.gridx = 1;
            contactPanel.add(new JLabel("<html>Hours:<br>Mon-Fri: 9:00 AM - 8:00 PM<br>Sat-Sun: 10:00 AM - 6:00 PM</html>"), gbc);
            
            // Show the contact information in a dialog
            JOptionPane.showMessageDialog(
                frame,
                contactPanel,
                "Contact Us",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        // Add buttons to panel
        buttonsPanel.add(searchBooksBtn);
        buttonsPanel.add(borrowMediaBtn);
        buttonsPanel.add(returnMediaBtn);
        buttonsPanel.add(payFineBtn);
        buttonsPanel.add(myProfileBtn);
        buttonsPanel.add(contactBtn);
        
        // Add components to content panel
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        // Add header and content to main panel
        currentPanel.add(headerPanel, BorderLayout.NORTH);
        currentPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        frame.add(currentPanel);
        
        // Refresh the frame
        frame.revalidate();
        frame.repaint();
    }
    
    private void showAdminDashboard(String username) {
        // Clear the current panel
        frame.remove(currentPanel);
        
        // Create main panel with border layout
        currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(128, 0, 128)); // Purple color for admin
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add welcome message
        JLabel welcomeLabel = new JLabel("Admin Panel - Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Add logout button
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, new Color(178, 34, 34), Color.WHITE);
        logoutButton.addActionListener(e -> showLoginPanel());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(logoutButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Create content panel with card layout for different views
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create main dashboard title
        JLabel titleLabel = new JLabel("System Admin Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(75, 0, 130));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Create buttons panel with grid layout
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 40, 80));
        buttonsPanel.setOpaque(false);
        
        // Create and add admin buttons
        JButton addBookBtn = createDashboardButton("Add Book", "📚");
        JButton addCDBtn = createDashboardButton("Add CD", "💿");
        JButton sendRemindersBtn = createDashboardButton("Send Reminders", "✉️");
        JButton overdueReportBtn = createDashboardButton("Show Overdue Report", "📊");
        JButton listUsersBtn = createDashboardButton("List All Users", "👥");
        JButton unregisterUserBtn = createDashboardButton("Unregister User", "❌");
        
        // Add action listeners for admin buttons
        addBookBtn.addActionListener(e -> showAddBookForm(username));
        addCDBtn.addActionListener(e -> showAddCDForm(username));
        sendRemindersBtn.addActionListener(e -> {
            try {
                // Send reminders to all users with overdue books
                List<User> notifiedUsers = environment.getReminderService().sendDailyReminders();
                
                // Get the sent messages from the email notifier
                List<String> sentMessages = environment.getEmailNotifier().getSentMessages();
                
                // Build the result message
                StringBuilder message = new StringBuilder();
                message.append("Reminders sent to ").append(notifiedUsers.size()).append(" user(s).\n\n");
                
                if (!sentMessages.isEmpty()) {
                    message.append("Messages sent:\n");
                    for (String msg : sentMessages) {
                        message.append("- ").append(msg).append("\n");
                    }
                } else {
                    message.append("No overdue books found to send reminders for.");
                }
                
                // Show the results
                JOptionPane.showMessageDialog(
                    frame,
                    message.toString(),
                    "Reminders Sent",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                // Clear the sent messages for the next time
                environment.getEmailNotifier().clear();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error sending reminders: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        overdueReportBtn.addActionListener(e -> showMessage("Generating Overdue Report..."));
        listUsersBtn.addActionListener(e -> {
            try {
                // Get all users
                java.util.Collection<User> users = environment.getUserService().listAllUsers();
                
                if (users.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "No users found in the system.",
                        "No Users",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }

                // Create a formatted string with user details
                StringBuilder userList = new StringBuilder("<html><body style='width: 300px;'>");
                userList.append("<h3>All Registered Users</h3>");
                userList.append("<table border='1' cellpadding='5' style='border-collapse: collapse; width: 100%;'>");
                userList.append("<tr><th>Name</th><th>ID</th><th>Role</th></tr>");
                
                for (User user : users) {
                    userList.append(String.format(
                        "<tr><td>%s</td><td>%s</td><td>%s</td></tr>",
                        user.getName(),
                        user.getId(),
                        user.getRole()
                    ));
                }
                
                userList.append("</table></body></html>");
                
                // Create a scrollable panel for the user list
                JTextPane textPane = new JTextPane();
                textPane.setContentType("text/html");
                textPane.setText(userList.toString());
                textPane.setEditable(false);
                textPane.setCaretPosition(0);
                
                JScrollPane scrollPane = new JScrollPane(textPane);
                scrollPane.setPreferredSize(new Dimension(500, 300));
                
                // Show the user list in a dialog
                JOptionPane.showMessageDialog(
                    frame,
                    scrollPane,
                    "All Registered Users",
                    JOptionPane.PLAIN_MESSAGE
                );
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error retrieving user list: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        unregisterUserBtn.addActionListener(e -> {
            try {
                // Get the list of users
                java.util.Collection<User> users = environment.getUserService().listAllUsers();
                
                // Create an array of user display strings for the dropdown
                String[] userOptions = users.stream()
                    .map(user -> String.format("%s (ID: %s, Role: %s)", 
                        user.getName(), user.getId(), user.getRole()))
                    .toArray(String[]::new);
                
                if (userOptions.length == 0) {
                    JOptionPane.showMessageDialog(
                        frame,
                        "No users found in the system.",
                        "No Users",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    return;
                }
                
                // Show user selection dialog
                String selectedUser = (String) JOptionPane.showInputDialog(
                    frame,
                    "Select a user to unregister:",
                    "Unregister User",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userOptions,
                    userOptions[0]
                );
                
                if (selectedUser == null) {
                    return; // User cancelled
                }
                
                // Extract user ID from the selection
                String userId = selectedUser.substring(
                    selectedUser.indexOf("ID:") + 4,
                    selectedUser.indexOf(", Role:")
                ).trim();
                
                // Confirm unregistration
                int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to unregister this user? This action cannot be undone.",
                    "Confirm Unregistration",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        environment.getUserService().unregister(userId);
                        JOptionPane.showMessageDialog(
                            frame,
                            "User successfully unregistered.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Error unregistering user: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "An error occurred: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        // Add buttons to panel
        buttonsPanel.add(addBookBtn);
        buttonsPanel.add(addCDBtn);
        buttonsPanel.add(sendRemindersBtn);
        buttonsPanel.add(overdueReportBtn);
        buttonsPanel.add(listUsersBtn);
        buttonsPanel.add(unregisterUserBtn);
        
        // Add components to content panel
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        // Add header and content to main panel
        currentPanel.add(headerPanel, BorderLayout.NORTH);
        currentPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        frame.add(currentPanel);
        
        // Add keyboard shortcut for logout (Ctrl+L)
        Action logoutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoginPanel();
            }
        };
        
        // Bind Ctrl+L to logout
        KeyStroke logoutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
        currentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(logoutKeyStroke, "logout");
        currentPanel.getActionMap().put("logout", logoutAction);
        
        // Refresh the frame
        frame.revalidate();
        frame.repaint();
    }
    
    private void showAddCDForm(String username) {
        // Create the main panel with border layout
        JPanel addCDPanel = new JPanel(new BorderLayout());
        addCDPanel.setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204)); // Blue color for CD form
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add back button to header
        JButton backButton = new JButton("Back to Admin Dashboard");
        styleButton(backButton, new Color(0, 102, 204), Color.WHITE);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> showAdminDashboard(username));
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        headerPanel.add(backButtonPanel, BorderLayout.WEST);
        
        // Add title
        JLabel titleLabel = new JLabel("Add New CD", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 5, 10);
        JLabel titleLabel1 = new JLabel("TITLE");
        titleLabel1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel1.setForeground(new Color(52, 73, 94));
        formPanel.add(titleLabel1, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField titleField = new JTextField(20);
        styleTextField(titleField);
        formPanel.add(titleField, gbc);
        
        // Artist
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 5, 10);
        JLabel artistLabel = new JLabel("ARTIST");
        artistLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        artistLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(artistLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField artistField = new JTextField(20);
        styleTextField(artistField);
        formPanel.add(artistField, gbc);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Cancel Button
        JButton cancelButton = new JButton("CANCEL");
        styleButton(cancelButton, new Color(149, 165, 166), Color.WHITE);
        cancelButton.addActionListener(e -> showAdminDashboard(username));
        buttonsPanel.add(cancelButton);
        
        // Add CD Button
        JButton addCDButton = new JButton("ADD CD");
        styleButton(addCDButton, new Color(46, 204, 113), Color.WHITE);
        addCDButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String artist = artistField.getText().trim();
            
            // Validate inputs
            if (title.isEmpty() || artist.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please fill in all fields.", 
                    "Incomplete Information", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Create and save the new CD using CatalogService
                environment.getCatalogService().addCd(title, artist);
                
                // Show success message
                JOptionPane.showMessageDialog(frame, 
                    "CD added successfully!\nTitle: " + title + "\nArtist: " + artist, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                titleField.setText("");
                artistField.setText("");
                
                // Return to admin dashboard
                showAdminDashboard(username);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error adding CD: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonsPanel.add(addCDButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(buttonsPanel, gbc);
        
        // Add components to main panel
        addCDPanel.add(headerPanel, BorderLayout.NORTH);
        addCDPanel.add(formPanel, BorderLayout.CENTER);
        
        // Update the current panel
        frame.remove(currentPanel);
        currentPanel = addCDPanel;
        frame.add(currentPanel);
        frame.revalidate();
        frame.repaint();
    }
    
    private void showAddBookForm(String username) {
        // Create the main panel with border layout
        JPanel addBookPanel = new JPanel(new BorderLayout());
        addBookPanel.setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(128, 0, 128));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add back button to header
        JButton backButton = new JButton("Back to Admin Dashboard");
        styleButton(backButton, new Color(128, 0, 128), Color.WHITE);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> showAdminDashboard(username));
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        headerPanel.add(backButtonPanel, BorderLayout.WEST);
        
        // Add title
        JLabel titleLabel = new JLabel("Add New Book", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 5, 10);
        JLabel titleLabel1 = new JLabel("TITLE");
        titleLabel1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel1.setForeground(new Color(52, 73, 94));
        formPanel.add(titleLabel1, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        JTextField titleField = new JTextField(20);
        styleTextField(titleField);
        formPanel.add(titleField, gbc);
        
        // Author
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 5, 10);
        JLabel authorLabel = new JLabel("AUTHOR");
        authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        authorLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(authorLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField authorField = new JTextField(20);
        styleTextField(authorField);
        formPanel.add(authorField, gbc);
        
        // ISBN
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel isbnLabel = new JLabel("ISBN");
        isbnLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        isbnLabel.setForeground(new Color(52, 73, 94));
        formPanel.add(isbnLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JTextField isbnField = new JTextField(20);
        styleTextField(isbnField);
        formPanel.add(isbnField, gbc);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Cancel Button
        JButton cancelButton = new JButton("CANCEL");
        styleButton(cancelButton, new Color(149, 165, 166), Color.WHITE);
        cancelButton.addActionListener(e -> showAdminDashboard(username));
        buttonsPanel.add(cancelButton);
        
        // Add Book Button
        JButton addBookButton = new JButton("ADD BOOK");
        styleButton(addBookButton, new Color(46, 204, 113), Color.WHITE);
        addBookButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            
            // Validate inputs
            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please fill in all fields.", 
                    "Incomplete Information", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                // Generate a unique ID for the new book
                String id = "BK" + System.currentTimeMillis();
                
                // Create and save the new book using CatalogService
                Book newBook = environment.getCatalogService().addBook(title, author, isbn);
                
                // Show success message
                JOptionPane.showMessageDialog(frame, 
                    "Book added successfully!\nID: " + id + "\nTitle: " + title, 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                titleField.setText("");
                authorField.setText("");
                isbnField.setText("");
                
                // Return to admin dashboard
                showAdminDashboard(username);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error adding book: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonsPanel.add(addBookButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        formPanel.add(buttonsPanel, gbc);
        
        // Add components to main panel
        addBookPanel.add(headerPanel, BorderLayout.NORTH);
        addBookPanel.add(formPanel, BorderLayout.CENTER);
        
        // Update the current panel
        frame.remove(currentPanel);
        currentPanel = addBookPanel;
        frame.add(currentPanel);
        frame.revalidate();
        frame.repaint();
    }
    
    private JButton createDashboardButton(String text, String emoji) {
        JButton button = new JButton("<html><div style='text-align:center;'>" + 
            "<div style='font-size:24px;'>" + emoji + "</div>" +
            "<div>" + text + "</div></html>");
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(240, 240, 245));
        button.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 120));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 245));
            }
        });
        return button;
    }
    
    private void showSignUpPanel() {
        // Clear the current panel
        frame.remove(currentPanel);
        
        // Create main panel with border layout
        currentPanel = new JPanel(new BorderLayout());
        currentPanel.setBackground(Color.WHITE);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add back button to header
        JButton backButton = new JButton("Back to Login");
        styleButton(backButton, new Color(52, 152, 219), Color.WHITE);
        backButton.setBorderPainted(false);
        backButton.addActionListener(e -> showLoginPanel());
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        headerPanel.add(backButtonPanel, BorderLayout.WEST);
        
        // Add title
        JLabel titleLabel = new JLabel("Create New Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Create the sign up form panel
        JPanel signUpFormPanel = createSignUpFormPanel();
        
        // Add header and form to main panel
        currentPanel.add(headerPanel, BorderLayout.NORTH);
        currentPanel.add(signUpFormPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        frame.add(currentPanel);
        
        // Refresh the frame
        frame.revalidate();
        frame.repaint();
    }
    
    private JPanel createSignUpFormPanel() {
        JPanel signUpPanel = new JPanel(new GridBagLayout());
        signUpPanel.setBackground(Color.WHITE);
        signUpPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        signUpPanel.add(titleLabel, gbc);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 5, 10);
        JLabel nameLabel = new JLabel("FULL NAME");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(nameLabel, gbc);
        
        gbc.gridy = 2;
        JTextField nameField = new JTextField(20);
        styleTextField(nameField);
        signUpPanel.add(nameField, gbc);
        
        // Username
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 0, 5, 10);
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(userLabel, gbc);
        
        gbc.gridy = 4;
        JTextField newUsernameField = new JTextField(20);
        styleTextField(newUsernameField);
        signUpPanel.add(newUsernameField, gbc);
        
        // Password
        gbc.gridy = 5;
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(passLabel, gbc);
        
        gbc.gridy = 6;
        JPasswordField newPasswordField = new JPasswordField(20);
        stylePasswordField(newPasswordField);
        signUpPanel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridy = 7;
        JLabel confirmPassLabel = new JLabel("CONFIRM PASSWORD");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmPassLabel.setForeground(new Color(52, 73, 94));
        signUpPanel.add(confirmPassLabel, gbc);
        
        gbc.gridy = 8;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        stylePasswordField(confirmPasswordField);
        signUpPanel.add(confirmPasswordField, gbc);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Back Button
        JButton backButton = new JButton("BACK");
        styleButton(backButton, new Color(149, 165, 166), Color.WHITE);
        backButton.addActionListener(e -> showLoginPanel());
        buttonsPanel.add(backButton);
        
        // Create Account Button
        JButton createAccountButton = new JButton("CREATE ACCOUNT");
        styleButton(createAccountButton, new Color(46, 204, 113), Color.WHITE);
        createAccountButton.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String username = newUsernameField.getText().trim();
            String password = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate inputs
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(frame, 
                    "Please fill in all fields.", 
                    "Incomplete Information", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, 
                    "Passwords do not match.", 
                    "Password Mismatch", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Create a new member user
                environment.getUserService().registerMember(username, fullName, password);
                
                // Clear fields
                nameField.setText("");
                newUsernameField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                
                // Show success message and return to login
                JOptionPane.showMessageDialog(frame, 
                    "Account created successfully! Please log in with your new credentials.", 
                    "Account Created", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                showLoginPanel();
                
            } catch (LibraryException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error creating account: " + ex.getMessage(), 
                    "Registration Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonsPanel.add(createAccountButton);
        
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        signUpPanel.add(buttonsPanel, gbc);
        
        return signUpPanel;
    }
    
    private void handlePayFine() {
        try {
            // Get current user
            User currentUser = environment.getAuthService().getCurrentUser()
                .orElseThrow(() -> new LibraryException("You must be logged in to pay fines"));
            
            // Get current fine balance
            BigDecimal currentBalance = currentUser.getFineBalance();
            
            if (currentBalance.signum() <= 0) {
                JOptionPane.showMessageDialog(
                    frame,
                    "You don't have any outstanding fines to pay.",
                    "No Outstanding Fines",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            
            // Create input dialog for payment amount
            String amountStr = JOptionPane.showInputDialog(
                frame,
                String.format("Your current fine balance is: $%.2f%nEnter amount to pay:", currentBalance),
                "Pay Fine",
                JOptionPane.PLAIN_MESSAGE
            );
            
            // If user cancelled the dialog
            if (amountStr == null) {
                return;
            }
            
            try {
                // Parse the payment amount
                BigDecimal paymentAmount = new BigDecimal(amountStr.trim());
                
                // Validate the payment amount
                if (paymentAmount.signum() <= 0) {
                    throw new NumberFormatException("Payment amount must be positive");
                }
                
                if (paymentAmount.compareTo(currentBalance) > 0) {
                    throw new NumberFormatException(
                        String.format("Payment cannot exceed current balance of $%.2f", currentBalance)
                    );
                }
                
                // Process the payment
                BigDecimal newBalance = environment.getFineService().payFine(
                    currentUser.getId(),
                    paymentAmount
                );
                
                // Show success message
                String message;
                if (newBalance.signum() > 0) {
                    message = String.format(
                        "Payment of $%.2f processed successfully.%nRemaining balance: $%.2f",
                        paymentAmount, newBalance
                    );
                } else {
                    message = String.format(
                        "Payment of $%.2f processed successfully.%nYour fine balance is now cleared!",
                        paymentAmount
                    );
                }
                
                JOptionPane.showMessageDialog(
                    frame,
                    message,
                    "Payment Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Invalid amount: " + ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (LibraryException ex) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Error processing payment: " + ex.getMessage(),
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                frame,
                "An error occurred: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}