import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * Data structure which holds info about each frame
 *
 * @author vishalchandra
 * @version 0.1
 * date 8/24/18
 */
public class Frame implements Serializable
{
    AffineTransform leftArm, rightArm, leftLeg, rightLeg;
    Point bodyPos;

    public Frame(Point pos, AffineTransform LA, AffineTransform RA, AffineTransform LL, AffineTransform RL)
    {
        bodyPos = new Point(pos.x, pos.y);
        leftArm  = (AffineTransform) LA.clone();
        rightArm = (AffineTransform) RA.clone();
        leftLeg  = (AffineTransform) LL.clone();
        rightLeg = (AffineTransform) RL.clone();
    }
}
