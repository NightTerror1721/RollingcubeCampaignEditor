package kp.rollingcube.ce.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;

/**
 *
 * @author Marc
 */
public class LevelLocation
{
    @Getter private final String name;
    @Getter private final Path levelPath;
    @Getter private final Path thumbnailPath;
    
    private LevelLocation(String name, Path levelPath, Path thumbnailPath)
    {
        this.name = name;
        this.levelPath = levelPath;
        this.thumbnailPath = thumbnailPath;
    }
    
    public boolean isInvalid() { return name == null; }
    public boolean isValid() { return name != null; }
    
    public boolean hasLevelPath() { return levelPath != null; }
    public boolean hasThumbnailPath() { return thumbnailPath != null; }
    
    public static LevelLocation find(Path path)
    {
        Path levelPath = PathUtils.changeExtension(path, "json");
        Path thumbnailPath = PathUtils.changeExtension(path, "png");
        
        if(!Files.isRegularFile(levelPath)) levelPath = null;
        if(!Files.isRegularFile(thumbnailPath)) thumbnailPath = null;
        
        if(levelPath == null && thumbnailPath == null)
            return new LevelLocation(null, null, null);
        
        String name = PathUtils.removeExtension(path).getFileName().toString();
        return new LevelLocation(name, levelPath, thumbnailPath);
    }
}
