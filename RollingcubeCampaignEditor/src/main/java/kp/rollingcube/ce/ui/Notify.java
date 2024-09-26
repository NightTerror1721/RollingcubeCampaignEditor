package kp.rollingcube.ce.ui;

import java.awt.Component;
import java.util.Optional;
import javax.swing.JOptionPane;
import kp.rollingcube.ce.utils.StringUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class Notify
{
    public void error(Component parent, String title, String message)
    {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    public void error(Component parent, String message)
    {
        error(parent, "An error has occurred", message);
    }
    
    public void ferror(Component parent, String title, String message, Object... messageArgs)
    {
        if(title == null)
            title = "An error has occurred";
        var fmsg = String.format(message, messageArgs);
        error(parent, title, fmsg);
    }
    public void ferror(Component parent, String message, Object... messageArgs)
    {
        var fmsg = String.format(message, messageArgs);
        error(parent, fmsg);
    }
    
    
    public CancelableAnswere cancelableAsk(Component parent, String title, String message)
    {
        var result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        switch(result)
        {
            case JOptionPane.YES_OPTION: return CancelableAnswere.YES;
            case JOptionPane.NO_OPTION: return CancelableAnswere.NO;
            case JOptionPane.CANCEL_OPTION: return CancelableAnswere.CANCEL;
            default: return CancelableAnswere.CANCEL;
        }
    }
    
    public boolean ask(Component parent, String title, String message)
    {
        var result = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    
    public @NonNull Optional<String> askName(Component parent, String title, String message)
    {
        var result = JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
        if(StringUtils.isNullOrBlank(result))
            return Optional.empty();
        return Optional.of(result);
    }
    
    
    public enum CancelableAnswere
    {
        YES,
        NO,
        CANCEL
    }
}
