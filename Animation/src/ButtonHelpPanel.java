import javax.swing.*;
import java.awt.*;

/**
 * A single tab in the HelpWindow class;
 *
 * @author vishalchandra
 * @version 0.1
 * Date: 8/24/18
 */
public class ButtonHelpPanel extends JPanel
{
    /**
     * Constructor
     */
    public ButtonHelpPanel()
    {
        setLayout(new BorderLayout());

        add(new JLabel(new ImageIcon("res/help/buttonsHelp.png")));
    }
}
