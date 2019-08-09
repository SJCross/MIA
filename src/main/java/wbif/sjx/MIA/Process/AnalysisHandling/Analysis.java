package wbif.sjx.MIA.Process.AnalysisHandling;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.ProgressMonitor;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Created by sc13967 on 21/10/2016.
 *
 * Abstract Analysis-type class, which will be extended by particular analyses
 *
 */
public class Analysis {
    private ModuleCollection modules = new ModuleCollection();
    private boolean shutdown = false;
    private String analysisFilename = "";

    // CONSTRUCTOR

    public Analysis() {
        initialise();

    }


    // PUBLIC METHODS

    /*
     * Initialisation method is where workspace is populated with modules and module-specific parameters.
     */
    public void initialise() {}

    /*
     * The method that gets called by the BatchProcessor.  This shouldn't have any user interaction elements
     * @param workspace Workspace containing stores for images and objects
     */
    public boolean execute(Workspace workspace) {
        // Running through modules
        int total = modules.size();
        int count = 0;
        for (Module module:modules) {
            if (Thread.currentThread().isInterrupted()) break;
            if (module.isEnabled() && module.isRunnable()) {
                boolean status = module.execute(workspace);
                if (!status) {
                    // The module failed or requested analysis termination.  Add this message to the write
                    MIA.log.write("Analysis terminated early for file \""+workspace.getMetadata().getFile()+
                            "\" by module \""+module.getName()+"\" (\""+module.getNickname()+"\").", LogRenderer.Level.WARNING);

                    // End the analysis generateModuleList
                    break;

                }
            }

            // Updating progress bar
            double percentageComplete = ((double) (++count))/((double) total)*100;
            ProgressMonitor.setWorkspaceProgress(workspace,percentageComplete);
            double overallPercentageComplete = ProgressMonitor.getOverallProgress();
            GUI.setProgress((int) Math.round(overallPercentageComplete));
        }

        // We're only interested in the measurements now, so clearing images and object coordinates
        workspace.clearAllImages(true);
        workspace.clearAllObjects(true);

        // If enabled, write the current memory usage to the console
        if (MIA.log.isWriteEnabled(LogRenderer.Level.MEMORY)) {
            double totalMemory = Runtime.getRuntime().totalMemory();
            double usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            DecimalFormat df = new DecimalFormat("#.0");

            String memoryMessage = df.format(usedMemory*1E-6)+" MB of "+df.format(totalMemory*1E-6)+" MB" +
                    ", analysis complete"+
                    ", file \""+workspace.getMetadata().getFile() +
                    ", time "+dateTime;

            MIA.log.write(memoryMessage, LogRenderer.Level.MEMORY);

        }

        return true;

    }

    public ModuleCollection getModules() {
        return modules;

    }

    public void setModules(ModuleCollection modules) {
        this.modules = modules;
    }

    public void removeAllModules() {
        modules.clear();

    }

    public void shutdown() {
        shutdown = true;

    }

    public String getAnalysisFilename() {
        return analysisFilename;
    }

    public void setAnalysisFilename(String analysisFilename) {
        this.analysisFilename = analysisFilename;
    }

    public boolean hasVisibleParameters() {
        return (modules.hasVisibleParameters());
    }
}
