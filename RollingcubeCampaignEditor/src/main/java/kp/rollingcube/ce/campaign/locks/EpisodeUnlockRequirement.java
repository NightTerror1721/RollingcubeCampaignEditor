package kp.rollingcube.ce.campaign.locks;

import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public final class EpisodeUnlockRequirement
{
    @Getter
    private @NonNull EpisodeUnlockRequirementId id = EpisodeUnlockRequirementId.DEFAULT;
    
    @Getter
    private int value;
    
    private EpisodeUnlockRequirement(
            EpisodeUnlockRequirementType type,
            EpisodeUnlockRequirementLevelType levelType,
            EpisodeUnlockRequirementRequest request,
            int value
    )
    {
        this.id = EpisodeUnlockRequirementId.of(type, levelType, request);
        this.value = Math.max(0, value);
    }
    
    public static EpisodeUnlockRequirement of(
            EpisodeUnlockRequirementType type,
            EpisodeUnlockRequirementLevelType levelType,
            EpisodeUnlockRequirementRequest request,
            int value
    )
    {
        return new EpisodeUnlockRequirement(type, levelType, request, value);
    }
    
    public static EpisodeUnlockRequirement of(@NonNull EpisodeUnlockRequirementId id, int value)
    {
        return new EpisodeUnlockRequirement(id.getType(), id.getLevelType(), id.getRequest(), value);
    }
    
    public @NonNull EpisodeUnlockRequirement copy()
    {
        return new EpisodeUnlockRequirement(getType(), getLevelType(), getRequest(), value);
    }
    
    public EpisodeUnlockRequirementType getType() { return id.getType(); }
    public EpisodeUnlockRequirementLevelType getLevelType() { return id.getLevelType(); }
    public EpisodeUnlockRequirementRequest getRequest() { return id.getRequest(); }
    
    public void setValue(int value) { this.value = Math.max(0, value); }
    
}
