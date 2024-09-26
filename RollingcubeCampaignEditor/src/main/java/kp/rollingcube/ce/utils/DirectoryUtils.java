package kp.rollingcube.ce.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class DirectoryUtils
{
    public boolean isEmpty(Path path)
    {
        if(Files.isDirectory(path))
        {
            try(var entries = Files.list(path))
            {
                return entries.findFirst().isEmpty();
            }
            catch(IOException ex) { return false; }
        }

        return false;
    }
    
    public boolean isEmptyOrHasFile(Path path, String filename) throws IOException
    {
        if(Files.isDirectory(path))
        {
            var result = Files.list(path)
                    .filter(p -> p.getFileName().toString().equals(filename))
                    .findFirst();
            if(result.isPresent())
                return true;
            
            return Files.list(path).findFirst().isEmpty();
        }
        return false;
    }
}
