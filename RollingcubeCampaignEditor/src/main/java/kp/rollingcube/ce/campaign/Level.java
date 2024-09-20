package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import kp.rollingcube.ce.utils.LevelLocation;
import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public abstract class Level
{
    @Getter
    protected final @NonNull Episode episode;
    
    Level(Episode episode)
    {
        this.episode = episode;
    }
    
    public abstract @NonNull LevelType getType();
    
    public abstract @NonNull String getLabel();
    
    protected abstract @NonNull String getFilenameWithoutExtension();
    
    public abstract void remove() throws IllegalArgumentException, IOException;
    
    public final @NonNull Path getLevelPath() { return getPath(".json"); }
    public final @NonNull Path getThumbnailPath() { return getPath(".png"); }
    
    public final boolean existsLevelFile() { return Files.isRegularFile(getLevelPath()); }
    public final boolean existsThumbnailFile() { return Files.isRegularFile(getThumbnailPath()); }
    
    public void replaceLevelFiles(Path levelPath) throws IllegalArgumentException, IOException
    {
        var location = LevelLocation.find(levelPath);
        if(location.isInvalid() || !location.hasLevelPath())
            throw new IllegalArgumentException(String.format("Level %s not exists or it's not valid level", levelPath));
        
        episode.ensureFolder(getType());
        Files.copy(location.getLevelPath(), getLevelPath(), StandardCopyOption.REPLACE_EXISTING);
        
        if(location.hasThumbnailPath())
            Files.copy(location.getThumbnailPath(), getThumbnailPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    private Path getPath(String extension)
    {
        return episode.getPath()
                .resolve(getType().getFolderName())
                .resolve(getFilenameWithoutExtension() + extension);
    }
    
    
    public final boolean isNormal() { return getType() == LevelType.NORMAL; }
    public final boolean isBonus() { return getType() == LevelType.BONUS; }
    public final boolean isSecret() { return getType() == LevelType.SECRET; }
    
    public final NormalLevel asNormal() { return (NormalLevel) this; }
    public final BonusLevel asBonus() { return (BonusLevel) this; }
    public final SecretLevel asSecret() { return (SecretLevel) this; }
}
