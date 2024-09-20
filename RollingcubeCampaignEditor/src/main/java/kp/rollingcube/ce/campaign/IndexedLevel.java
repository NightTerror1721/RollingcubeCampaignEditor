package kp.rollingcube.ce.campaign;

import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Marc
 */
public abstract class IndexedLevel extends Level
{
    @Getter @Setter(AccessLevel.PACKAGE)
    protected int index;
    
    IndexedLevel(Episode episode)
    {
        super(episode);
    }
    
    public abstract void swapWith(int otherIndex) throws IOException;
}
