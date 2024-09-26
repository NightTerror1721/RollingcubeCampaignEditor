package kp.rollingcube.ce.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import javax.imageio.ImageIO;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
public final class Thumbnail
{
    private final AtomicReference<BufferedImage> image = new AtomicReference<>(null);
    private final @NonNull byte[] data;
    private final @NonNull IntSupplier widthGet;
    private final @NonNull IntSupplier heightGet;
    private final Consumer<Thumbnail> onLoadFinishedCallback;
    
    public Thumbnail(
            byte[] data,
            @NonNull IntSupplier widthGet,
            @NonNull IntSupplier heightGet,
            Consumer<Thumbnail> onLoadFinishedCallback)
    {
        this.data = data;
        this.widthGet = widthGet;
        this.heightGet = heightGet;
        this.onLoadFinishedCallback = onLoadFinishedCallback;
        
        if(data != null)
        {
            var thread = new Thread(this::loadImage);
            thread.start();
        }
    }
    
    public Thumbnail(
            Optional<byte[]> data,
            @NonNull IntSupplier widthGet,
            @NonNull IntSupplier heightGet,
            Consumer<Thumbnail> onLoadFinishedCallback)
    {
        this(data.orElse(null), widthGet, heightGet, onLoadFinishedCallback);
    }
    
    public void draw(Graphics g, ImageObserver observer)
    {
        var bi = image.get();
        if(bi != null)
            g.drawImage(bi, 0, 0, widthGet.getAsInt(), heightGet.getAsInt(), observer);
    }
    
    private void loadImage()
    {
        try(var bais = new ByteArrayInputStream(data))
        {
            if(data == null)
                return;
            
            image.set(ImageIO.read(bais));
        }
        catch(IOException ex)
        {
            
        }
        finally
        {
            if(onLoadFinishedCallback != null)
                onLoadFinishedCallback.accept(this);
        }
    }
}
