package kp.rollingcube.ce.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JTextField;
import kp.rollingcube.ce.utils.StringUtils;

/**
 *
 * @author Marc
 */
public class StringTextFieldManager extends TextFieldManager<String>
{
    public StringTextFieldManager(JTextField field, Consumer<String> setter, Supplier<String> getter, ChangesNotifier changesNotifier)
    {
        super(field, setter, getter, changesNotifier);
    }
    
    @Override
    protected boolean validateFieldInput(String str)
    {
        return !StringUtils.isNullOrBlank(str);
    }

    @Override
    protected boolean validateValue(String value)
    {
        return !StringUtils.isNullOrBlank(value);
    }

    @Override
    protected String convertToString(String value)
    {
        return value;
    }

    @Override
    protected String convertFromString(String str)
    {
        return str;
    }
}
