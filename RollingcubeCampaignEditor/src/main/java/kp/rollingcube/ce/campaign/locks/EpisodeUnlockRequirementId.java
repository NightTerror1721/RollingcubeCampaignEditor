package kp.rollingcube.ce.campaign.locks;

import lombok.Value;

/**
 *
 * @author Marc
 */
@Value(staticConstructor = "of")
public final class EpisodeUnlockRequirementId
{
    public static final EpisodeUnlockRequirementId DEFAULT = of(
            EpisodeUnlockRequirementType.AMOUNT,
            EpisodeUnlockRequirementLevelType.ANY,
            EpisodeUnlockRequirementRequest.COMPLETED
    );
    
    private final EpisodeUnlockRequirementType type;
    private final EpisodeUnlockRequirementLevelType levelType;
    private final EpisodeUnlockRequirementRequest request;
    
    public EpisodeUnlockRequirementId copy()
    {
        return new EpisodeUnlockRequirementId(type, levelType, request);
    }
    
    public EpisodeUnlockRequirementId copyWith(EpisodeUnlockRequirementType type)
    {
        return new EpisodeUnlockRequirementId(type, levelType, request);
    }
    public EpisodeUnlockRequirementId copyWith(EpisodeUnlockRequirementLevelType levelType)
    {
        return new EpisodeUnlockRequirementId(type, levelType, request);
    }
    public EpisodeUnlockRequirementId copyWith(EpisodeUnlockRequirementRequest request)
    {
        return new EpisodeUnlockRequirementId(type, levelType, request);
    }
}
