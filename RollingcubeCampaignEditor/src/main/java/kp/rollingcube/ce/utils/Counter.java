package kp.rollingcube.ce.utils;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Marc
 */
public final class Counter
{
    @Getter @Setter
    private int value;
    
    public Counter(int initialValue) { this.value = initialValue; }
    public Counter() { this.value = 0; }
    
    public Counter increase() { value++; return this; }
    public Counter decrease() { value--; return this; }
    
    public Counter add(int amount) { value += amount; return this; }
    public Counter subtract(int amount) { value -= amount; return this; }
    public Counter multiply(int amount) { value *= amount; return this; }
    public Counter multiply(float amount) { value *= amount; return this; }
    public Counter divide(int amount) { value /= amount; return this; }
    public Counter divide(float amount) { value /= amount; return this; }
}
