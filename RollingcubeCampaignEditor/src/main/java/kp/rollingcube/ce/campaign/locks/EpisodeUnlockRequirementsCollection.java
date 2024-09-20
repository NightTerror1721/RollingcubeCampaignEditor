package kp.rollingcube.ce.campaign.locks;

import java.util.HashMap;
import java.util.Optional;
import kp.rollingcube.ce.campaign.EpisodeUnlockRequirements;
import kp.rollingcube.ce.utils.StringUtils;
import lombok.NonNull;
import org.json.JSONObject;

/**
 *
 * @author Marc
 */
public final class EpisodeUnlockRequirementsCollection implements EpisodeUnlockRequirements
{
    private final HashMap<String, EpisodeUnlockRequirementsForEpisode> episodes = new HashMap<>();
    
    public boolean isEmpty() { return episodes.isEmpty(); }
    
    @Override
    public Iterable<EpisodeUnlockRequirementsForEpisode> getEpisodes() { return episodes.values(); }
    
    @Override
    public Optional<EpisodeUnlockRequirementsForEpisode> getEpisode(String episodeName)
    {
        if(episodeName == null || episodeName.isBlank())
            return Optional.empty();
        
        var reqs = episodes.getOrDefault(episodeName, null);
        if(reqs == null)
            return Optional.empty();
        
        return Optional.of(reqs);
    }
    
    @Override
    public boolean hasEpisode(@NonNull String episodeName)
    {
        return episodes.containsKey(episodeName);
    }
    
    @Override
    public EpisodeUnlockRequirementsForEpisode addEpisode(String episodeName)
    {
        if(episodeName == null || episodeName.isBlank())
            throw new IllegalArgumentException("Episode name cannot be null or empty");
        
        if(episodes.containsKey(episodeName))
            throw new IllegalArgumentException(String.format("Episode \"%s\" already exists", episodeName));
        
        var episode = new EpisodeUnlockRequirementsForEpisode(episodeName);
        episodes.put(episode.getEpisodeName(), episode);
        
        return episode;
    }
    
    public void clear() { episodes.clear(); }
    
    public @NonNull JSONObject toJson()
    {
        var json = new JSONObject();
        for(var epi : episodes.values())
        {
            JSONObject jsonEpi = epi.toJson();
            json.put(epi.getEpisodeName(), jsonEpi);
        }
        return json;
    }
    
    public void fromJson(JSONObject json)
    {
        episodes.clear();
        
        if(json == null || json.isEmpty())
            return;
        
        for(var key : json.keySet())
        {
            if(StringUtils.isNullOrBlank(key))
                continue;
            
            var jsonEpi = json.optJSONObject(key);
            if(jsonEpi == null || jsonEpi.isEmpty())
                continue;
            
            var epi = new EpisodeUnlockRequirementsForEpisode(key);
            epi.fromJson(jsonEpi);
            
            episodes.put(epi.getEpisodeName(), epi);
        }
    }
}
