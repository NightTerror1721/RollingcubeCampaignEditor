package kp.rollingcube.ce.utils;

import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class MathUtils
{
    public int clamp(int value, int min, int max)
    {
        return Math.min(max, Math.max(min, value));
    }
}
