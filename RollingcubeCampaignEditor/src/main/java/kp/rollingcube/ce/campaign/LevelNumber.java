package kp.rollingcube.ce.campaign;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public final class LevelNumber implements Comparable<LevelNumber>
{
    public static LevelNumber INVALID = new LevelNumber(0);
    public static LevelNumber ONE = new LevelNumber(1);
    
    @Getter
    private final int number;
    
    private LevelNumber(int number)
    {
        this.number = Math.max(0, number);
    }
    
    public boolean isValid() { return number != 0; }
    public boolean isInvalid() { return number == 0; }
    
    public boolean equals(LevelNumber other) { return number == other.number; }
    
    @Override
    public boolean equals(Object other)
    {
        if(other == null)
            return false;
        
        if(this == other)
            return true;
        
        if(other instanceof LevelNumber)
            return number == ((LevelNumber) other).number;
        
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + this.number;
        return hash;
    }

    @Override
    public int compareTo(LevelNumber other) { return Integer.compare(number, other.number); }
    
    @Override
    public @NonNull String toString() { return Integer.toString(number); }
    
    public static LevelNumber of(int number) { return new LevelNumber(number); }
    
    public static LevelNumber of(String number)
    {
        try { return new LevelNumber(Integer.parseInt(number)); }
        catch(NumberFormatException ex) { return INVALID; }
    }
}
