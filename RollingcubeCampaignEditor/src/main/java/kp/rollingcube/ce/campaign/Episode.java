package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import kp.rollingcube.ce.campaign.locks.EpisodeUnlockRequirementsCollection;
import kp.rollingcube.ce.utils.IOUtils;
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
    
    private byte[] thumbnail;
    
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
    public boolean hasThumbnailPath() { return Files.isRegularFile(getThumbnailPath()); }
    
    public boolean hasThumbnail() { return thumbnail != null; }
    public @NonNull Optional<byte[]> getThumbnail() { return Optional.ofNullable(thumbnail); }
    
    public void changeName(String name) throws IOException { campaign.changeEpisodeName(this.name, name); }
    
    public void changeThumbnail(Path path) throws IOException
    {
        var data = IOUtils.readAllBytesFromFile(path);
        thumbnail = data;
    }
    
    public void removeThumbnail()
    {
        thumbnail = null;
    }
    
    public void swapWith(Episode other)
    {
        if(!campaign.equals(other.campaign))
            throw new IllegalStateException();
        
        campaign.swapEpisodes(index, other.index);
    }
    
    public void remove() { campaign.removeEpisode(name); }
    
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
    
    public @NonNull Set<String> getSecretLevelsNames() { return secretLevels.keySet(); }
    
    public @NonNull NormalLevel getNormalLevel(int index) { return normalLevels.get(index); }
    public @NonNull BonusLevel getBonusLevel(int index) { return bonusLevels.get(index); }
    public @NonNull SecretLevel getSecretLevel(String name)
    {
        var level = secretLevels.getOrDefault(name, null);
        if(level == null)
            throw new IllegalArgumentException(String.format("Level %s not found", name));
        return level;
    }
    
    public void swapNormalLevels(int sourceIndex, int targetIndex) throws IndexOutOfBoundsException
    {
        swapIndexedLevels(normalLevels, sourceIndex, targetIndex);
    }
    
    public void swapBonusLevels(int sourceIndex, int targetIndex) throws IndexOutOfBoundsException
    {
        swapIndexedLevels(bonusLevels, sourceIndex, targetIndex);
    }
    
    private <T extends IndexedLevel> void swapIndexedLevels(ArrayList<T> levels, int sourceIndex, int targetIndex) throws IndexOutOfBoundsException
    {
        if(sourceIndex < 0 || sourceIndex >= levels.size())
            throw new IndexOutOfBoundsException(sourceIndex);
        if(targetIndex < 0 || targetIndex >= levels.size())
            throw new IndexOutOfBoundsException(targetIndex);
        
        if(sourceIndex == targetIndex)
            return;
        
        var current = levels.get(sourceIndex);
        var target = levels.get(targetIndex);
        
        current.setIndex(targetIndex);
        levels.set(current.getIndex(), current);
        
        target.setIndex(sourceIndex);
        levels.set(target.getIndex(), target);
    }
    
    public void changeSecretLevelName(String currentName, String newName) throws IllegalArgumentException
    {
        if(currentName == null || currentName.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        if(newName == null || newName.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        if(existsSecretLevel(newName))
            throw new IllegalArgumentException(String.format("Secret Level \"%s\" already exists", newName));
        
        var level = getSecretLevel(currentName);
        secretLevels.remove(level.getName());
        level.setName(newName);
        secretLevels.put(level.getName(), level);
    }
    
    public void removeNormalLevel(int index) throws IllegalArgumentException
    {
        removeIndexedLevel(normalLevels, index);
    }
    
    public void removeBonusLevel(int index) throws IllegalArgumentException
    {
        removeIndexedLevel(bonusLevels, index);
    }
    
    private void removeIndexedLevel(ArrayList<? extends IndexedLevel> levels, int index) throws IllegalArgumentException
    {
        if(index < 0 || index >= levels.size())
            throw new IndexOutOfBoundsException(index);
        
        levels.remove(index);
        
        int idx = 0;
        for(var level : levels)
            level.setIndex(idx++);
    }
    
    public void removeSecretLevel(String name) throws IllegalArgumentException
    {
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("Secret Level name cannot be null or empty");
        
        var level = getSecretLevel(name);
        secretLevels.remove(level.getName());
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
        newLevel.loadExternData(levelPath);
        newLevel.setIndex(levels.size());
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
        level.loadExternData(levelPath);
        
        secretLevels.put(level.getName(), level);
        return level;
    }
    
    void read(CampaingLoadSaveState state)
    {
        readLevels(normalLevels, state);
        readLevels(bonusLevels, state);
        readLevels(secretLevels.values(), state);
        
        if(!hasThumbnailPath())
            thumbnail = null;
        else
        {
            state.setCurrentDataText(getThumbnailPath());
            try { thumbnail = IOUtils.readAllBytesFromFile(getThumbnailPath()); }
            catch(IOException ex) {}
            finally { state.resolveElement(); }
        }
    }
    private static void readLevels(Iterable<? extends Level> levels, CampaingLoadSaveState state)
    {
        for(var level : levels)
        {
            try
            {
                level.read(state);
            }
            catch(IOException ex)
            {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    void write(CampaingLoadSaveState state)
    {
        try
        {
            Files.createDirectories(getPath());
            Files.createDirectories(getNormalLevelsPath());
            Files.createDirectories(getBonusLevelsPath());
            Files.createDirectories(getSecretLevelsPath());
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        writeLevels(normalLevels, state);
        writeLevels(bonusLevels, state);
        writeLevels(secretLevels.values(), state);
        
        try
        {
            state.setCurrentDataText(getThumbnailPath());
            if(thumbnail != null)
            {
                IOUtils.writeAllBytesToFile(getThumbnailPath(), thumbnail);
            }
        }
        catch(IOException ex) {}
        finally { state.resolveElement(); }
    }
    private static void writeLevels(Iterable<? extends Level> levels, CampaingLoadSaveState state)
    {
        for(var level : levels)
        {
            try
            {
                level.write(state);
            }
            catch(IOException ex)
            {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    void prepareLoadState(CampaingLoadSaveState state)
    {
        prepareLoadSaveState(normalLevels, state, false);
        prepareLoadSaveState(bonusLevels, state, false);
        prepareLoadSaveState(secretLevels.values(), state, false);
        state.addElement();
    }
    
    void prepareSaveState(CampaingLoadSaveState state)
    {
        prepareLoadSaveState(normalLevels, state, true);
        prepareLoadSaveState(bonusLevels, state, true);
        prepareLoadSaveState(secretLevels.values(), state, true);
        state.addElement();
    }
    
    private static void prepareLoadSaveState(Iterable<? extends Level> levels, CampaingLoadSaveState state, boolean isSave)
    {
        for(var level : levels)
        {
            if(isSave)
                level.prepareSaveState(state);
            else
                level.prepareLoadState(state);
        }
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
                    
                    episode.secretLevels.put(level.getName(), level);
                }
            }
        }
        
        var reqsJson = json.optJSONObject("unlockRequirements");
        episode.requirements.fromJson(reqsJson);
        
        return Optional.of(episode);
    }
}
