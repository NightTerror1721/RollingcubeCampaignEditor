package kp.rollingcube.ce.ui;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import kp.rollingcube.ce.campaign.Campaign;
import kp.rollingcube.ce.campaign.Episode;
import kp.rollingcube.ce.campaign.LevelType;
import kp.rollingcube.ce.utils.Thumbnail;
import kp.rollingcube.ce.utils.UIUtils;
import kp.rollingcube.ce.utils.Version;

/**
 *
 * @author Marc
 */
public class CampaignEditor extends JFrame implements ChangesNotifier
{
    private Campaign campaign;
    private Path campaignPath;
    private Thumbnail campaignThumbnail;
    
    private Episode selectedEpisode;
    private Thumbnail selectedEpisodeThumbnail;
    
    private boolean unstoredChanges;
    
    private final JPanel episodesPanel = new JPanel();
    private final HashMap<String, EpisodeCard> episodeCardsMap = new HashMap<>();
    
    private final LevelsPanel normalLevelsPanel = new LevelsPanel(this, LevelType.NORMAL);
    private final LevelsPanel bonusLevelsPanel = new LevelsPanel(this, LevelType.BONUS);
    private final LevelsPanel secretLevelsPanel = new LevelsPanel(this, LevelType.SECRET);
    
    private StringTextFieldManager campaignName;
    private IntegerTextFieldManager campaignRequiredFruits;
    private IntegerTextFieldManager campaignLevelsUntilSaveGame;
    
    private CampaignEditor()
    {
        initComponents();
        init();
    }
    
    private void init()
    {
        episodesPanel.setLayout(new GridBagLayout());
        episodesScrollPanel.getVerticalScrollBar().setUnitIncrement(30);
        
        normalLevelsRootPanel.add(normalLevelsPanel);
        bonusLevelsRootPanel.add(bonusLevelsPanel);
        secretLevelsRootPanel.add(secretLevelsPanel);
        
        UIUtils.setIcon(this);
        UIUtils.focus(this);
        
        campaignName = new StringTextFieldManager(
                campaignNameField,
                value -> campaign.setName(value),
                () -> campaign.getName(),
                this
        );
        
        campaignRequiredFruits = new IntegerTextFieldManager(
                campaignFruitsField,
                value -> campaign.setRequiredFruitsToBonus(value),
                () -> campaign.getRequiredFruitsToBonus(),
                this
        );
        campaignRequiredFruits.setMinValue(0);
        campaignRequiredFruits.setMaxValue(5);
        
        campaignLevelsUntilSaveGame = new IntegerTextFieldManager(
                campaignLevelsSaveGameField,
                value -> campaign.setLevelsUntilSaveGame(value),
                () -> campaign.getLevelsUntilSaveGame(),
                this
        );
        campaignLevelsUntilSaveGame.setMinValue(0);
        campaignLevelsUntilSaveGame.setMaxValue(Integer.MAX_VALUE);
        
        newCampaign(true);
    }
    
    public static void open()
    {
        UIUtils.useSystemLookAndFeel();
        var editor = new CampaignEditor();
        editor.setVisible(true);
    }
    
    @Override
    public void notifyChanges() { unstoredChanges = true; }
    
    private void updateProperties()
    {
        campaignName.bind();
        campaignRequiredFruits.bind();
        campaignLevelsUntilSaveGame.bind();
        updateCampaignThumbnail();
        updateEpisodesPanel();
        updateSelectedEpisodePanel();
        updateTitle();
    }
    
    private void updateCampaignThumbnail()
    {
        if(!campaign.hasThumbnail())
        {
            campaignThumbnail = null;
            campaignThumbnailPanel.repaint();
        }
        else
        {
            campaignThumbnail = new Thumbnail(
                    campaign.getThumbnail(),
                    campaignThumbnailPanel::getWidth,
                    campaignThumbnailPanel::getHeight,
                    image -> campaignThumbnailPanel.repaint()
            );
        }
    }
    
    private void updateEpisodesPanel()
    {
        episodeCardsMap.clear();
        episodesPanel.removeAll();
        if(campaign == null)
            return;
        
        int lastIndex = 0;
        for(var episode : campaign.getEpisodes())
        {
            var episodePanel = new EpisodeCard();
            episodePanel.setEpisode(episode);
            episodePanel.setListener(new EpisodeCard.EventsListener()
            {
                @Override
                public void onEdit(Episode episode) { selectEpisode(episode); }

                @Override
                public void onDelete(Episode episode) { deleteEpisode(episode); }

                @Override
                public void onUp(Episode episode) { moveUpEpisode(episode); }

                @Override
                public void onDown(Episode episode) { moveDownEpisode(episode); }
                
            });
            episodePanel.update();
            episodesPanel.add(episodePanel, prepareEpisodesConstraints(episode.getIndex()));
            episodeCardsMap.put(episode.getName(), episodePanel);
            lastIndex = Math.max(lastIndex, episode.getIndex());
        }
        episodesPanel.add(new JPanel(), prepareEpisodesGhostConstraints(lastIndex + 1));
        episodesPanel.updateUI();
    }
    
    private GridBagConstraints prepareEpisodesConstraints(int index)
    {
        var ct = new GridBagConstraints();
        ct.fill = GridBagConstraints.HORIZONTAL;
        ct.anchor = GridBagConstraints.NORTHWEST;
        ct.weightx = 1;
        ct.weighty = 1;
        ct.gridx = 0;
        ct.gridy = index;
        return ct;
    }
    
    private GridBagConstraints prepareEpisodesGhostConstraints(int lastIndex)
    {
        var ct = prepareEpisodesConstraints(lastIndex);
        ct.weighty = 100;
        return ct;
    }
    
    private void updateSelectedEpisodePanel()
    {
        if(selectedEpisode == null)
        {
            selectedEpisodeNameField.setText("");
            
            normalLevelsPanel.setEpisode(null);
            bonusLevelsPanel.setEpisode(null);
            secretLevelsPanel.setEpisode(null);
            
            normalLevelsPanel.update();
            bonusLevelsPanel.update();
            secretLevelsPanel.update();
            
            updateSelectedEpisodeThumbnail();
            
            episodeViewPanel.setVisible(false);
        }
        else
        {
            selectedEpisodeNameField.setText(selectedEpisode.getName());
            
            normalLevelsPanel.setEpisode(selectedEpisode);
            bonusLevelsPanel.setEpisode(selectedEpisode);
            secretLevelsPanel.setEpisode(selectedEpisode);
            
            normalLevelsPanel.update();
            bonusLevelsPanel.update();
            secretLevelsPanel.update();
            
            updateSelectedEpisodeThumbnail();
            
            episodeViewPanel.setVisible(true);
        }
    }
    
    private void updateSelectedEpisodeThumbnail()
    {
        if(selectedEpisode == null || !selectedEpisode.hasThumbnail())
        {
            selectedEpisodeThumbnail = null;
            selectedEpisodeThumbnailPanel.repaint();
        }
        else
        {
            selectedEpisodeThumbnail = new Thumbnail(
                    selectedEpisode.getThumbnail(),
                    selectedEpisodeThumbnailPanel::getWidth,
                    selectedEpisodeThumbnailPanel::getHeight,
                    image -> selectedEpisodeThumbnailPanel.repaint()
            );
        }
    }
    
    private void updateTitle()
    {
        if(campaign == null)
            setTitle(String.format("Rollingcube Campaign Editor v%s", Version.APP_VERSION));
        else
            setTitle(String.format("Rollingcube Campaign Editor v%s - %s", Version.APP_VERSION, campaign.getName()));
    }
    
    private void selectedEpisodeChangeName()
    {
        if(selectedEpisode == null)
            return;
        
        var name = Notify.askName(this, "Change Episode Name", "Enter a new name for the episode");
        if(name.isEmpty())
            return;
        
        try
        {
            campaign.changeEpisodeName(selectedEpisode.getName(), name.get());
            updateEpisodesPanel();
            updateSelectedEpisodePanel();
            notifyChanges();
        }
        catch(IOException ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while changing the episode name: %s",
                    ex.getLocalizedMessage()
            );
        }
    }
    
    private void selectEpisode(Episode episode)
    {
        selectedEpisode = episode;
        updateSelectedEpisodePanel();
    }
    
    private void deleteEpisode(Episode episode)
    {
        if(!Notify.ask(
                this,
                "Delete Episode",
                String.format("Are you sure you want to delete the %s episode?",
                        episode.getName())
        )) return;
        
        try
        {
            if(selectedEpisode != null && selectedEpisode.equals(episode))
                selectEpisode(null);
            campaign.removeEpisode(episode.getName());
            notifyChanges();
        }
        catch(Exception ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while deleting episode: %s",
                    ex.getLocalizedMessage()
            );
        }
        finally { updateEpisodesPanel(); }
    }
    
    private void moveUpEpisode(Episode episode)
    {
        int index = episode.getIndex();
        if(index <= 0)
            return;
        
        try
        {
            campaign.swapEpisodes(index, index - 1);
            notifyChanges();
        }
        catch(Exception ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while episode move up: %s",
                    ex.getLocalizedMessage()
            );
        }
        finally { updateEpisodesPanel(); }
    }
    
    private void moveDownEpisode(Episode episode)
    {
        int index = episode.getIndex();
        if(index >= campaign.getEpisodesCount() - 1)
            return;
        
        try
        {
            campaign.swapEpisodes(index, index + 1);
            notifyChanges();
        }
        catch(Exception ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while episode move down: %s",
                    ex.getLocalizedMessage()
            );
        }
        finally { updateEpisodesPanel(); }
    }
    
    private void createNewEpisode()
    {
        var name = Notify.askName(this, "New Episode Name", "Enter a name for the new episode");
        if(name.isEmpty())
            return;
        
        try
        {
            campaign.addEpisode(name.get());
            updateEpisodesPanel();
            notifyChanges();
        }
        catch(IOException ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while creating the new episode: %s",
                    ex.getLocalizedMessage()
            );
        }
    }
    
    private boolean newCampaign(boolean ignoreUnstoredChanges)
    {
        if(!ignoreUnstoredChanges && !resolveUnstoredChanges())
            return false;
        
        campaign = Campaign.createNew();
        campaign.setName("Unnamed");
        campaignPath = null;
        unstoredChanges = false;
        selectedEpisode = null;
        updateProperties();
        return true;
    }
    
    private boolean loadCampaign()
    {
        if(!resolveUnstoredChanges())
            return false;
        
        var path = FileChooser.openCampaign(this);
        if(path.isEmpty())
            return false;
        
        try
        {
            var cp = CampaignLoader.loadCampaign(this, path.get());
            campaign = cp;
            campaignPath = cp.getPath();
            unstoredChanges = false;
            selectedEpisode = null;
            updateProperties();
            return true;
        }
        catch(Exception ex)
        {
            Notify.error(this, ex.getLocalizedMessage());
            return false;
        }
    }
    
    private boolean saveCampaign(boolean askForPath)
    {
        if(!validateProject())
            return false;
        
        var oldPath = campaignPath;
        if(askForPath || campaignPath == null)
        {
            var oPath = FileChooser.saveCampaign(this, campaignPath);
            if(oPath.isEmpty())
                return false;
            
            campaignPath = oPath.get();
        }
        
        try
        {
            CampaignLoader.saveCampaign(this, campaign, campaignPath);
            unstoredChanges = false;
            return true;
        }
        catch(Exception ex)
        {
            campaignPath = oldPath;
            Notify.error(this, ex.getLocalizedMessage());
            return false;
        }
    }
    
    private boolean validateProject()
    {
        if(!campaignName.isValid())
        {
            Notify.error(this, "The campaign name must be valid");
            return false;
        }
        if(!campaignRequiredFruits.isValid())
        {
            Notify.error(this, "The \"required fruits to bonus\" field must be a number between 0 and 5.");
            return false;
        }
        if(!campaignLevelsUntilSaveGame.isValid())
        {
            Notify.error(this, "The \"levels until save game\" field must be a number equal to or greater than 0.");
            return false;
        }
        
        if(campaign.getEpisodesCount() <= 0)
        {
            Notify.error(this, "There must be at least one episode in the campaign.");
            return false;
        }
        
        return true;
    }
    
    private boolean resolveUnstoredChanges()
    {
        if(!unstoredChanges)
            return true;
        
        var result = Notify.cancelableAsk(this, "Save changes", "Do you want to save pending changes?");
        switch(result)
        {
            case YES: return saveCampaign(false);
            case NO: return true;
            default: return false;
        }
    }
    
    private void closeApp()
    {
        if(!resolveUnstoredChanges())
            return;
        
        System.exit(0);
    }
    
    private void drawCampaignThumbnail(Graphics g)
    {
        if(campaignThumbnail != null)
            campaignThumbnail.draw(g, campaignThumbnailPanel);
    }
    
    private void drawSelectedEpisodeThumbnail(Graphics g)
    {
        if(selectedEpisode != null && selectedEpisodeThumbnail != null)
            selectedEpisodeThumbnail.draw(g, selectedEpisodeThumbnailPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        campaignNameField = new javax.swing.JTextField();
        campaignFruitsField = new javax.swing.JTextField();
        campaignLevelsSaveGameField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        campaignThumbnailPanel = new javax.swing.JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCampaignThumbnail(g);
            }
        };
        changeCampaignThumbnailButton = new javax.swing.JButton();
        deleteCampaignThumbnailButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        episodesPanelExtern = new javax.swing.JPanel();
        episodesScrollPanel = new javax.swing.JScrollPane(episodesPanel);
        newEpisodeButton = new javax.swing.JButton();
        episodeViewPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        selectedEpisodeThumbnailPanel = new javax.swing.JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSelectedEpisodeThumbnail(g);
            }
        };
        episodeChangeThumbnailButton = new javax.swing.JButton();
        episodeDeleteThumbnailButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        selectedEpisodeNameField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        normalLevelsRootPanel = new javax.swing.JPanel();
        bonusLevelsRootPanel = new javax.swing.JPanel();
        secretLevelsRootPanel = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        menuNewCampaign = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuOpenCampaign = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuSaveCampaign = new javax.swing.JMenuItem();
        menuSaveAsCampaign = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1066, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Campaign Properties"));

        jLabel1.setText("Campaign Name:");

        jLabel2.setText("Required Fruits to bonus:");

        jLabel3.setText("Levels until save game:");

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Thumbnail"));

        javax.swing.GroupLayout campaignThumbnailPanelLayout = new javax.swing.GroupLayout(campaignThumbnailPanel);
        campaignThumbnailPanel.setLayout(campaignThumbnailPanelLayout);
        campaignThumbnailPanelLayout.setHorizontalGroup(
            campaignThumbnailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 107, Short.MAX_VALUE)
        );
        campaignThumbnailPanelLayout.setVerticalGroup(
            campaignThumbnailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 107, Short.MAX_VALUE)
        );

        changeCampaignThumbnailButton.setText("Change");
        changeCampaignThumbnailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeCampaignThumbnailButtonActionPerformed(evt);
            }
        });

        deleteCampaignThumbnailButton.setText("Delete");
        deleteCampaignThumbnailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCampaignThumbnailButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(campaignThumbnailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changeCampaignThumbnailButton)
                    .addComponent(deleteCampaignThumbnailButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(deleteCampaignThumbnailButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeCampaignThumbnailButton))
                    .addComponent(campaignThumbnailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campaignLevelsSaveGameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campaignFruitsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(campaignNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campaignNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campaignFruitsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(campaignLevelsSaveGameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Campaign Episodes"));

        episodesPanelExtern.setLayout(new java.awt.BorderLayout());

        episodesScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        episodesPanelExtern.add(episodesScrollPanel, java.awt.BorderLayout.CENTER);

        newEpisodeButton.setText("New Episode");
        newEpisodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newEpisodeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(episodesPanelExtern, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newEpisodeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(episodesPanelExtern, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newEpisodeButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        episodeViewPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Episode Properties"));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Thumbnail"));

        javax.swing.GroupLayout selectedEpisodeThumbnailPanelLayout = new javax.swing.GroupLayout(selectedEpisodeThumbnailPanel);
        selectedEpisodeThumbnailPanel.setLayout(selectedEpisodeThumbnailPanelLayout);
        selectedEpisodeThumbnailPanelLayout.setHorizontalGroup(
            selectedEpisodeThumbnailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 76, Short.MAX_VALUE)
        );
        selectedEpisodeThumbnailPanelLayout.setVerticalGroup(
            selectedEpisodeThumbnailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        episodeChangeThumbnailButton.setText("Change");
        episodeChangeThumbnailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                episodeChangeThumbnailButtonActionPerformed(evt);
            }
        });

        episodeDeleteThumbnailButton.setText("Delete");
        episodeDeleteThumbnailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                episodeDeleteThumbnailButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedEpisodeThumbnailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(episodeChangeThumbnailButton)
                    .addComponent(episodeDeleteThumbnailButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(0, 24, Short.MAX_VALUE)
                        .addComponent(episodeDeleteThumbnailButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(episodeChangeThumbnailButton))
                    .addComponent(selectedEpisodeThumbnailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel5.setText("Episode Name:");

        selectedEpisodeNameField.setEditable(false);
        selectedEpisodeNameField.setEnabled(false);

        jButton1.setText("Change Name");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectedEpisodeNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(selectedEpisodeNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        normalLevelsRootPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Normal Levels", normalLevelsRootPanel);

        bonusLevelsRootPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Bonus Levels", bonusLevelsRootPanel);

        secretLevelsRootPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Secret Levels", secretLevelsRootPanel);

        javax.swing.GroupLayout episodeViewPanelLayout = new javax.swing.GroupLayout(episodeViewPanel);
        episodeViewPanel.setLayout(episodeViewPanelLayout);
        episodeViewPanelLayout.setHorizontalGroup(
            episodeViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(episodeViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(episodeViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 893, Short.MAX_VALUE))
                .addContainerGap())
        );
        episodeViewPanelLayout.setVerticalGroup(
            episodeViewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(episodeViewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        jMenu1.setText("File");

        menuNewCampaign.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuNewCampaign.setText("New Campaign");
        menuNewCampaign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewCampaignActionPerformed(evt);
            }
        });
        jMenu1.add(menuNewCampaign);
        jMenu1.add(jSeparator1);

        menuOpenCampaign.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuOpenCampaign.setText("Open Campaign");
        menuOpenCampaign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenCampaignActionPerformed(evt);
            }
        });
        jMenu1.add(menuOpenCampaign);
        jMenu1.add(jSeparator2);

        menuSaveCampaign.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuSaveCampaign.setText("Save");
        menuSaveCampaign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveCampaignActionPerformed(evt);
            }
        });
        jMenu1.add(menuSaveCampaign);

        menuSaveAsCampaign.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuSaveAsCampaign.setText("Save As...");
        menuSaveAsCampaign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveAsCampaignActionPerformed(evt);
            }
        });
        jMenu1.add(menuSaveAsCampaign);
        jMenu1.add(jSeparator3);

        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        jMenu1.add(menuExit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(episodeViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(episodeViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newEpisodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newEpisodeButtonActionPerformed
        createNewEpisode();
    }//GEN-LAST:event_newEpisodeButtonActionPerformed

    private void menuOpenCampaignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenCampaignActionPerformed
        loadCampaign();
    }//GEN-LAST:event_menuOpenCampaignActionPerformed

    private void menuSaveCampaignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveCampaignActionPerformed
        saveCampaign(false);
    }//GEN-LAST:event_menuSaveCampaignActionPerformed

    private void menuSaveAsCampaignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsCampaignActionPerformed
        saveCampaign(true);
    }//GEN-LAST:event_menuSaveAsCampaignActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        closeApp();
    }//GEN-LAST:event_menuExitActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeApp();
    }//GEN-LAST:event_formWindowClosing

    private void menuNewCampaignActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewCampaignActionPerformed
        newCampaign(false);
    }//GEN-LAST:event_menuNewCampaignActionPerformed

    private void changeCampaignThumbnailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeCampaignThumbnailButtonActionPerformed
        var path = FileChooser.openThumbnailFileChooser(this);
        if(path.isEmpty())
            return;
        
        try
        {
            campaign.changeThumbnail(path.get());
            notifyChanges();
        }
        catch(IOException ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while loading campaign thumbnail: %s",
                    ex.getLocalizedMessage()
            );
        }
        finally
        {
            updateCampaignThumbnail();
        }
    }//GEN-LAST:event_changeCampaignThumbnailButtonActionPerformed

    private void deleteCampaignThumbnailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCampaignThumbnailButtonActionPerformed
        campaign.removeThumbnail();
        updateCampaignThumbnail();
        notifyChanges();
    }//GEN-LAST:event_deleteCampaignThumbnailButtonActionPerformed

    private void episodeChangeThumbnailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_episodeChangeThumbnailButtonActionPerformed
        if(selectedEpisode == null)
            return;
        
        var path = FileChooser.openThumbnailFileChooser(this);
        if(path.isEmpty())
            return;
        
        try
        {
            selectedEpisode.changeThumbnail(path.get());
            notifyChanges();
        }
        catch(IOException ex)
        {
            Notify.ferror(
                    this,
                    null,
                    "An error occurred while loading episode thumbnail: %s",
                    ex.getLocalizedMessage()
            );
        }
        finally
        {
            updateSelectedEpisodeThumbnail();
            var card = episodeCardsMap.getOrDefault(selectedEpisode.getName(), null);
            if(card != null)
                card.update();
        }
    }//GEN-LAST:event_episodeChangeThumbnailButtonActionPerformed

    private void episodeDeleteThumbnailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_episodeDeleteThumbnailButtonActionPerformed
        if(selectedEpisode == null)
            return;
        
        selectedEpisode.removeThumbnail();
        updateSelectedEpisodeThumbnail();
        notifyChanges();
        
        var card = episodeCardsMap.getOrDefault(selectedEpisode.getName(), null);
        if(card != null)
            card.update();
    }//GEN-LAST:event_episodeDeleteThumbnailButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        selectedEpisodeChangeName();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bonusLevelsRootPanel;
    private javax.swing.JTextField campaignFruitsField;
    private javax.swing.JTextField campaignLevelsSaveGameField;
    private javax.swing.JTextField campaignNameField;
    private javax.swing.JPanel campaignThumbnailPanel;
    private javax.swing.JButton changeCampaignThumbnailButton;
    private javax.swing.JButton deleteCampaignThumbnailButton;
    private javax.swing.JButton episodeChangeThumbnailButton;
    private javax.swing.JButton episodeDeleteThumbnailButton;
    private javax.swing.JPanel episodeViewPanel;
    private javax.swing.JPanel episodesPanelExtern;
    private javax.swing.JScrollPane episodesScrollPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem menuNewCampaign;
    private javax.swing.JMenuItem menuOpenCampaign;
    private javax.swing.JMenuItem menuSaveAsCampaign;
    private javax.swing.JMenuItem menuSaveCampaign;
    private javax.swing.JButton newEpisodeButton;
    private javax.swing.JPanel normalLevelsRootPanel;
    private javax.swing.JPanel secretLevelsRootPanel;
    private javax.swing.JTextField selectedEpisodeNameField;
    private javax.swing.JPanel selectedEpisodeThumbnailPanel;
    // End of variables declaration//GEN-END:variables
}
