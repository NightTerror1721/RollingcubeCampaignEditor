package kp.rollingcube.ce.campaign.locks;

import lombok.NonNull;

/**
 *
 * @author Marc
 */
public enum EpisodeUnlockRequirementLevelType
{
    ANY,
    NORMAL,
    BONUS,
    SECRET;
    
    public final boolean isAny() { return this == ANY; }
    public final boolean isNormal() { return this == NORMAL; }
    public final boolean isBonus() { return this == BONUS; }
    public final boolean isSecret() { return this == SECRET; }
    
    public final @NonNull String toJsonString()
    {
        switch(this)
        {
            case ANY: return "any";
            case NORMAL: return "normal";
            case BONUS: return "bonus";
            case SECRET: return "secret";
            default: return "any";
        }
    }
    
    public static @NonNull EpisodeUnlockRequirementLevelType decode(String value)
    {
        if(value == null)
            return ANY;
        
        switch(value)
        {
            case "any": return ANY;
            case "normal": return NORMAL;
            case "bonus": return BONUS;
            case "secret": return SECRET;
        }
        
        return ANY;
    }
}
