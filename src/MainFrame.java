import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {
    private ArrayList<User> users;

    public MainFrame(ArrayList<User> users) {
        this.users = users;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Movie Review Social Network");
        getContentPane().setBackground(new Color(123, 50, 250));
        setSize(800, 600);

        JLabel label = new JLabel("Movie Review Social Network", SwingConstants.CENTER);
        label.setFont(new Font("Serif", Font.BOLD, 36));
        label.setForeground(Color.WHITE);
        add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(123, 50, 250));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Serif", Font.PLAIN, 24));
        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame(users);
        });
        buttonPanel.add(loginButton);

        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}