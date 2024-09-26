package kp.rollingcube.ce.ui;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.swing.JTextField;
import lombok.Getter;

/**
 *
 * @author Marc
 */
public class IntegerTextFieldManager extends TextFieldManager<Integer>
{
    @Getter private int minValue;
    @Getter private int maxValue;
    
    public IntegerTextFieldManager(JTextField field, IntConsumer setter, IntSupplier getter, ChangesNotifier changesNotifier) {
        super(field, value -> setter.accept(safeIntegerToInt(value)), () -> getter.getAsInt(), changesNotifier);
    }
    
    public void setMinValue(int value) { this.minValue = value; }
    public void setMaxValue(int value) { this.maxValue = value; }
    
    @Override
    protected boolean validateFieldInput(String str)
    {
        if(str == null)
            return false;
        
        try { return validateValue(Integer.valueOf(str)); }
        catch(NumberFormatException ex) { return false; }
    }

    @Override
    protected boolean validateValue(Integer integerValue)
    {
        int min = Math.min(minValue, maxValue);
        int max = Math.max(minValue, maxValue);
        int value = safeIntegerToInt(integerValue);
        return value >= min && value <= max;
    }

    @Override
    protected String convertToString(Integer value)
    {
        return Integer.toString(safeIntegerToInt(value));
    }

    @Override
    protected Integer convertFromString(String str)
    {
        if(str == null)
            return 0;
        
        try { return Integer.valueOf(str); }
        catch(NumberFormatException ex) { return 0; }
    }
    
    private static int safeIntegerToInt(Integer value)
    {
        if(value == null)
            return 0;
        
        return value;
    }
}
