package kp.rollingcube.ce.campaign.locks;

import lombok.NonNull;

/**
 *
 * @author Marc
 */
public enum EpisodeUnlockRequirementType
{
    AMOUNT,
    ALL;
    
    public final boolean isAmount() { return this != ALL; }
    public final boolean isAll() { return this == ALL; }
    
    public final @NonNull String toJsonString()
    {
        switch(this)
        {
            case AMOUNT: return "amount";
            case ALL: return "all";
            default: return "amount";
        }
    }
    
    public static @NonNull EpisodeUnlockRequirementType decode(String value)
    {
        if(value == null)
            return AMOUNT;
        
        switch(value)
        {
            case "amount": return AMOUNT;
            case "all": return ALL;
        }
        
        return AMOUNT;
    }
}
