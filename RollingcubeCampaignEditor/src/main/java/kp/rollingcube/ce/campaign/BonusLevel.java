package kp.rollingcube.ce.campaign;

import java.io.IOException;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public final class BonusLevel extends IndexedLevel
{
    BonusLevel(Episode episode)
    {
        super(episode);
    }

    @Override
    public @NonNull LevelType getType() { return LevelType.BONUS; }

    @Override
    public @NonNull String getLabel() { return "Bonus level " + index; }

    @Override
    public @NonNull String getFilenameWithoutExtension()
    {
        return Integer.toString(index + 1);
    }
    
    @Override
    public void swapWith(int otherIndex) throws IOException
    {
        episode.swapBonusLevels(index, otherIndex);
    }
    
    public void swapWith(BonusLevel level) throws IllegalArgumentException, IOException
    {
        if(!episode.equals(level.episode))
            throw new IllegalArgumentException("Both levels must belong to the same episode");
        swapWith(level.index);
    }

    @Override
    public void remove() throws IllegalArgumentException, IOException
    {
        episode.removeBonusLevel(index);
    }
}
