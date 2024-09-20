package kp.rollingcube.ce.campaign.locks;

import java.util.HashMap;
import kp.rollingcube.ce.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONObject;

/**
 *
 * @author Marc
 */
public final class EpisodeUnlockRequirementsForEpisode
{
    @Getter
    private final @NonNull String episodeName;
    private final HashMap<EpisodeUnlockRequirementId, EpisodeUnlockRequirement> requirements = new HashMap<>();
    
    EpisodeUnlockRequirementsForEpisode(String episodeName)
    {
        this.episodeName = episodeName;
    }
    
    public boolean isEmpty() { return requirements.isEmpty(); }
    
    public Iterable<EpisodeUnlockRequirement> getRequirements() { return requirements.values(); }
    
    public boolean hasRequirement(@NonNull EpisodeUnlockRequirementId reqId)
    {
        return requirements.containsKey(reqId);
    }
    
    public void setAllRequirementEnabled(EpisodeUnlockRequirementLevelType levelType, EpisodeUnlockRequirementRequest request, boolean enabled)
    {
        var id = EpisodeUnlockRequirementId.of(EpisodeUnlockRequirementType.ALL, levelType, request);
        if(enabled)
        {
            var req = requirements.getOrDefault(id, null);
            if(req == null)
            {
                req = EpisodeUnlockRequirement.of(id, 1);
                requirements.put(id, req);
            }
        }
        else
            requirements.remove(id);
    }
    public boolean isAllRequirementEnabled(EpisodeUnlockRequirementLevelType levelType, EpisodeUnlockRequirementRequest request)
    {
        var id = EpisodeUnlockRequirementId.of(EpisodeUnlockRequirementType.ALL, levelType, request);
        return requirements.containsKey(id);
    }
    
    public void setAmountRequirementValue(EpisodeUnlockRequirementLevelType levelType, EpisodeUnlockRequirementRequest request, int value)
    {
        var id = EpisodeUnlockRequirementId.of(EpisodeUnlockRequirementType.AMOUNT, levelType, request);
        if(value > 0)
        {
            var req = requirements.getOrDefault(id, null);
            if(req == null)
            {
                req = EpisodeUnlockRequirement.of(id, 0);
                requirements.put(id, req);
            }
            req.setValue(value);
        }
        else
            requirements.remove(id);
    }
    
    public int getAmountRequirementValue(EpisodeUnlockRequirementLevelType levelType, EpisodeUnlockRequirementRequest request)
    {
        var id = EpisodeUnlockRequirementId.of(EpisodeUnlockRequirementType.AMOUNT, levelType, request);
        var req = requirements.getOrDefault(id, null);
        if(req == null)
            return 0;
        
        return req.getValue();
    }
    
    @NonNull JSONObject toJson()
    {
        var json = new JSONObject();
        for(var req : requirements.values())
        {
            var reqType = json.optJSONObject(req.getType().toJsonString());
            if(reqType == null)
            {
                reqType = new JSONObject();
                json.put(req.getType().toJsonString(), reqType);
            }
            
            var levelType = reqType.optJSONObject(req.getLevelType().toJsonString());
            if(levelType == null)
            {
                levelType = new JSONObject();
                reqType.put(req.getLevelType().toJsonString(), levelType);
            }
            
            if(!json.has(req.getRequest().toJsonString()))
            {
                if(req.getType().isAmount())
                    levelType.put(req.getRequest().toJsonString(), req.getValue());
                else
                    levelType.put(req.getRequest().toJsonString(), true);
            }
        }
        return json;
    }
    
    void fromJson(JSONObject json)
    {
        requirements.clear();
        
        if(json == null || json.isEmpty())
            return;
        
        for(String reqTypeKey : json.keySet())
        {
            if(StringUtils.isNullOrBlank(reqTypeKey))
                continue;
            
            var type = EpisodeUnlockRequirementType.decode(reqTypeKey);
            var reqTypeJson = json.optJSONObject(reqTypeKey);
            if(reqTypeJson == null || reqTypeJson.isEmpty())
                continue;
            
            for(var levelTypeKey : reqTypeJson.keySet())
            {
                if(StringUtils.isNullOrBlank(levelTypeKey))
                    continue;
                
                var levelType = EpisodeUnlockRequirementLevelType.decode(levelTypeKey);
                var levelTypeJson = reqTypeJson.optJSONObject(levelTypeKey);
                if(levelTypeJson == null || levelTypeJson.isEmpty())
                    continue;
                
                for(var requestKey : levelTypeJson.keySet())
                {
                    if(StringUtils.isNullOrBlank(requestKey))
                        continue;
                    
                    var request = EpisodeUnlockRequirementRequest.decode(requestKey);
                    if(type.isAmount())
                    {
                        var value = levelTypeJson.optInt(requestKey, 0);
                        if(value > 0)
                        {
                            var req = EpisodeUnlockRequirement.of(type, levelType, request, value);
                            requirements.put(req.getId(), req);
                        }
                    }
                    else
                    {
                        var value = levelTypeJson.optBoolean(requestKey, false);
                        if(value)
                        {
                            var req = EpisodeUnlockRequirement.of(type, levelType, request, 1);
                            requirements.put(req.getId(), req);
                        }
                    }
                }
            }
        }
    }
}
