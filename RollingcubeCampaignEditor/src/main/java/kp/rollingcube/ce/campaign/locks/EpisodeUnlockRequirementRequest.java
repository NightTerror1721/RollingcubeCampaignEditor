package kp.rollingcube.ce.campaign.locks;

import lombok.NonNull;

/**
 *
 * @author Marc
 */
public enum EpisodeUnlockRequirementRequest
{
    COMPLETED,
    COMPLETED_BEFORE_HOURGLASS_ENDS,
    COMPLETED_WITH_FULL_TREASURE,
    COLLECTED_FRUITS;
    
    public final boolean isCompleted() { return this == COMPLETED; }
    public final boolean isCompletedBeforeHourglassEnds() { return this == COMPLETED_BEFORE_HOURGLASS_ENDS; }
    public final boolean isCompletedWithFullTreasure() { return this == COMPLETED_WITH_FULL_TREASURE; }
    public final boolean isCollectedFruits() { return this == COLLECTED_FRUITS; }
    
    public final @NonNull String toJsonString()
    {
        switch(this)
        {
            case COMPLETED: return "completed";
            case COMPLETED_BEFORE_HOURGLASS_ENDS: return "completedBeforeHourglassEnds";
            case COMPLETED_WITH_FULL_TREASURE: return "completedWithFullTreasure";
            case COLLECTED_FRUITS: return "collectedFruits";
            default: return "completed";
        }
    }
    
    public static @NonNull EpisodeUnlockRequirementRequest decode(String value)
    {
        if(value == null)
            return COMPLETED;
        
        switch(value)
        {
            case "completed": return COMPLETED;
            case "completedBeforeHourglassEnds": return COMPLETED_BEFORE_HOURGLASS_ENDS;
            case "completedWithFullTreasure": return COMPLETED_WITH_FULL_TREASURE;
            case "collectedFruits": return COLLECTED_FRUITS;
        }
        
        return COMPLETED;
    }
}
