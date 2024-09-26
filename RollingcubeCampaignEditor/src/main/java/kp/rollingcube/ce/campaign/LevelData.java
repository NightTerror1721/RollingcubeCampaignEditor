package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import kp.rollingcube.ce.utils.IOUtils;
import kp.rollingcube.ce.utils.LevelLocation;
import lombok.Getter;
import lombok.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Marc
 */
public final class LevelData
{
    @Getter
    private String data;
    
    @Getter
    private byte[] thumbnail;
    
    private Boolean hasFruitItem;
    private String secretExit;
    
    void prepareLoadState(CampaingLoadSaveState state) { state.addElements(2); }
    void prepareSaveState(CampaingLoadSaveState state) { state.addElements(2); }
    
    public boolean hasDataFile() { return data != null; }
    public boolean hasThumbnailFile() { return thumbnail != null; }
    
    public boolean hasFruit()
    {
        if(hasFruitItem == null)
            readLevelDataExtraInfo();
        return hasFruitItem != null && hasFruitItem;
    }
    
    public Optional<String> getSecretExitLevelTag()
    {
        if(hasFruitItem == null)
            readLevelDataExtraInfo();
        return Optional.ofNullable(secretExit);
    }
    
    public void read(@NonNull Level level, CampaingLoadSaveState state) throws IOException
    {
        var location = level.getLocation();
        readDataFile(location, state);
        readThumbnailFile(location, state);
    }
    
    public void write(@NonNull Level level, CampaingLoadSaveState state) throws IOException
    {
        writeDataFile(level.getLevelPath(), state);
        writeThumbnailFile(level.getThumbnailPath(), state);
    }
    
    public void loadExternLevel(Path path) throws IOException
    {
        var location = LevelLocation.find(path);
        readExternDataFile(location);
        readExternThumbnailFile(location);
    }
    
    private void readDataFile(LevelLocation location, CampaingLoadSaveState state) throws IOException
    {
        try
        {
            state.setCurrentDataText(location.getLevelPath());
            if(!location.hasLevelPath())
            {
                data = null;
                return;
            }

            data = IOUtils.readAllFromFile(location.getLevelPath(), StandardCharsets.UTF_8);
        }
        finally { state.resolveElement(); }
    }
    private void readExternDataFile(LevelLocation location) throws IOException
    {
        if(!location.hasLevelPath())
        {
            data = null;
            return;
        }

        data = IOUtils.readAllFromFile(location.getLevelPath(), StandardCharsets.UTF_8);
    }
    
    private void readThumbnailFile(LevelLocation location, CampaingLoadSaveState state) throws IOException
    {
        try
        {
            state.setCurrentDataText(location.getThumbnailPath());
            if(!location.hasThumbnailPath())
            {
                thumbnail = null;
                return;
            }

            thumbnail = IOUtils.readAllBytesFromFile(location.getThumbnailPath());
        }
        finally { state.resolveElement(); }
    }
    private void readExternThumbnailFile(LevelLocation location) throws IOException
    {
        if(!location.hasThumbnailPath())
        {
            thumbnail = null;
            return;
        }

        thumbnail = IOUtils.readAllBytesFromFile(location.getThumbnailPath());
    }
    
    private void writeDataFile(Path path, CampaingLoadSaveState state) throws IOException
    {
        try
        {
            state.setCurrentDataText(path);
            if(data == null)
                return;

            IOUtils.writeToFile(path, data, StandardCharsets.UTF_8);
        }
        finally { state.resolveElement(); }
    }
    
    private void writeThumbnailFile(Path path, CampaingLoadSaveState state) throws IOException
    {
        try
        {
            state.setCurrentDataText(path);
            if(thumbnail == null)
                return;

            //IOUtils.writeImage(path, thumbnail);
            IOUtils.writeAllBytesToFile(path, thumbnail);
        }
        finally { state.resolveElement(); }
    }
    
    private void readLevelDataExtraInfo()
    {
        if(data == null)
        {
            hasFruitItem = false;
            secretExit = null;
            return;
        }
        
        try
        {
            boolean fruit = false;
            String secret = null;
            
            var json = new JSONObject(new JSONTokener(data));
            var jsonBlocks = json.optJSONArray("blocks");
            if(jsonBlocks != null)
            {
                int len = jsonBlocks.length();
                for(int i = 0; i < len && !(fruit && secret != null); i++)
                {
                    var jsonBlock = jsonBlocks.optJSONObject(i);
                    if(jsonBlock == null)
                        continue;
                    
                    for(var jsonSide : blockSides(jsonBlock))
                    {
                        if(secret == null)
                            secret = getSecretLevelTag(jsonSide);
                        if(!fruit)
                            fruit = getHasFruit(jsonSide);
                    }
                }
            }
            json.clear();
            
            hasFruitItem = fruit;
            secretExit = secret;
        }
        catch(JSONException ex)
        {
            ex.printStackTrace(System.err);
            hasFruitItem = false;
            secretExit = null;
        }
    }
    
    private static String getSecretLevelTag(JSONObject jsonSide)
    {
        var template = jsonSide.optString("template");
        if(template == null || !template.equals("Exit"))
            return null;
        
        var jsonProps = jsonSide.optJSONObject("properties");
        if(jsonProps == null)
            return null;
        
        var secret = jsonProps.optString("Secret");
        if(secret == null || !secret.equalsIgnoreCase("true"))
            return null;
        
        return jsonProps.optString("NextLevel");
    }
    
    private static boolean getHasFruit(JSONObject jsonSide)
    {
        var jsonItem = jsonSide.optJSONObject("item");
        if(jsonItem == null)
            return false;
        
        var template = jsonItem.optString("template");
        return template != null && template.equals("Fruit");
    }
    
    
    private static final String[] SIDE_NAMES = { "up", "down", "left", "right", "front", "back" };
    private static Iterable<JSONObject> blockSides(JSONObject jsonBlock)
    {
        return () -> new Iterator<JSONObject>()
        {
            private int sideIdx = 0;
            private JSONObject jsonSide;
            
            {
                prepareNextSide();
            }
            
            private void prepareNextSide()
            {
                jsonSide = null;
                while(jsonSide == null && sideIdx < SIDE_NAMES.length)
                    jsonSide = jsonBlock.optJSONObject(SIDE_NAMES[sideIdx++]);
            }

            @Override
            public boolean hasNext() { return jsonSide != null; }

            @Override
            public JSONObject next()
            {
                if(jsonSide == null)
                    throw new NoSuchElementException();
                
                var next = jsonSide;
                prepareNextSide();
                return next;
            }
        };
    }
}
