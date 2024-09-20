package kp.rollingcube.ce.campaign;

import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Marc
 */
public final class SecretLevel extends Level
{
    @Getter @Setter(AccessLevel.PACKAGE)
    private @NonNull String name;
    
    @Getter @Setter
    private String alias;
    
    @Getter @Setter private boolean oneTry;
    @Getter @Setter private boolean penalty;
    
    SecretLevel(Episode episode)
    {
        super(episode);
    }
    
    @Override
    public @NonNull LevelType getType() { return LevelType.SECRET; }

    @Override
    public @NonNull String getLabel() { return name; }

    @Override
    protected @NonNull String getFilenameWithoutExtension() { return name; }

    @Override
    public void remove() throws IllegalArgumentException, IOException
    {
        episode.removeSecretLevel(name);
    }
    
    public void changeName(String name) throws IOException
    {
        episode.changeSecretLevelName(this.name, name);
    }
}
