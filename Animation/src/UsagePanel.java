import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A single tab of the HelpWindow class
 *
 * @author vishalchandra
 * @version 0.1
 * Date: 8/24/18
 */
public class UsagePanel extends JPanel implements ActionListener
{
    JLabel limbMoveLabel;
    JLabel bodyMoveLabel;

    ImageIcon limbMoveIcon;
    ImageIcon bodyMoveIcon;

    JLabel textLabel;
    JPanel topPanel;
    JButton resetButton;

    /**
     * Constructor
     */
    public UsagePanel()
    {
        //Panel setup
        setSize(1000, 1000);
        setVisible(true);
        setLayout(new BorderLayout());

        //Image setup - GIFs play inside of JLabels
        limbMoveIcon = new ImageIcon("res/help/limbMove.gif");
        limbMoveLabel = new JLabel(limbMoveIcon);

        bodyMoveIcon = new ImageIcon("res/help/bodyMove.gif");
        bodyMoveLabel = new JLabel(bodyMoveIcon);

        textLabel = new JLabel(new ImageIcon("res/help/text.png"));

        //adds
        add(limbMoveLabel, BorderLayout.WEST);
        add(bodyMoveLabel, BorderLayout.EAST);
        add(textLabel, BorderLayout.SOUTH);

        //reset
        topPanel = new JPanel();
        resetButton = new JButton("Reset GIFs");
        resetButton.addActionListener(this);
        topPanel.add(resetButton);
        add(topPanel);
    }

    /**
     * Executes when reset button is pressed
     * @param e Action Event
     */
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("called");
        //https://stackoverflow.com/a/32447700 made this work!
        limbMoveIcon.getImage().flush();
        bodyMoveIcon.getImage().flush();
        limbMoveLabel.setIcon(null);
        bodyMoveLabel.setIcon(null);
        limbMoveLabel.setIcon(limbMoveIcon);
        bodyMoveLabel.setIcon(bodyMoveIcon);
    }
}
