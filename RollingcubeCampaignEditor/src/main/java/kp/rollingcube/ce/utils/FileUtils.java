package kp.rollingcube.ce.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class FileUtils
{
    public void swap(Path f1, Path f2) throws IOException
    {
        if(Files.isRegularFile(f1))
        {
            if(Files.isRegularFile(f2))
            {
                var temp = Files.createTempFile(null, null);
                Files.copy(f1, temp, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(f2, f1, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(temp, f2, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(temp);
            }
            else
            {
                Files.copy(f1, f2, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(f1);
            }
        }
        else if(Files.isRegularFile(f2))
        {
            Files.copy(f2, f1, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(f2);
        }
    }
    
    public boolean isDirectoryEmpty(@NonNull Path path) throws IOException
    {
        if(Files.isDirectory(path))
        {
            try(var it = Files.newDirectoryStream(path))
            {
                return !it.iterator().hasNext();
            }
        }
        return false;
    }
}
