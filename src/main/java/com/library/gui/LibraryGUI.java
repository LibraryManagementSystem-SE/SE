package com.library.gui;
//GUI
import com.library.domain.User;
import com.library.service.LibraryException;
import com.library.system.LibraryEnvironment;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

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
        JButton searchBooksBtn = createDashboardButton("Search Books", "🔍");
        JButton borrowMediaBtn = createDashboardButton("Borrow Media", "📚");
        JButton returnMediaBtn = createDashboardButton("Return Media", "↩️");
        JButton payFineBtn = createDashboardButton("Pay Fine", "💳");
        JButton myProfileBtn = createDashboardButton("My Profile", "👤");
        JButton contactBtn = createDashboardButton("Contact Us", "📞");
        
        // Add action listeners
        searchBooksBtn.addActionListener(e -> showMessage("Opening Book Search"));
        borrowMediaBtn.addActionListener(e -> showMessage("Opening Borrow Media"));
        returnMediaBtn.addActionListener(e -> showMessage("Opening Return Media"));
        payFineBtn.addActionListener(e -> showMessage("Opening Pay Fine"));
        myProfileBtn.addActionListener(e -> showMessage("Opening Profile"));
        contactBtn.addActionListener(e -> showMessage("Opening Contact Page"));
        
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
    
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}