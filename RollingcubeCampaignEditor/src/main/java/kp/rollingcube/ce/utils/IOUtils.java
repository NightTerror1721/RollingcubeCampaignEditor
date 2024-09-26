package kp.rollingcube.ce.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class IOUtils
{
    public @NonNull String readAll(@NonNull Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[8192];
        int len;
        
        while((len = reader.read(buffer, 0, buffer.length)) > 0)
            sb.append(buffer, 0, len);
        
        return sb.toString();
    }
    
    public @NonNull String readAll(@NonNull InputStream in) throws IOException
    {
        return readAll(new InputStreamReader(in));
    }
    
    public @NonNull byte[] readAllBytes(@NonNull InputStream in) throws IOException
    {
        var baos = new ByteArrayOutputStream(8192);
        byte[] buffer = new byte[8192];
        int len;
        
        while((len = in.read(buffer, 0, buffer.length)) > 0)
            baos.write(buffer, 0, len);
        
        return baos.toByteArray();
    }
    
    public @NonNull byte[] readAllBytesFromFile(@NonNull Path filePath) throws IOException
    {
        try(var is = Files.newInputStream(filePath))
        {
            return readAllBytes(is);
        }
    }
    
    public @NonNull String readAllFromFile(@NonNull Path filePath, Charset charset) throws IOException
    {
        return Files.readString(filePath, charset);
    }
    public @NonNull String readAllFromFile(@NonNull Path filePath) throws IOException
    {
        return Files.readString(filePath);
    }
    
    public BufferedImage readImage(@NonNull Path filePath) throws IOException
    {
        try(var is = Files.newInputStream(filePath))
        {
            return ImageIO.read(is);
        }
    }
    
    public void writeAllBytes(@NonNull OutputStream os, byte[] data) throws IOException
    {
        int offset = 0;
        while(offset < data.length)
        {
            int len = Math.min(8192, data.length - offset);
            os.write(data, offset, len);
            offset += len;
        }
    }
    
    public void writeAllBytesToFile(@NonNull Path filePath, byte[] data) throws IOException
    {
        try(var os = Files.newOutputStream(filePath))
        {
            writeAllBytes(os, data);
        }
    }
    
    public void writeToFile(@NonNull Path filePath, @NonNull String text, Charset charset) throws IOException
    {
        Files.writeString(filePath, text, charset);
    }
    public void writeToFile(@NonNull Path filePath, @NonNull String text) throws IOException
    {
        Files.writeString(filePath, text);
    }
    
    public void writeImage(@NonNull Path filePath, @NonNull BufferedImage image) throws IOException
    {
        try(var os = Files.newOutputStream(filePath))
        {
            ImageIO.write(image, "png", os);
        }
    }
    
    
    public @NonNull Path getUserDirectory()
    {
        return Path.of(System.getProperty("user.dir")).toAbsolutePath();
    }
    
    public @NonNull Path getHomeDirectory()
    {
        return Path.of(System.getProperty("user.home")).toAbsolutePath();
    }
    
    public @NonNull String getFileName(@NonNull Path file)
    {
        var name = file.getFileName().toString();
        int index = name.lastIndexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }
    
    public @NonNull String getFileExtension(@NonNull Path file)
    {
        var name = file.getFileName().toString();
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1);
    }
    
    public @NonNull Path concatElementAtPathEnd(@NonNull Path path, @NonNull String element)
    {
        if(path.getParent() == null)
            return Paths.get(path.getFileName().toString() + element);
        return path.getParent().resolve(path.getFileName().toString() + element);
    }
    
    public @NonNull URL getClasspathResourceUrl(@NonNull String path)
    {
        if(!path.startsWith("/"))
            path = "/" + path;
        return IOUtils.class.getResource(path);
    }
    
    public @NonNull InputStream getClasspathResourceAsStream(@NonNull String path)
    {
        if(!path.startsWith("/"))
            path = "/" + path;
        return IOUtils.class.getResourceAsStream(path);
    }
    
    
    public BufferedImage loadImage(Path path)
    {
        try(var is = Files.newInputStream(path))
        {
            return ImageIO.read(is);
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
            return null;
        }
    }
}
