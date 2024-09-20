package kp.rollingcube.ce.campaign;

/**
 *
 * @author Marc
 */
public enum LevelType
{
    NORMAL,
    BONUS,
    SECRET;
    
    public final String getFolderName()
    {
        switch(this)
        {
            case NORMAL: return "Normal";
            case BONUS: return "Bonus";
            case SECRET: return "Secret";
            default: throw new IllegalStateException();
        }
    }
}
