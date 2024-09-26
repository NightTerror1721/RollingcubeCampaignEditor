/*
 * 
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package kp.rollingcube.ce.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import kp.rollingcube.ce.campaign.BonusLevel;
import kp.rollingcube.ce.campaign.Episode;
import kp.rollingcube.ce.campaign.LevelType;
import kp.rollingcube.ce.campaign.NormalLevel;
import kp.rollingcube.ce.utils.Counter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 *
 * @author Marc
 */
public class LevelsPanel extends JPanel
{
    private final @NonNull CampaignEditor editor;
    private final @NonNull LevelType levelType;
    private final JPanel levelsPanel = new JPanel();
    
    @Getter @Setter
    private Episode episode;
    
    public LevelsPanel(@NonNull CampaignEditor editor, @NonNull LevelType levelType)
    {
        this.editor = editor;
        this.levelType = levelType;
        initComponents();
        
        var layout = new GridBagLayout();
        levelsPanel.setLayout(layout);
        levelsScrollPanel.getVerticalScrollBar().setUnitIncrement(30);
    }
    
    public void update()
    {
        levelsPanel.removeAll();
        
        if(episode == null)
        {
            newLevelButton.setEnabled(false);
            levelsPanel.updateUI();
            return;
        }
        
        if(levelType == LevelType.SECRET)
            updateSecretLevels();
        else
            updateIndexedLevels();
        
        newLevelButton.setEnabled(true);
        levelsPanel.updateUI();
    }
    
    private void updateIndexedLevels()
    {
        boolean isBonus = levelType == LevelType.BONUS;
        int len = isBonus ? episode.getBonusLevelsCount() : episode.getNormalLevelsCount();
        
        int index;
        for(index = 0; index < len; index++)
        {
            if(!isBonus)
            {
                var level = episode.getNormalLevel(index);
                var levelPanel = new IndexableLevelCard<NormalLevel>();
                levelPanel.setListener(new IndexableLevelCard.EventsListener<>()
                {
                    @Override
                    public void onDelete(NormalLevel level)
                    {
                        if(!Notify.ask(
                                editor,
                                "Delete Normal Level",
                                String.format("Are you sure you want to delete the %s normal level?",
                                        level.getIndex() + 1)
                        )) return;
                        
                        try { episode.removeNormalLevel(level.getIndex()); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("deleting", ex); }
                        finally { update(); }
                    }

                    @Override
                    public void onUp(NormalLevel level)
                    {
                        int index = level.getIndex();
                        if(index <= 0)
                            return;
                        
                        try { episode.swapNormalLevels(index, index - 1); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("moving up", ex); }
                        finally { update(); }
                    }

                    @Override
                    public void onDown(NormalLevel level)
                    {
                        int index = level.getIndex();
                        if(index >= episode.getNormalLevelsCount() - 1)
                            return;
                        
                        try { episode.swapNormalLevels(index, index + 1); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("moving down", ex); }
                        finally { update(); }
                    }
                });
                levelPanel.setLevel(level);
                levelPanel.update();
                levelsPanel.add(levelPanel, prepareConstraints(level.getIndex()));
            }
            else
            {
                var level = episode.getBonusLevel(index);
                var levelPanel = new IndexableLevelCard<BonusLevel>();
                levelPanel.setListener(new IndexableLevelCard.EventsListener<>()
                {
                    @Override
                    public void onDelete(BonusLevel level)
                    {
                        if(!Notify.ask(
                                editor,
                                "Delete Bonus Level",
                                String.format("Are you sure you want to delete the %s bonus level?",
                                        level.getIndex() + 1)
                        )) return;
                        
                        try { episode.removeBonusLevel(level.getIndex()); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("deleting", ex); }
                        finally { update(); }
                    }

                    @Override
                    public void onUp(BonusLevel level)
                    {
                        int index = level.getIndex();
                        if(index <= 0)
                            return;
                        
                        try { episode.swapBonusLevels(index, index - 1); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("moving up", ex); }
                        finally { update(); }
                    }

                    @Override
                    public void onDown(BonusLevel level)
                    {
                        int index = level.getIndex();
                        if(index >= episode.getNormalLevelsCount() - 1)
                            return;
                        
                        try { episode.swapBonusLevels(index, index + 1); editor.notifyChanges(); }
                        catch(IllegalArgumentException ex) { eventError("moving down", ex); }
                        finally { update(); }
                    }
                });
                levelPanel.setLevel(level);
                levelPanel.update();
                levelsPanel.add(levelPanel, prepareConstraints(level.getIndex()));
            }
        }
        
        levelsPanel.add(new JPanel(), prepareGhostConstraints(index));
        levelsPanel.add(new JPanel(), prepareGhostConstraints(index + 1));
    }
    
    private void updateSecretLevels()
    {
        var index = new Counter();
        episode.getSecretLevelsNames().stream().sorted().forEach(levelName -> {
            var level = episode.getSecretLevel(levelName);
            var levelPanel = new SecretLevelCard();
            levelPanel.setListener(lv -> {
                if(!Notify.ask(
                        editor,
                        "Delete Secret Level",
                        String.format("Are you sure you want to delete the \"%s\" secret level?",
                                lv.getName())
                )) return;

                try { episode.removeSecretLevel(lv.getName()); editor.notifyChanges(); }
                catch(IllegalArgumentException ex) { eventError("deleting", ex); }
                finally { update(); }
            });
            levelPanel.setLevel(level);
            levelPanel.update();
            levelsPanel.add(levelPanel, prepareConstraints(index.getValue()));
            index.increase();
        });
        
        levelsPanel.add(new JPanel(), prepareGhostConstraints(index.getValue()));
        levelsPanel.add(new JPanel(), prepareGhostConstraints(index.getValue() + 1));
    }
    
    private void loadLevels()
    {
        if(episode == null)
            return;
        
        var paths = FileChooser.openLevelsFileChooser(editor);
        if(paths.isEmpty())
            return;
        
        for(var path : paths)
        {
            try
            {
                switch(levelType)
                {
                    case NORMAL: episode.addNormalLevel(path); break;
                    case BONUS: episode.addBonusLevel(path); break;
                    //case SECRET: episode.addNormalLevel(path); break;
                }
            }
            catch(Exception ex)
            {
                Notify.ferror(
                        editor,
                        null,
                        "An error occurred while loading level \"%s\": %s",
                        path,
                        ex.getLocalizedMessage()
                );
            }
        }
        update();
        editor.notifyChanges();
    }
    
    private GridBagConstraints prepareConstraints(int index)
    {
        var ct = new GridBagConstraints();
        ct.fill = GridBagConstraints.HORIZONTAL;
        ct.anchor = GridBagConstraints.NORTHWEST;
        ct.weightx = 1;
        ct.weighty = 1;
        ct.gridx = index % 2;
        ct.gridy = index / 2;
        return ct;
    }
    
    private GridBagConstraints prepareGhostConstraints(int lastIndex)
    {
        var ct = prepareConstraints(lastIndex);
        ct.weighty = 100;
        return ct;
    }
    
    private void eventError(String action, String message)
    {
        Notify.ferror(
                editor,
                null,
                "An error occurred while %s level: %s",
                action,
                message
        );
    }
    private void eventError(String action, Throwable ex)
    {
        eventError(action, ex.getLocalizedMessage());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newLevelButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        levelsScrollPanel = new javax.swing.JScrollPane(levelsPanel);

        newLevelButton.setText("Add Level");
        newLevelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLevelButtonActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        levelsScrollPanel.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jPanel1.add(levelsScrollPanel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newLevelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newLevelButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newLevelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLevelButtonActionPerformed
        loadLevels();
    }//GEN-LAST:event_newLevelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane levelsScrollPanel;
    private javax.swing.JButton newLevelButton;
    // End of variables declaration//GEN-END:variables
}
