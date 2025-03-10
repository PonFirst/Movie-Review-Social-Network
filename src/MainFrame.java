import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame
{
    public MainFrame()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 750);
        this.setTitle("Movie Review Social Network");
        this.setVisible(true);
        this.getContentPane().setBackground(new Color(123,50,250));
    }
}
