import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LoginFrame extends JFrame {
    private ArrayList<User> users;  // Store all users
    private JTextField emailField;  // Email field
    private JPasswordField passwordField;   // Password field
    private boolean statusFlag; // True if logged in, false if not

    public LoginFrame(ArrayList<User> users) {
        this.users = users;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Movie Review Social Network - Login");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(123, 50, 250));

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(123, 50, 250));

        // Title
        JLabel titleLabel = new JLabel("Movie Review Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(200, 50, 400, 40);
        panel.add(titleLabel);

        // Email label and field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setBounds(250, 150, 100, 30);
        panel.add(emailLabel);

        emailField = new JTextField(20);
        emailField.setBounds(350, 150, 200, 30);
        panel.add(emailField);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setBounds(250, 200, 100, 30);
        panel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(350, 200, 200, 30);
        panel.add(passwordField);

        // Login button
        JButton loginButton = new JButton("Log In");
        loginButton.setBounds(350, 250, 100, 30);
        panel.add(loginButton);
        loginButton.addActionListener(e -> attemptLogin());

        add(panel);
        setVisible(true);
    }

    private void attemptLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                statusFlag = true;
                emailField.setEnabled(false);
                passwordField.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Welcome, " + user.getUserName() + "!");
                return;
            }
        }
        statusFlag = false; // Remains not logged in
        JOptionPane.showMessageDialog(this, "Invalid email or password.");
    }
}