import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The external wrapper of the GUI. Serializes info upon close
 *
 * @author vishalchandra
 * @version 0.1
 * date: 8/24/18
 */
public class Window extends JFrame
{
    /**
     * Constructor
     */
    public Window()
    {
        setSize(1000, 500);
        setVisible(true);
        Panel animationPanel = new Panel();
        add(animationPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        //learned how to call method on window close: https://stackoverflow.com/a/16372860
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                animationPanel.serializeFrames();
                dispose();
                System.exit(0);
            }
        });

        /*
        how to pack without conforming window to size of components:
        https://www.reddit.com/r/java/comments/27pk3c/til_an_important_difference_with_framepack_vs/
         */
        validate();
    }
}
