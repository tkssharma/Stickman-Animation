import javax.swing.*;
import java.awt.*;

/**
 * A temporary, disposable window meant to aid the user in operating the program
 *
 * @author vishalchandra
 * @version 0.1
 * date: 8/24/18
 */
public class HelpWindow extends JFrame
{
    /**
     * Constructor
     */
    public HelpWindow()
    {
        //setup
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 1000);
        setVisible(true);

        //components
        JTabbedPane jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        jTabbedPane.addTab("", new ImageIcon("res/help/info.png"), new UsagePanel());
        jTabbedPane.addTab("", new ImageIcon("res/help/buttons.png"), new ButtonHelpPanel());

        JPanel creditPanel = new JPanel();
        creditPanel.setLayout(new BorderLayout());
        creditPanel.setSize(100, 100);
        creditPanel.add(new JLabel(new ImageIcon("res/help/credit.png")), BorderLayout.CENTER);
        jTabbedPane.addTab("", new ImageIcon("res/help/me.png"), creditPanel);
        add(jTabbedPane);
    }
}