package kp.rollingcube.ce.utils;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class UIUtils
{
    private static Image LOGO;
    
    
    public void useSystemLookAndFeel()
    {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); }
        catch(UnsupportedLookAndFeelException ex) { ex.printStackTrace(System.err); }
    }
    
    public void focus(@NonNull Window frame)
    {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = frame.getSize();
        frame.setLocation((screen.width - window.width) / 2,
                        (screen.height - window.height) / 2);
    }
    
    public void focus(@NonNull JDialog dialog)
    {
        Container parent = dialog.getParent();
        if(!(parent instanceof JDialog) && !(parent instanceof JFrame))
        {
            focus((Window) dialog);
            return;
        }
        
        Point p = parent.getLocation();
        Dimension screen = parent.getSize();
        Dimension window = dialog.getSize();
        p.x += (screen.width - window.width) / 2;
        p.y += (screen.height - window.height) / 2;
        
        dialog.setLocation(p);
    }
    
    public void setIcon(@NonNull JFrame frame)
    {
        if(LOGO == null)
        {
            try { LOGO = ImageIO.read(IOUtils.getClasspathResourceUrl("/logo.png")); }
            catch(IOException ex) { ex.printStackTrace(System.err); }
        }
        if(LOGO != null)
            frame.setIconImage(LOGO);
    }
}
