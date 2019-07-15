package wbif.sjx.MIA.GUI.Panels.MainPanels;

import wbif.sjx.MIA.Module.Module;

import javax.swing.*;

public abstract class MainPanel extends JPanel {
    public abstract void updatePanel();
    public abstract void updateModules();
    public abstract void updateModuleStates();
    public abstract void updateParameters();
    public abstract void updateHelpNotes();

    public abstract int getPreferredWidth();
    public abstract int getMinimumWidth();
    public abstract int getPreferredHeight();
    public abstract int getMinimumHeight();

    public abstract int getProgress();
    public abstract void setProgress(int progress);

    public abstract boolean showHelp();
    public abstract void setShowHelp(boolean showHelp);
    public abstract boolean showNotes();
    public abstract void setShowNotes(boolean showNotes);

    public abstract Module getLastHelpNotesModule();
    public abstract void setLastHelpNotesModule(Module module);

}
