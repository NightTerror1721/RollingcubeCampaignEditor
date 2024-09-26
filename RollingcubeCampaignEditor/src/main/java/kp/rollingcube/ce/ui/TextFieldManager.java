package kp.rollingcube.ce.ui;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public abstract class TextFieldManager<T> implements DocumentListener
{
    private static final Color ERROR_FORE_COLOR = Color.WHITE;
    private static final Color ERROR_BACK_COLOR = Color.RED;
    
    private final AtomicBoolean updateEnabled = new AtomicBoolean(true);
    
    private final @NonNull JTextField field;
    private final @NonNull Consumer<T> setter;
    private final @NonNull Supplier<T> getter;
    private final @NonNull ChangesNotifier changesNotifier;
    
    private final Color normalForegroundColor;
    private final Color normalBackgroundColor;
    
    public TextFieldManager(@NonNull JTextField field, @NonNull Consumer<T> setter, @NonNull Supplier<T> getter, @NonNull ChangesNotifier changesNotifier)
    {
        this.field = field;
        this.setter = setter;
        this.getter = getter;
        this.changesNotifier = changesNotifier;
        
        this.normalForegroundColor = field.getForeground();
        this.normalBackgroundColor = field.getBackground();
        
        field.getDocument().addDocumentListener(this);
    }
    
    protected abstract boolean validateFieldInput(String str);
    protected abstract boolean validateValue(T value);
    protected abstract String convertToString(T value);
    protected abstract T convertFromString(String str);
    
    
    public synchronized void bind()
    {
        updateEnabled.set(false);
        field.setText(convertToString(getter.get()));
        validate();
        updateEnabled.set(true);
    }
    
    private synchronized void update()
    {
        if(!updateEnabled.get())
            return;

        var newValue = convertFromString(field.getText());
        setter.accept(newValue);
        validate();
        changesNotifier.notifyChanges();
    }
    
    private void validate()
    {
        if(isValid())
        {
            field.setForeground(normalForegroundColor);
            field.setBackground(normalBackgroundColor);
        }
        else
        {
            field.setForeground(ERROR_FORE_COLOR);
            field.setBackground(ERROR_BACK_COLOR);
        }
    }
    
    public final boolean isValid()
    {
        return validateValue(getter.get()) && validateFieldInput(field.getText());
    }
        
    @Override
    public final void insertUpdate(DocumentEvent e) { update(); }

    @Override
    public final void removeUpdate(DocumentEvent e) { update(); }

    @Override
    public final void changedUpdate(DocumentEvent e) { update(); }
}
