package kp.rollingcube.ce.ui;

import java.awt.Window;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import kp.rollingcube.ce.utils.GlobalProperties;
import kp.rollingcube.ce.utils.IOUtils;
import kp.rollingcube.ce.utils.PathUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Marc
 */
@UtilityClass
public final class FileChooser
{
    private Optional<Path> preparePath(File file)
    {
        if(file == null)
            return Optional.empty();
        
        var path = file.toPath();
        return Optional.of(path);
    }
    
    private final Path DEFAULT_CURRENT_PATH = IOUtils.getUserDirectory();
    private Path getCurrentPath(String fcId)
    {
        var tag = "paths.fc." + fcId;
        return GlobalProperties.getPath(tag, DEFAULT_CURRENT_PATH);
    }
    private void setCurrentPath(String fcId, JFileChooser fc)
    {
        var tag = "paths.fc." + fcId;
        var path = fc.getCurrentDirectory() == null ? null : fc.getCurrentDirectory().toPath();
        GlobalProperties.set(tag, path);
    }
    
    private JFileChooser CAMPAIGN_LOAD_FC;
    private JFileChooser campaignLoadFileChooser()
    {
        if(CAMPAIGN_LOAD_FC == null)
        {
            CAMPAIGN_LOAD_FC = new JFileChooser();
            CAMPAIGN_LOAD_FC.setAcceptAllFileFilterUsed(false);
            CAMPAIGN_LOAD_FC.setFileHidingEnabled(true);
            CAMPAIGN_LOAD_FC.setMultiSelectionEnabled(false);
            CAMPAIGN_LOAD_FC.setCurrentDirectory(getCurrentPath("campaign_load").toFile());
            CAMPAIGN_LOAD_FC.setFileSelectionMode(JFileChooser.FILES_ONLY);
            CAMPAIGN_LOAD_FC.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    var path = f.toPath();
                    if(Files.isDirectory(path))
                        return true;

                    return path.getFileName().toString().equals("campaign.json");
                }

                @Override
                public String getDescription() { return "Rollingcube Campaign (campaign.json)"; }
            });
        }
        return CAMPAIGN_LOAD_FC;
    }
    
    public @NonNull Optional<Path> openCampaign(Window parent)
    {
        var fc = campaignLoadFileChooser();
        try
        {
            if(fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
                return preparePath(fc.getSelectedFile());
            return Optional.empty();
        }
        finally { setCurrentPath("campaign_load", fc); }
    }
    
    private JFileChooser CAMPAIGN_SAVE_FC;
    private JFileChooser campaignSaveFileChooser()
    {
        if(CAMPAIGN_SAVE_FC == null)
        {
            CAMPAIGN_SAVE_FC = new JFileChooser();
            CAMPAIGN_SAVE_FC.setAcceptAllFileFilterUsed(false);
            CAMPAIGN_SAVE_FC.setFileHidingEnabled(true);
            CAMPAIGN_SAVE_FC.setMultiSelectionEnabled(false);
            CAMPAIGN_SAVE_FC.setCurrentDirectory(getCurrentPath("campaign_save").toFile());
            CAMPAIGN_SAVE_FC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            CAMPAIGN_SAVE_FC.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    var path = f.toPath();
                    try { return Files.isDirectory(path); }
                    catch(Exception ex) { return false; }
                }

                @Override
                public String getDescription() { return "Rollingcube Campaign folder"; }
            });
        }
        return CAMPAIGN_SAVE_FC;
    }
    
    public @NonNull Optional<Path> saveCampaign(Window parent, Path currentCampaignPath)
    {
        var fc = campaignSaveFileChooser();
        
        if(currentCampaignPath != null)
        {
            if(Files.isDirectory(currentCampaignPath))
            {
                var pathParent = currentCampaignPath.getParent();
                if(pathParent != null)
                    fc.setCurrentDirectory(pathParent.toFile());
                else
                    fc.setCurrentDirectory(currentCampaignPath.toFile());
                fc.setSelectedFile(currentCampaignPath.toFile());
            }
            else
            {
                var pathParent = currentCampaignPath.getParent();
                if(pathParent != null)
                    fc.setCurrentDirectory(pathParent.toFile());
                fc.setSelectedFile(null);
            }
        }
        
        try
        {
            if(fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
                return preparePath(fc.getSelectedFile());
            return Optional.empty();
        }
        finally { setCurrentPath("campaign_save", fc); }
    }
    
    
    private JFileChooser LEVEL_LOAD_FC;
    private JFileChooser levelLoadFileChooser()
    {
        if(LEVEL_LOAD_FC == null)
        {
            LEVEL_LOAD_FC = new JFileChooser();
            LEVEL_LOAD_FC.setAcceptAllFileFilterUsed(false);
            LEVEL_LOAD_FC.setFileHidingEnabled(true);
            LEVEL_LOAD_FC.setMultiSelectionEnabled(true);
            LEVEL_LOAD_FC.setFileSelectionMode(JFileChooser.FILES_ONLY);
            LEVEL_LOAD_FC.setCurrentDirectory(getCurrentPath("level_load").toFile());
            LEVEL_LOAD_FC.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    var path = f.toPath();
                    if(Files.isDirectory(path))
                        return true;

                    return path.getFileName().toString().endsWith(".json");
                }

                @Override
                public String getDescription() { return "Rollingcube Level (.json)"; }
                
            });
        }
        return LEVEL_LOAD_FC;
    }
    
    public @NonNull List<Path> openLevelsFileChooser(Window parent)
    {
        var fc = levelLoadFileChooser();
        if(fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return List.of();
        
        try
        {
            var files = fc.getSelectedFiles();
            if(files == null)
                return List.of();

            return Stream.of(files)
                    .map(File::toPath)
                    .filter(path -> Files.isRegularFile(path))
                    .map(path -> PathUtils.removeExtension(path).toAbsolutePath())
                    .distinct()
                    .collect(Collectors.toList());
        }
        finally { setCurrentPath("level_load", fc); }
    }
    
    
    private JFileChooser THUMBNAIL_LOAD_FC;
    private JFileChooser thumbnailFileChooser()
    {
        if(THUMBNAIL_LOAD_FC == null)
        {
            THUMBNAIL_LOAD_FC = new JFileChooser();
            THUMBNAIL_LOAD_FC.setAcceptAllFileFilterUsed(false);
            THUMBNAIL_LOAD_FC.setFileHidingEnabled(true);
            THUMBNAIL_LOAD_FC.setMultiSelectionEnabled(false);
            THUMBNAIL_LOAD_FC.setFileSelectionMode(JFileChooser.FILES_ONLY);
            THUMBNAIL_LOAD_FC.setCurrentDirectory(getCurrentPath("thumbnail_load").toFile());
            THUMBNAIL_LOAD_FC.setFileFilter(new FileFilter()
            {
                @Override
                public boolean accept(File f)
                {
                    var path = f.toPath();
                    if(Files.isDirectory(path))
                        return true;

                    return path.getFileName().toString().endsWith(".png");
                }

                @Override
                public String getDescription() { return "PNG Image (.png)"; }
                
            });
        }
        return THUMBNAIL_LOAD_FC;
    }
    
    public @NonNull Optional<Path> openThumbnailFileChooser(Window parent)
    {
        var fc = thumbnailFileChooser();
        try
        {
            if(fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
                return Optional.empty();

            return preparePath(fc.getSelectedFile());
        }
        finally { setCurrentPath("thumbnail_load", fc); }
    }
}
