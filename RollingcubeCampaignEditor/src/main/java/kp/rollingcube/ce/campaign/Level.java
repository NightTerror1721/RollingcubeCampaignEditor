package kp.rollingcube.ce.campaign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
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
    
    private final LevelData data = new LevelData();
    
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
    public final @NonNull LevelLocation getLocation() { return LevelLocation.find(getPath(null)); }
    
    public final boolean existsLevelFile() { return Files.isRegularFile(getLevelPath()); }
    public final boolean existsThumbnailFile() { return Files.isRegularFile(getThumbnailPath()); }
    
    public final Optional<String> getData() { return Optional.ofNullable(data.getData()); }
    public final Optional<byte[]> getThumbnail() { return Optional.ofNullable(data.getThumbnail()); }
    
    public final boolean hasFruit() { return data.hasFruit(); }
    public final Optional<String> getSecretExitLevelTag() { return data.getSecretExitLevelTag(); }
    
    public final void loadExternData(Path path) throws IOException { data.loadExternLevel(path); }
    
    final void read(CampaingLoadSaveState state) throws IOException { data.read(this, state); }
    final void write(CampaingLoadSaveState state) throws IOException { data.write(this, state); }
    
    final void prepareLoadState(CampaingLoadSaveState state) { data.prepareLoadState(state); }
    final void prepareSaveState(CampaingLoadSaveState state) { data.prepareSaveState(state); }
    
    private Path getPath(String extension)
    {
        var filename = extension == null ? getFilenameWithoutExtension() : (getFilenameWithoutExtension() + extension);
        return episode.getPath()
                .resolve(getType().getFolderName())
                .resolve(filename);
    }
    
    
    public final boolean isNormal() { return getType() == LevelType.NORMAL; }
    public final boolean isBonus() { return getType() == LevelType.BONUS; }
    public final boolean isSecret() { return getType() == LevelType.SECRET; }
    
    public final NormalLevel asNormal() { return (NormalLevel) this; }
    public final BonusLevel asBonus() { return (BonusLevel) this; }
    public final SecretLevel asSecret() { return (SecretLevel) this; }
}
