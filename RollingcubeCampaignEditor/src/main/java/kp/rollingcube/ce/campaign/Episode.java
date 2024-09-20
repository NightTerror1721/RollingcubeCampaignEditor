package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import kp.rollingcube.ce.campaign.locks.EpisodeUnlockRequirementsCollection;
import kp.rollingcube.ce.utils.FileUtils;
import kp.rollingcube.ce.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.json.JSONObject;

/**
 *
 * @author Marc
 */
public final class Episode
{
    @Getter
    private final @NonNull Campaign campaign;
    
    @Getter @Setter(AccessLevel.PACKAGE)
    private int index;
    
    @Getter @Setter(AccessLevel.PACKAGE)
    private @NonNull String name;
    
    @Getter
    private Integer unlockedNormalLevels;
    
    private final ArrayList<NormalLevel> normalLevels = new ArrayList<>();
    private final ArrayList<BonusLevel> bonusLevels = new ArrayList<>();
    private final HashMap<String, SecretLevel> secretLevels = new HashMap<>();
    
    private final EpisodeUnlockRequirementsCollection requirements = new EpisodeUnlockRequirementsCollection();
    
    Episode(@NonNull Campaign campaign, int index, @NonNull String name)
    {
        this.campaign = campaign;
        this.index = index;
        this.name = name;
    }
    
    public @NonNull Path getPath() { return campaign.getPath().resolve(name); }
    
    public @NonNull Path getNormalLevelsPath() { return getPath().resolve(LevelType.NORMAL.getFolderName()); }
    public @NonNull Path getBonusLevelsPath() { return getPath().resolve(LevelType.BONUS.getFolderName()); }
    public @NonNull Path getSecretLevelsPath() { return getPath().resolve(LevelType.SECRET.getFolderName()); }
    
    public @NonNull Path getThumbnailPath() { return getPath().resolve("thumbnail.png"); }
    
    public void changeName(String name) throws IOException { campaign.changeEpisodeName(this.name, name); }
    
    public void swapWith(Episode other)
    {
        if(!campaign.equals(other.campaign))
            throw new IllegalStateException();
        
        campaign.swapEpisodes(index, other.index);
    }
    
    public void remove() { campaign.removeEpisode(name); }
    
    void ensureFolder(LevelType folder) throws IOException
    {
        var path = getPath().resolve(folder.getFolderName());
        if(!Files.isDirectory(path))
            Files.createDirectories(path);
    }
    
    public void setUnlockedNormalLevels(Integer amount)
    {
        if(amount == null || amount < 1)
            unlockedNormalLevels = null;
        else
            unlockedNormalLevels = amount;
    }
    
    public EpisodeUnlockRequirements getRequirements() { return requirements; }
    
    public int getNormalLevelsCount() { return normalLevels.size(); }
    public int getBonusLevelsCount() { return bonusLevels.size(); }
    public int getSecretLevelsCount() { return secretLevels.size(); }
    
    public boolean existsSecretLevel(String name)
    {
        if(name == null || name.isBlank())
            return false;
        
        return secretLevels.containsKey(name);
    }
    
    public @NonNull NormalLevel getNormalLevel(int index) { return normalLevels.get(index); }
    public @NonNull BonusLevel getBonusLevel(int index) { return bonusLevels.get(index); }
    public @NonNull SecretLevel getSecretLevel(String name)
    {
        var level = secretLevels.getOrDefault(name, null);
        if(level == null)
            throw new IllegalArgumentException(String.format("Level %s not found", name));
        return level;
    }
    
    public void swapNormalLevels(int sourceIndex, int targetIndex) throws IOException
    {
        swapIndexedLevels(normalLevels, sourceIndex, targetIndex);
    }
    
    public void swapBonusLevels(int sourceIndex, int targetIndex) throws IOException
    {
        swapIndexedLevels(bonusLevels, sourceIndex, targetIndex);
    }
    
    private void swapIndexedLevels(ArrayList<? extends IndexedLevel> levels, int sourceIndex, int targetIndex) throws IOException
    {
        if(sourceIndex < 0 || sourceIndex >= levels.size())
            throw new IndexOutOfBoundsException(sourceIndex);
        if(targetIndex < 0 || targetIndex >= levels.size())
            throw new IndexOutOfBoundsException(targetIndex);
        
        if(sourceIndex == targetIndex)
            return;
        
        var current = levels.get(sourceIndex);
        var target = levels.get(targetIndex);
        
        ensureFolder(current.getType());
        ensureFolder(target.getType());
        FileUtils.swap(current.getLevelPath(), target.getLevelPath());
        FileUtils.swap(current.getThumbnailPath(), target.getThumbnailPath());
    }
    
    public void changeSecretLevelName(String currentName, String newName) throws IOException
    {
        if(currentName == null || currentName.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        if(newName == null || newName.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        if(existsSecretLevel(newName))
            throw new IllegalArgumentException(String.format("Secret Level \"%s\" already exists", newName));
        
        var level = getSecretLevel(currentName);
        
        level.setName(newName);
        var currentLevelPath = level.getLevelPath();
        var currentThumbnailPath = level.getThumbnailPath();
        
        secretLevels.remove(level.getName());
        level.setName(newName);
        secretLevels.put(level.getName(), level);
        
        ensureFolder(level.getType());
        FileUtils.swap(currentLevelPath, level.getLevelPath());
        FileUtils.swap(currentThumbnailPath, level.getThumbnailPath());
    }
    
    public void removeNormalLevel(int index) throws IllegalArgumentException, IOException
    {
        removeIndexedLevel(normalLevels, index);
    }
    
    public void removeBonusLevel(int index) throws IllegalArgumentException, IOException
    {
        removeIndexedLevel(bonusLevels, index);
    }
    
    private void removeIndexedLevel(ArrayList<? extends IndexedLevel> levels, int index) throws IllegalArgumentException, IOException
    {
        if(index < 0 || index >= levels.size())
            throw new IndexOutOfBoundsException(index);
        
        for(int i = index + 1; i < levels.size(); i++)
        {
            var level = levels.get(i - 1);
            var nextLevel = levels.get(i);
            level.replaceLevelFiles(nextLevel.getLevelPath());
        }
        
        var lastLevel = levels.removeLast();
        ensureFolder(lastLevel.getType());
        Files.deleteIfExists(lastLevel.getLevelPath());
        Files.deleteIfExists(lastLevel.getThumbnailPath());
    }
    
    public void removeSecretLevel(String name) throws IOException
    {
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        var level = getSecretLevel(name);
        secretLevels.remove(level.getName());
        
        ensureFolder(level.getType());
        Files.deleteIfExists(level.getLevelPath());
        Files.deleteIfExists(level.getThumbnailPath());
    }
    
    public @NonNull NormalLevel addNormalLevel(Path levelPath) throws IllegalArgumentException, IOException
    {
        return addIndexedLevel(normalLevels, new NormalLevel(this), levelPath);
    }
    
    public @NonNull BonusLevel addBonusLevel(Path levelPath) throws IllegalArgumentException, IOException
    {
        return addIndexedLevel(bonusLevels, new BonusLevel(this), levelPath);
    }
    
    private static <T extends IndexedLevel> @NonNull T addIndexedLevel(ArrayList<T> levels, T newLevel, Path levelPath) throws IllegalArgumentException, IOException
    {
        newLevel.setIndex(levels.size());
        newLevel.replaceLevelFiles(levelPath);
        
        levels.add(newLevel);
        return newLevel;
    }
    
    public @NonNull SecretLevel addSecretLevel(String name, Path levelPath) throws IllegalArgumentException, IOException
    {
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        if(existsSecretLevel(name))
            throw new IllegalArgumentException(String.format("Already exists level with name \"%s\"", name));
        
        SecretLevel level = new SecretLevel(this);
        level.setName(name);
        level.replaceLevelFiles(levelPath);
        
        secretLevels.put(level.getName(), level);
        return level;
    }
    
    @NonNull JSONObject toJson()
    {
        var json = new JSONObject();
        json.put("name", name);
        json.put("normalLevels", normalLevels.size());
        json.put("bonusLevels", bonusLevels.size());
        if(unlockedNormalLevels != null && unlockedNormalLevels >= 1)
            json.put("unlockedNormalLevels", unlockedNormalLevels);
        
        var secretsJson = new JSONObject();
        for(var level : secretLevels.values())
        {
            var levelJson = new JSONObject();
            if(!StringUtils.isNullOrBlank(level.getAlias()))
                levelJson.put("alias", level.getAlias());
            
            levelJson.put("oneTry", level.isOneTry());
            levelJson.put("penalty", level.isPenalty());
            secretsJson.put(level.getName(), levelJson);
        }
        json.put("secretLevels", secretsJson);
        
        if(!requirements.isEmpty())
        {
            var reqJson = requirements.toJson();
            if(!reqJson.isEmpty())
                json.put("unlockRequirements", reqJson);
        }
        
        return json;
    }
    
    static Optional<Episode> createFromJson(Campaign campaign, int index, JSONObject json)
    {
        if(json == null || json.isEmpty())
            return Optional.empty();
        
        var name = json.optString("name");
        if(StringUtils.isNullOrBlank(name))
            return Optional.empty();
        
        var episode = new Episode(campaign, index, name);
        
        int normalLevelsCount = json.optInt("normalLevels", 0);
        for(int i = 0; i < normalLevelsCount; i++)
        {
            var level = new NormalLevel(episode);
            level.setIndex(i);
            episode.normalLevels.add(level);
        }
        
        int bonusLevelsCount = json.optInt("bonusLevels", 0);
        for(int i = 0; i < bonusLevelsCount; i++)
        {
            var level = new BonusLevel(episode);
            level.setIndex(i);
            episode.bonusLevels.add(level);
        }
        
        int unlockedNormalLevels = json.optInt("unlockedNormalLevels", -1);
        if(unlockedNormalLevels >= 0)
            episode.unlockedNormalLevels = unlockedNormalLevels;
        else
            episode.unlockedNormalLevels = null;
        
        var secretsJson = json.optJSONObject("secretLevels");
        if(secretsJson != null && !secretsJson.isEmpty())
        {
            for(var key : secretsJson.keySet())
            {
                if(StringUtils.isNullOrBlank(key) || episode.secretLevels.containsKey(key))
                    continue;
                
                var secretLevelJson = secretsJson.optJSONObject(key);
                if(secretLevelJson != null && !secretLevelJson.isEmpty())
                {
                    var level = new SecretLevel(episode);
                    level.setName(key);
                    
                    var alias = secretLevelJson.optString("alias");
                    boolean oneTry = secretLevelJson.optBoolean("oneTry", true);
                    boolean penalty = secretLevelJson.optBoolean("penalty", false);
                    
                    level.setAlias(StringUtils.isNullOrBlank(alias) ? null : alias);
                    level.setOneTry(oneTry);
                    level.setPenalty(penalty);
                }
            }
        }
        
        var reqsJson = json.optJSONObject("unlockRequirements");
        episode.requirements.fromJson(reqsJson);
        
        return Optional.of(episode);
    }
}
