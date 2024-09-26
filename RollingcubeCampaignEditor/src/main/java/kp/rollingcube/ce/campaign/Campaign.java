package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import kp.rollingcube.ce.utils.DirectoryUtils;
import kp.rollingcube.ce.utils.IOUtils;
import kp.rollingcube.ce.utils.MathUtils;
import kp.rollingcube.ce.utils.StringUtils;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Marc
 */
public final class Campaign
{
    @Getter private @NonNull Path path;
    @Getter private @NonNull String name;
    @Getter private int requiredFruitsToBonus;
    @Getter private int levelsUntilSaveGame;
    @Getter private Integer defaultUnlockedNormalLevels;
    private byte[] thumbnail;
    
    private final ArrayList<Episode> episodes = new ArrayList<>();
    private final HashMap<String, Episode> episodesByName = new HashMap<>();
    
    private Campaign(Path path)
    {
        this.path = path;
    }
    
    public Path getPropertiesPath() { return path.resolve("campaign.json"); }
    public Path getThumbnailPath() { return path.resolve("campaign.png"); }
    
    public boolean hasPropertiesPath() { return Files.isRegularFile(getPropertiesPath()); }
    public boolean hasThumbnailPath() { return Files.isRegularFile(getThumbnailPath()); }
    
    public boolean hasThumbnail() { return thumbnail != null; }
    public Optional<byte[]> getThumbnail() { return Optional.ofNullable(thumbnail); }
    
    public void setName(String name) { this.name = StringUtils.nonNull(name); }
    public void setRequiredFruitsToBonus(int value) { this.requiredFruitsToBonus = MathUtils.clamp(value, 0, 5); }
    public void setLevelsUntilSaveGame(int value) { this.levelsUntilSaveGame = Math.max(0, value); }
    public void setDefaultUnlockedNormalLevels(Integer value)
    {
        if(value == null)
            this.defaultUnlockedNormalLevels = null;
        else
            this.defaultUnlockedNormalLevels = Math.max(1, value);
    }
    
    public boolean hasAnyEpisode() { return !episodes.isEmpty(); }
    public int getEpisodesCount() { return episodes.size(); }
    
    public Iterable<Episode> getEpisodes() { return episodes; }
    
    public boolean hasEpisode(String name)
    {
        if(StringUtils.isNullOrBlank(name))
            return false;
        
        return episodesByName.containsKey(name);
    }
    
    public @NonNull Episode getEpisode(int index)
    {
        return episodes.get(index);
    }
    
    public @NonNull Episode getEpisode(String name)
    {
        if(StringUtils.isNullOrBlank(name))
            throw new IllegalArgumentException("Episode name cannot be null or empty");
        
        var episode = episodesByName.getOrDefault(name, null);
        if(episode == null)
            throw new IllegalArgumentException(String.format("Episode \"%s\" not found", name));
        
        return episode;
    }
    
    public Episode addEpisode(String name) throws IOException
    {
        if(StringUtils.isNullOrBlank(name))
            throw new IllegalArgumentException("Episode name cannot be null or empty");
        
        if(episodesByName.containsKey(name))
            throw new IllegalArgumentException(String.format("Episode \"%s\" already exists", name));
        
        var episode = new Episode(this, episodes.size(), name);
        episodes.add(episode);
        episodesByName.put(episode.getName(), episode);
        
        Files.createDirectories(episode.getPath());
        
        return episode;
    }
    
    public void changeEpisodeName(String currentName, String newName) throws IOException
    {
        if(StringUtils.isNullOrBlank(newName))
            throw new IllegalArgumentException("Episode name cannot be null or empty");
        
        var episode = getEpisode(currentName);
        
        if(hasEpisode(newName))
            throw new IllegalArgumentException(String.format("Episode \"%s\" already exists", newName));
        
        var sourcePath = episode.getPath();
        episodesByName.remove(episode.getName());
        
        episode.setName(newName);
        episodesByName.put(episode.getName(), episode);
        var destPath = episode.getPath();
        
        Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public void swapEpisodes(int sourceIndex, int targetIndex)
    {
        if(sourceIndex < 0 || sourceIndex >= episodes.size())
            throw new IndexOutOfBoundsException(sourceIndex);
        if(targetIndex < 0 || targetIndex >= episodes.size())
            throw new IndexOutOfBoundsException(targetIndex);
        
        if(sourceIndex == targetIndex)
            return;
        
        var current = episodes.get(sourceIndex);
        var target = episodes.get(targetIndex);
        
        target.setIndex(sourceIndex);
        current.setIndex(targetIndex);
        
        episodes.set(current.getIndex(), current);
        episodes.set(target.getIndex(), target);
    }
    
    public void removeEpisode(String name)
    {
        if(StringUtils.isNullOrBlank(name))
            throw new IllegalArgumentException("Episode name cannot be null or empty");
        
        var episode = getEpisode(name);
        episodesByName.remove(episode.getName());
        episodes.remove(episode.getIndex());
        
        for(int i = 0; i < episodes.size(); ++i)
            episodes.get(i).setIndex(i);
    }
    
    public void changeThumbnail(Path path) throws IOException
    {
        var data = IOUtils.readAllBytesFromFile(path);
        thumbnail = data;
    }
    
    public void removeThumbnail()
    {
        thumbnail = null;
    }
    
    public CampaingLoadSaveState save(Path path) throws IllegalArgumentException
    {
        if(Files.exists(path) && (!Files.isDirectory(path) || !DirectoryUtils.isEmpty(path)))
        {
            throw new IllegalArgumentException(String.format(
                    "\"%s\" is not a valid folder to save a campaign. Required new folder or empty folder.", path.toString()));
        }
        
        var state = new CampaingLoadSaveState(this, path, Campaign::doSave);
        return state;
    }
    private static void doSave(@NonNull Campaign campaign, @NonNull Path path, @NonNull CampaingLoadSaveState state)
    {
        state.start(path);
        try
        {
            state.addElements(2);
            for(var episode : campaign.episodes)
                episode.prepareSaveState(state);
            
            campaign.path = path;
            Files.createDirectories(path);
            for(var episode : campaign.episodes)
                episode.write(state);
            
            try(var w = Files.newBufferedWriter(campaign.getPropertiesPath()))
            {
                var json = campaign.toJson();
                json.write(w, 4, 0);
                state.resolveElement();
            }
            
            try
            {
                state.setCurrentDataText(campaign.getThumbnailPath());
                if(campaign.thumbnail != null)
                {
                    IOUtils.writeAllBytesToFile(campaign.getThumbnailPath(), campaign.thumbnail);
                }
            }
            catch(IOException ex) {}
            finally { state.resolveElement(); }
            
            state.finish();
        }
        catch(Throwable ex)
        {
            ex.printStackTrace(System.err);
            state.finish(ex.getLocalizedMessage());
        }
    }
    
    public static CampaingLoadSaveState load(@NonNull Path path)
    {
        var fileName = path.getFileName();
        if(fileName != null && fileName.toString().equals("campaign.json"))
            path = path.getParent();
        
        var campaign = new Campaign(path);
        var state = new CampaingLoadSaveState(campaign, path, Campaign::doLoad);
        return state;
    }
    private static void doLoad(@NonNull Campaign campaign, @NonNull Path path, @NonNull CampaingLoadSaveState state)
    {
        try(var is = Files.newInputStream(campaign.getPropertiesPath()))
        {
            var json = new JSONObject(new JSONTokener(is));
            campaign.prepareFromJson(json);
            
            state.addElements(2);
            for(var episode : campaign.episodes)
                episode.prepareLoadState(state);

            state.start(path);
            state.resolveElement();
            for(var episode : campaign.episodes)
                episode.read(state);
            
            if(!campaign.hasThumbnailPath())
                campaign.thumbnail = null;
            else
            {
                state.setCurrentDataText(campaign.getThumbnailPath());
                try { campaign.thumbnail = IOUtils.readAllBytesFromFile(campaign.getThumbnailPath()); }
                catch(IOException ex) {}
                finally { state.resolveElement(); }
            }
            
            state.finish();
        }
        catch(Throwable ex)
        {
            ex.printStackTrace(System.err);
            state.finish(ex.getLocalizedMessage());
        }
    }
    
    public static Campaign createNew()
    {
        return new Campaign(IOUtils.getUserDirectory().resolve("temp"));
    }
    
    private @NonNull JSONObject toJson()
    {
        var json = new JSONObject();
        json.put("name", name);
        json.put("requiredFruitsToBonus", requiredFruitsToBonus);
        json.put("levelsUntilSaveGame", levelsUntilSaveGame);
        
        if(defaultUnlockedNormalLevels != null && defaultUnlockedNormalLevels > 0)
            json.put("defaultUnlockedNormalLevels", defaultUnlockedNormalLevels);
        
        var episodesJson = new JSONArray();
        for(var episode : episodes)
            episodesJson.put(episode.toJson());
        json.put("episodes", episodesJson);
        
        return json;
    }
    
    private void prepareFromJson(JSONObject json)
    {
        episodes.clear();
        episodesByName.clear();
        
        if(json == null)
            json = new JSONObject();
        
        String nameJson = json.getString("name");
        if(StringUtils.isNullOrBlank(nameJson))
            nameJson = "unknown";
        
        this.name = nameJson;
        this.requiredFruitsToBonus = MathUtils.clamp(json.optInt("requiredFruitsToBonus", 0), 0, 5);
        this.levelsUntilSaveGame = Math.max(0, json.optInt("levelsUntilSaveGame"));
        
        var unlockLevels = json.optInt("defaultUnlockedNormalLevels", 0);
        if(unlockLevels > 0)
            this.defaultUnlockedNormalLevels = unlockLevels;
        else
            this.defaultUnlockedNormalLevels = null;
        
        var episodesArrayJson = json.optJSONArray("episodes");
        if(episodesArrayJson != null && !episodesArrayJson.isEmpty())
        {
            int index = 0;
            int len = episodesArrayJson.length();
            for(int i = 0; i < len; ++i)
            {
                var episodeJson = episodesArrayJson.get(i);
                if(episodeJson != null && episodeJson instanceof JSONObject)
                {
                    var episodeOpt = Episode.createFromJson(this, index, (JSONObject) episodeJson);
                    if(episodeOpt.isPresent() && !episodesByName.containsKey(episodeOpt.get().getName()))
                    {
                        episodes.add(episodeOpt.get());
                        episodesByName.put(episodeOpt.get().getName(), episodeOpt.get());
                        index++;
                    }
                }
            }
        }
    }
}
