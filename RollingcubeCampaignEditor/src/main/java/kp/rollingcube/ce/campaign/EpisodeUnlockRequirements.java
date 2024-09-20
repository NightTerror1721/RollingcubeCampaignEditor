package kp.rollingcube.ce.campaign;

import java.util.Optional;
import kp.rollingcube.ce.campaign.locks.EpisodeUnlockRequirementsForEpisode;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public interface EpisodeUnlockRequirements
{
    Iterable<EpisodeUnlockRequirementsForEpisode> getEpisodes();
    
    Optional<EpisodeUnlockRequirementsForEpisode> getEpisode(String episodeName);
    
    boolean hasEpisode(@NonNull String episodeName);
    
    EpisodeUnlockRequirementsForEpisode addEpisode(String episodeName);
}
