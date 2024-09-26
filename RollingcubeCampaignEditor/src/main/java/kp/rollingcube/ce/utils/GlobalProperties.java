package kp.rollingcube.ce.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Marc
 */
@UtilityClass
public class GlobalProperties
{
    private final Path PROPS_FILE = IOUtils.getHomeDirectory().resolve(".RollingcubeCampaignEditorProps.json");
    
    private final HashMap<String, String> props = new HashMap<>();
    
    static { load(); }
    
    private void load()
    {
        try(var is = Files.newInputStream(PROPS_FILE))
        {
            var json = new JSONObject(new JSONTokener(is));
            for(var key : json.keySet())
            {
                var value = json.get(key);
                props.put(key, value.toString());
            }
        }
        catch(Exception ex)
        {
            //ex.printStackTrace(System.err);
        }
    }
    
    public void save()
    {
        try(var bw = Files.newBufferedWriter(PROPS_FILE))
        {
            var json = new JSONObject();
            for(var entry : props.entrySet())
                json.put(entry.getKey(), entry.getValue());
            
            json.write(bw);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public String get(@NonNull String name, String defaultValue)
    {
        return props.getOrDefault(name, defaultValue);
    }
    
    public int getInt(@NonNull String name, int defaultValue)
    {
        var value = props.getOrDefault(name, null);
        if(value == null)
            return defaultValue;
        
        try { return Integer.parseInt(value); }
        catch(NumberFormatException ex) { return defaultValue; }
    }
    
    public boolean getBoolean(@NonNull String name, boolean defaultValue)
    {
        var value = props.getOrDefault(name, null);
        if(value == null)
            return defaultValue;
        
        return value.equalsIgnoreCase("true");
    }
    
    public Path getPath(@NonNull String name, Path defaultValue)
    {
        var value = props.getOrDefault(name, null);
        if(value == null)
            return defaultValue;
        
        return Path.of(value);
    }
    
    public void set(@NonNull String name, String value, boolean save)
    {
        if(value == null)
            props.remove(name);
        else
            props.put(name, value);
        
        if(save)
            save();
    }
    public void set(@NonNull String name, String value) { set(name, value, true); }
    
    public void set(@NonNull String name, int value, boolean save) { set(name, Integer.toString(value), save); }
    public void set(@NonNull String name, int value) { set(name, Integer.toString(value)); }
    
    public void set(@NonNull String name, boolean value, boolean save) { set(name, Boolean.toString(value), save); }
    public void set(@NonNull String name, boolean value) { set(name, Boolean.toString(value)); }
    
    public void set(@NonNull String name, Path value, boolean save) { set(name, value == null ? null : value.toAbsolutePath().toString(), save); }
    public void set(@NonNull String name, Path value) { set(name, value == null ? null : value.toAbsolutePath().toString()); }
}
