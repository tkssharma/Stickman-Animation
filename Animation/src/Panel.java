import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Main class of the program; Controls all graphics and user interaction
 *
 * @author vishalchandra
 * @version 0.1
 * date: 8/24/18
 */
public class Panel extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
    Timer t = new Timer(100, this);
    long ticks = 0;
    BufferedImage body, leftArm, rightArm, leftLeg, rightLeg;

    //IMAGE INFO------------------------------------------------------
    Point bodyPos = new Point(125, 125);
    //definitions of limbs in relation to body
    Point leftArmOffset  = new Point(-24, 64);
    Point rightArmOffset = new Point(53, 64);
    Point leftLegOffset  = new Point(-9, 149);
    Point rightLegOffset = new Point(37, 149);
    AffineTransform leftArmTransform, rightArmTransform, leftLegTransform, rightLegTransform; //store angles of limbs
    AffineTransform prevLATransform, prevRATransform, prevLLTransform, prevRLTransform; //store angles of ghost limbs
    double LAangle = 0, RAangle = 0, LLangle = 0, RLangle = 0; //temp storage for limb angles while
                                              // affineTransform.getTranslateInstance is called


    Point prevBodyPos = new Point( 125, 125); //location of ghost image
    Dimension imgSize = new Dimension(75, 150);

    //CLICKING AND DRAGGING--------------------------------------------
    Rectangle bodyBounds; //used for detection
    //used for interactions with the limbs
    Ellipse2D leftArmCircle  = new Ellipse2D.Double(94, 234, 10, 10);
    Ellipse2D rightArmCircle = new Ellipse2D.Double(223, 234, 10, 10);
    Ellipse2D leftLegCircle  = new Ellipse2D.Double(110, 353, 10, 10);
    Ellipse2D rightLegCircle = new Ellipse2D.Double(207, 354, 10, 10);
    Point clickOffset = null; //distance between click loc and (0,0) of image
    //used in mouseDragged()
    boolean clickedOnBody = false;
    boolean clickedOnLeftArmCircle  = false;
    boolean clickedOnRightArmCircle = false;
    boolean clickedOnLeftLegCircle  = false;
    boolean clickedOnRightLegCircle = false;

    //ANIMATION--------------------------------------------------------
    ArrayList<Frame> frames = new ArrayList<>();
    boolean playing = false; //used in paintComponent()
    long loopIterations = 0; //used for looping the animation

    //GUI--------------------------------------------------------------
    JPanel buttonPanel;
    JButton saveButton;
    JButton playButton;
    JButton stopButton;
    JButton helpButton;
    JButton clearButton;
    JLabel fpsLabel;
    JComboBox fpsComboBox;

    /**
     * Constructor
     */
    public Panel()
    {
        deserializeFrames();
        if(!frames.isEmpty())
        {
            String message = "A sequence of frames was saved \nin the last session. Would you like to load them in?" +
                    " \nIf you choose not to, a new project will be started.";

            int response = JOptionPane.showConfirmDialog(this, message, "Frames found",
                           JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                           new ImageIcon("res/buttons/save.png"));

            if(response == JOptionPane.NO_OPTION)
            {
                frames.clear();
                try{
                    //PrintWriter just used to clear the file
                    PrintWriter pw = new PrintWriter("frames.txt");
                    pw.close();
                }
                catch(FileNotFoundException fnfe){}
            }
            else
            {
                /*
                 * Stickfigure in editing pane is set to pos of last frame
                 */
                bodyPos.x = frames.get(frames.size() - 1).bodyPos.x;
                bodyPos.y = frames.get(frames.size() - 1).bodyPos.y;

                prevBodyPos.x = bodyPos.x;
                prevBodyPos.y = bodyPos.y;

                int deltaX = bodyPos.x - 125;
                int deltaY = bodyPos.y - 125;

                //moving red circles with the body
                leftArmCircle.setFrame(leftArmCircle.getX() + deltaX, leftArmCircle.getY() + deltaY, 10, 10);
                rightArmCircle.setFrame(rightArmCircle.getX() + deltaX, rightArmCircle.getY() + deltaY, 10, 10);
                leftLegCircle.setFrame(leftLegCircle.getX() + deltaX, leftLegCircle.getY() + deltaY, 10, 10);
                rightLegCircle.setFrame(rightLegCircle.getX() + deltaX, rightLegCircle.getY() + deltaY, 10, 10);
            }
        }

        /*
        GUI SETUP---------------------------------------------------------------------
         */
        setSize(1000, 500);

        helpButton = new JButton(new ImageIcon("res/buttons/help.png"));
        helpButton.setToolTipText("help");
        helpButton.setActionCommand("help");
        helpButton.addActionListener(this);

        clearButton = new JButton(new ImageIcon("res/buttons/clear.png"));
        clearButton.setToolTipText("clear frames");
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(this);

        saveButton = new JButton(new ImageIcon("res/buttons/save.png"));
        saveButton.setToolTipText("save frame");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);

        playButton = new JButton(new ImageIcon("res/buttons/play.png"));
        playButton.setToolTipText("play animation");
        playButton.setActionCommand("play");
        playButton.addActionListener(this);

        stopButton = new JButton(new ImageIcon("res/buttons/stop.png"));
        stopButton.setToolTipText("stop animation");
        stopButton.setActionCommand("stop");
        stopButton.addActionListener(this);

        fpsLabel = new JLabel("FPS:");

        String[] fpsChoices =  {"10", "20", "30"};
        fpsComboBox = new JComboBox(fpsChoices);

        buttonPanel = new JPanel();
        buttonPanel.add(helpButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(fpsLabel);
        buttonPanel.add(fpsComboBox);
        add(buttonPanel, BorderLayout.NORTH);

        //------------------------------------------------------------------------------

        t.setActionCommand("tick");
        t.start();

        bodyBounds = new Rectangle(bodyPos.x, bodyPos.y, imgSize.width, imgSize.height);

        //image setup-------------------------------------------------------------------
        try
        {
            body     = ImageIO.read(new File("res/body/StickmanBody.png"));
            leftArm  = ImageIO.read(new File("res/body/LeftArm.png"));
            rightArm = ImageIO.read(new File("res/body/RightArm.png"));
            leftLeg  = ImageIO.read(new File("res/body/LeftLeg.png"));
            rightLeg = ImageIO.read(new File("res/body/RightLeg.png"));
        }
        catch(IOException ioe){}

        //------------------------------------------------------------------------------


        //AffineTransforms setups (limb rotation)---------------------------------------

        setupAffineTransforms();
        cloneAffineTransforms();

        //------------------------------------------------------------------------------

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /*
        _____________________________________________________________________________________________________
       {                                        DRAWING METHODS                                              }
        ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
     */

    /**
     * Shows graphics
     * @param g Graphics object
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(!playing)
        {
            //Ghost painting of previous frame
            makeTransparent(g2);
            g2.drawImage(body, prevBodyPos.x, prevBodyPos.y, imgSize.width, imgSize.height, null);
            g2.drawImage(leftArm, prevLATransform, null);
            g2.drawImage(rightArm, prevRATransform, null);
            g2.drawImage(leftLeg, prevLLTransform, null);
            g2.drawImage(rightLeg, prevRLTransform, null);

            //painting of current frame
            makeOpaque(g2);

            g2.drawImage(body, bodyPos.x, bodyPos.y, imgSize.width, imgSize.height, null);
            g2.drawImage(leftArm, leftArmTransform, null);
            g2.drawImage(rightArm, rightArmTransform, null);
            g2.drawImage(leftLeg, leftLegTransform, null);
            g2.drawImage(rightLeg, rightLegTransform, null);

            //painting of hold-circles for limb movement
            g2.setColor(Color.RED);
            g2.fillOval((int)leftArmCircle.getX(), (int) leftArmCircle.getY(), 10, 10);
            g2.fillOval((int)rightArmCircle.getX(), (int) rightArmCircle.getY(), 10, 10);
            g2.fillOval((int)leftLegCircle.getX(), (int) leftLegCircle.getY(), 10, 10);
            g2.fillOval((int)rightLegCircle.getX(), (int) rightLegCircle.getY(), 10, 10);

            loopIterations = 0;
        }

        //------------------------------------------------------------------------------------------------

        if(playing && frames.size() > 0)
        {
            System.out.println(fpsComboBox.getSelectedIndex());

            //setting fps
            int fps = fpsComboBox.getSelectedIndex();
            if(fps == 0) t.setDelay(100);
            else if(fps == 1) t.setDelay(50);
            else if(fps == 2) t.setDelay(33);

            Frame f = frames.get((int) loopIterations % frames.size()); //allows the animation to loop
            g2.drawImage(body, f.bodyPos.x, f.bodyPos.y, imgSize.width, imgSize.height, null);
            g2.drawImage(leftArm, f.leftArm, null);
            g2.drawImage(rightArm, f.rightArm, null);
            g2.drawImage(leftLeg, f.leftLeg, null);
            g2.drawImage(rightLeg, f.rightLeg, null);

            loopIterations++;
        }
    }

    /**
     * sets the alpha value of the graphics object to 0.2 (semi-transparent)
     * @param g2 Graphics2D object
     */
    public void makeTransparent(Graphics2D g2)
    {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
    }

    /**
     * sets the alpha value of the graphics object to 1 (fully opaque)
     * @param g2 Graphics2D object
     */
    public void makeOpaque(Graphics2D g2)
    {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    /*
        _____________________________________________________________________________________________________
       {                                        EVENT HANDLING                                               }
        ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
     */

    /**
     * Handles events sent by buttons and the timer.
     * @param e ActionEvent object
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("tick")) repaint(); ticks++;

        if(e.getActionCommand().equals("save"))
        {
            for(int i = 4; i > 0; i--)
                frames.add(new Frame(bodyPos, leftArmTransform, rightArmTransform, leftLegTransform, rightLegTransform));
        }

        if(e.getActionCommand().equals("play")) playing = true;

        if(e.getActionCommand().equals("stop")) playing = false;

        if(e.getActionCommand().equals("clear")) frames.clear();

        if(e.getActionCommand().equals("help")) new HelpWindow();
    }


    /**
     * identifies the part of the body that has been clicked on and stores the info about the current image orientation
     * for later use in showing the ghost image of the body
     * @param me MouseEvent object
     */
    public void mousePressed(MouseEvent me)
    {
        if(bodyBounds.contains(me.getPoint()))
        {
            prevBodyPos.x = bodyPos.x;
            prevBodyPos.y = bodyPos.y;

            cloneAffineTransforms();

            clickOffset = new Point(me.getX() - bodyPos.x, me.getY() - bodyPos.y);
            clickedOnBody = true;
        }

        if(leftArmCircle.contains((double) me.getX(), (double) me.getY()))
        {
            clickedOnLeftArmCircle  = true;
            prevLATransform = (AffineTransform) leftArmTransform.clone();
        }

        if(rightArmCircle.contains((double) me.getX(), (double) me.getY()))
        {
            clickedOnRightArmCircle = true;
            prevRATransform = (AffineTransform) rightArmTransform.clone();
        }

        if(leftLegCircle.contains((double) me.getX(), (double) me.getY()))
        {
            clickedOnLeftLegCircle = true;
            prevLLTransform = (AffineTransform) leftLegTransform.clone();
        }

        if(rightLegCircle.contains((double) me.getX(), (double) me.getY()))
        {
            clickedOnRightLegCircle = true;
            prevRLTransform = (AffineTransform) rightLegTransform.clone();
        }
    }

    /**
     * Resets the variables determining where the user has clicked and updates the points through which the user
     * interacts with the stickfigure
     * @param me
     */
    public void mouseReleased(MouseEvent me)
    {
        bodyBounds.x = bodyPos.x;
        bodyBounds.y = bodyPos.y;
        clickOffset = null;

        //resets location identifiers
        clickedOnBody = false;
        clickedOnLeftArmCircle = false;
        clickedOnRightArmCircle = false;
        clickedOnLeftLegCircle = false;
        clickedOnRightLegCircle = false;
    }

    /**
     * Controls interaction between the user and the stickfigure on screen
     * @param me MouseEvent object
     */
    public void mouseDragged(MouseEvent me)
    {
        if(clickedOnBody)
        {
            int deltaX = me.getX() - clickOffset.x - bodyPos.x;
            int deltaY = me.getY() - clickOffset.y - bodyPos.y;

            bodyPos.x += deltaX;
            bodyPos.y += deltaY;

            //moving red circles with the body
            leftArmCircle.setFrame(leftArmCircle.getX() + deltaX, leftArmCircle.getY() + deltaY, 10, 10);
            rightArmCircle.setFrame(rightArmCircle.getX() + deltaX, rightArmCircle.getY() + deltaY, 10, 10);
            leftLegCircle.setFrame(leftLegCircle.getX() + deltaX, leftLegCircle.getY() + deltaY, 10, 10);
            rightLegCircle.setFrame(rightLegCircle.getX() + deltaX, rightLegCircle.getY() + deltaY, 10, 10);

            setupAffineTransforms(); //moving the body moves the limbs too
        }


        if(clickedOnLeftArmCircle)
            leftArmTransform = rotate(leftArm, leftArmOffset, 45, me.getX(), me.getY(), 0);

        if(clickedOnRightArmCircle)
            rightArmTransform = rotate(rightArm, rightArmOffset,45, me.getX(), me.getY(), 1);

        if(clickedOnLeftLegCircle)
            leftLegTransform = rotate(leftLeg, leftLegOffset, 80, me.getX(), me.getY(), 2);

        if(clickedOnRightLegCircle)
            rightLegTransform = rotate(rightLeg, rightLegOffset,80, me.getX(), me.getY(), 3);
    }

    /*
        _____________________________________________________________________________________________________
       {                                            ROTATION                                                 }
        ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
     */
    /**
     * Called from the mouseDragged function to rotate the limbs of the figure
     * @param limb the image of the limb;
     * @param limbOffset the offset value of the limb used to determine its location in relation to the body
     * @param imgHeight 80 or 45, for legs and arms respectively
     * @param mouseX mouse xLoc
     * @param mouseY mouse yLoc
     * @param limbNum 0 - 3; limb identifier: LA (0), RA(1), LL(2), RL(3)
     * @return an AffineTransform object
     */
    public AffineTransform rotate(BufferedImage limb, Point limbOffset, int imgHeight, int mouseX, int mouseY,
                                  int limbNum)
    {
        Point pos = new Point(bodyPos.x + limbOffset.x, bodyPos.y + limbOffset.y); //position of leg

        //getting angle in radians to rotate by taking arctangent of mouseY/mouseX
        double theta = Math.toRadians(Math.atan2(mouseY - pos.y, mouseX - pos.x));

        AffineTransform limbTransform = AffineTransform.getTranslateInstance(bodyPos.x + limbOffset.x,
                bodyPos.y + limbOffset.y);
        limbTransform.scale((float) 45/limb.getWidth(), (float) imgHeight/limb.getHeight());

        if(limbNum % 2 == 1) limbTransform.rotate(theta * 60);
        if(limbNum % 2 == 0) limbTransform.rotate(theta * 60, limb.getWidth(), 0);

        if(limbNum == 0) LAangle = theta * 60;
        if(limbNum == 1) RAangle = theta * 60;
        if(limbNum == 2) LLangle = theta * 60;
        if(limbNum == 3) RLangle = theta * 60;

        return limbTransform;
    }


    /**
     * Updates the position and rotation of the limb images by storing the data into their respective AffineTransform
     * objects.
     */
    public void setupAffineTransforms()
    {
        //LA
        leftArmTransform = AffineTransform.getTranslateInstance(bodyPos.x + leftArmOffset.x,
                bodyPos.y + leftArmOffset.y);
        //scale must also be redefined since it is reset when getTranslateInstance() is called;
        leftArmTransform.scale((float) 45/leftArm.getWidth(), (float) 45/leftArm.getHeight());
        leftArmTransform.rotate(LAangle, leftArm.getWidth(), 0); //second and third params define anchor point

        //RA
        rightArmTransform = AffineTransform.getTranslateInstance(bodyPos.x + rightArmOffset.x,
                bodyPos.y + rightArmOffset.y);
        rightArmTransform.scale((float) 45/rightArm.getWidth(), (float) 45/rightArm.getHeight());
        rightArmTransform.rotate(RAangle);

        //LL
        leftLegTransform = AffineTransform.getTranslateInstance(bodyPos.x + leftLegOffset.x,
                bodyPos.y + leftLegOffset.y);
        leftLegTransform.scale((float) 46/leftLeg.getWidth(), (float) 80/leftLeg.getHeight());
        leftLegTransform.rotate(LLangle, leftLeg.getWidth(), 0);

        //RL
        rightLegTransform = AffineTransform.getTranslateInstance(bodyPos.x + rightLegOffset.x,
                bodyPos.y + rightLegOffset.y);
        rightLegTransform.scale((float) 46/rightLeg.getWidth(), (float) 80/rightLeg.getHeight());
        rightLegTransform.rotate(RLangle);
    }


    /**
     * Stores the contents of the current AffineTransform objects into objects meant to contain the contents of
     * the previous AffineTransforms. Allows the ghosting effect of the limbs when they are moved.
     */
    public void cloneAffineTransforms()
    {
        //cloning to avoid assigning prev object pointer to same data as original data
        prevLATransform = (AffineTransform) leftArmTransform.clone();

        prevRATransform = (AffineTransform) rightArmTransform.clone();

        prevLLTransform = (AffineTransform) leftLegTransform.clone();

        prevRLTransform = (AffineTransform) rightLegTransform.clone();
    }

    /*
        _____________________________________________________________________________________________________
       {                                            SERIALIZATION                                            }
        ‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾
     */

    /**
     * Writes the Arraylist of Frame objects to a file.
     *
     * Got code from:
     * https://beginnersbook.com/2013/12/how-to-serialize-arraylist-in-java/
     */
    public void serializeFrames()
    {
        if(frames.isEmpty()) return;
        try
        {
            FileOutputStream fos= new FileOutputStream("frames.txt");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(frames);
            oos.close();
            fos.close();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    /**
     * Recontructs the ArrayList of Frame objects from a file.
     *
     * Got code from:
     * https://beginnersbook.com/2013/12/how-to-serialize-arraylist-in-java/
     */
    public void deserializeFrames()
    {
        try
        {
            FileInputStream fis = new FileInputStream("frames.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            frames = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(ClassNotFoundException cnfe){}
        catch(IOException ioe){}
    }


    public void mouseMoved(MouseEvent me){}
    public void mouseEntered(MouseEvent me ){}
    public void mouseExited(MouseEvent me){}
    public void mouseClicked(MouseEvent me){}
}
