package kp.rollingcube.ce.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class StringUtils
{
    public boolean isNullOrEmpty(String string)
    {
        return string == null || string.isEmpty();
    }
    
    public boolean isNullOrBlank(String string)
    {
        return string == null || string.isBlank();
    }
    
    public @NonNull String nonNull(String value, @NonNull String defaultValue)
    {
        return value == null ? defaultValue : value;
    }
    
    public @NonNull String nonNull(String value)
    {
        return nonNull(value, "");
    }
}
