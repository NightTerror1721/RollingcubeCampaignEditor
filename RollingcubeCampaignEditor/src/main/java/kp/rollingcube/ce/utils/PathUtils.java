package kp.rollingcube.ce.utils;

import java.nio.file.Path;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class PathUtils
{
    public @NonNull Optional<String> getExtension(@NonNull String filename)
    {
        var chars = filename.toCharArray();
        int lastDot = -1;
        for(int i = 0; i < chars.length; ++i)
        {
            if(chars[i] == '.')
                lastDot = i;
        }
        
        if(lastDot < 0 || (chars.length - lastDot) <= 1)
            return Optional.empty();
        
        return Optional.of(filename.substring(lastDot + 1));
    }
    
    public @NonNull Optional<String> getExtension(@NonNull Path path)
    {
        var filename = path.getFileName();
        if(filename == null)
            return Optional.empty();
        
        return getExtension(path.toString());
    }
    
    public boolean hasExtension(@NonNull String filename) { return getExtension(filename).isPresent(); }
    public boolean hasExtension(@NonNull Path path) { return getExtension(path).isPresent(); }
    
    public boolean hasExtension(@NonNull String filename, String extension, boolean ignoreCase)
    {
        var ext = getExtension(filename);
        if(ignoreCase)
            return ext.isPresent() && ext.get().equalsIgnoreCase(extension);
        return ext.isPresent() && ext.get().equals(extension);
    }
    public boolean hasExtension(@NonNull String filename, String extension) { return hasExtension(filename, extension, false); }
    
    public boolean hasExtension(@NonNull Path path, String extension, boolean ignoreCase)
    {
        var ext = getExtension(path);
        if(ignoreCase)
            return ext.isPresent() && ext.get().equalsIgnoreCase(extension);
        return ext.isPresent() && ext.get().equals(extension);
    }
    public boolean hasExtension(@NonNull Path path, String extension) { return hasExtension(path, extension, false); }
    
    
    public @NonNull Path removeExtension(@NonNull Path path)
    {
        var pathFilename = path.getFileName();
        if(pathFilename == null)
            return path;
        
        var filename = pathFilename.toString();
        var chars = filename.toCharArray();
        int lastDot = -1;
        for(int i = 0; i < chars.length; ++i)
        {
            if(chars[i] == '.')
                lastDot = i;
        }
        
        if(lastDot < 0 || (chars.length - lastDot) <= 1)
            return path;
        
        var newFilename = filename.substring(0, lastDot);
        var parent = path.getParent();
        if(parent == null)
            return Path.of(newFilename);
        return parent.resolve(newFilename);
    }
    
    public @NonNull Path changeExtension(@NonNull Path path, String extension)
    {
        if(extension == null || extension.isBlank())
            return removeExtension(path);
        
        if(extension.charAt(0) != '.')
            extension = '.' + extension;
        
        var pathFilename = path.getFileName();
        if(pathFilename == null)
            return path;
        
        var filename = pathFilename.toString();
        var chars = filename.toCharArray();
        int lastDot = -1;
        for(int i = 0; i < chars.length; ++i)
        {
            if(chars[i] == '.')
                lastDot = i;
        }
        
        String newFilename;
        if(lastDot < 0 || (chars.length - lastDot) <= 1)
            newFilename = filename;
        else
            newFilename = filename.substring(0, lastDot);
        
        newFilename += extension;
        
        var parent = path.getParent();
        if(parent == null)
            return Path.of(newFilename);
        return parent.resolve(newFilename);
    }
}
