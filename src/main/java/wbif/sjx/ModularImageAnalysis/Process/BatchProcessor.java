// TODO: Add methods for XLS and JSON data export

package wbif.sjx.ModularImageAnalysis.Process;

import ij.Prefs;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ChoiceP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.IntegerP;
import wbif.sjx.ModularImageAnalysis.Object.ProgressMonitor;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;
import wbif.sjx.ModularImageAnalysis.Object.WorkspaceCollection;
import wbif.sjx.ModularImageAnalysis.Process.AnalysisHandling.Analysis;
import wbif.sjx.common.System.FileCrawler;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by sc13967 on 21/10/2016.
 */
public class BatchProcessor extends FileCrawler {
    DecimalFormat dfInt = new DecimalFormat("0");
    DecimalFormat dfDec = new DecimalFormat("0.00");

    private boolean verbose = true;
    private int nThreads = Runtime.getRuntime().availableProcessors()/2;

    private ThreadPoolExecutor pool;

    private boolean shutdownEarly;
    private int counter = 0;
    private int origThreads = Prefs.getThreads();


    // CONSTRUCTORS

    public BatchProcessor(File rootFolder) {
        super(rootFolder);
    }


    // PUBLIC METHODS

    public void run(Analysis analysis, Exporter exporter) throws IOException, InterruptedException {
        shutdownEarly = false;
        String exportMode = ((ChoiceP) analysis.getOutputControl().getParameter(OutputControl.EXPORT_MODE)).getChoice();

        WorkspaceCollection workspaces = new WorkspaceCollection();

        // The protocol to generateModuleList will depend on if a single file or a folder was selected
        if (rootFolder.getFolderAsFile().isFile()) {
            runSingle(workspaces, analysis);

            if (!exportMode.equals(OutputControl.ExportModes.NONE)) {
                System.out.println("Exporting results to spreadsheet");
                exporter.exportResults(workspaces, analysis);
            }

        } else {
            // The system can generateModuleList multiple files in parallel or one at a time
            runParallel(workspaces, analysis, exporter);

            switch (exportMode) {
                case OutputControl.ExportModes.ALL_TOGETHER:
                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    System.out.println("Exporting results to spreadsheet");
                    exporter.exportResults(workspaces,analysis);
                    break;
            }
        }

        // Saving the results
        if (shutdownEarly || exporter == null) return;

        GUI.setProgress(0);

    }

    private void runParallel(WorkspaceCollection workspaces, Analysis analysis, Exporter exporter) throws InterruptedException {
        File next = getNextValidFileInStructure();

        boolean continuousExport = ((BooleanP) analysis.getOutputControl().getParameter(OutputControl.CONTINUOUS_DATA_EXPORT)).isSelected();
        int saveNFiles = ((IntegerP) analysis.getOutputControl().getParameter(OutputControl.SAVE_EVERY_N)).getValue();
        String exportMode = ((ChoiceP) analysis.getOutputControl().getParameter(OutputControl.EXPORT_MODE)).getChoice();

        Module.setVerbose(false);

        // Set the number of Fiji threads to maximise the number of jobs, so it doesn't clash with MIA multi-threading.
        int nSimultaneousJobs = ((IntegerP) analysis.getInputControl().getParameter(InputControl.SIMULTANEOUS_JOBS)).getValue();
        if (nSimultaneousJobs != 1) {
            int nThreads = Math.floorDiv(origThreads,nSimultaneousJobs);
            Prefs.setThreads(nThreads);
            Prefs.savePreferences();
        }

        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(nSimultaneousJobs,nSimultaneousJobs,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Runnables are first stored in a HashSet, then loaded all at once to the ThreadPoolExecutor.  This means the
        // system isn't scanning files and reading for the analysis simultaneously.
        int loadTotal = 0;
        HashSet<Runnable> tasks = new HashSet<>();
        while (next != null) {
            File finalNext = next;

            // Adding a parameter to the metadata structure indicating the depth of the current file
            int fileDepth = 0;
            File parent = next.getParentFile();
            while (parent != rootFolder.getFolderAsFile() && parent != null) {
                parent = parent.getParentFile();
                fileDepth++;
            }

            // For the current file, determining how many series to processAutomatic (and which ones)
            TreeMap<Integer,String> seriesNumbers = analysis.getInputControl().getSeriesNumbers(next);

            // Iterating over all series to analyse, adding each one as a new workspace
            for (int seriesNumber:seriesNumbers.keySet()) {
                Workspace workspace = workspaces.getNewWorkspace(next,seriesNumber);
                String seriesName = seriesNumbers.get(seriesNumber);
                if (seriesName.equals("")) seriesName = "FILE: "+finalNext.getName();
                workspace.getMetadata().setSeriesName(seriesName);

                workspace.getMetadata().put("FILE_DEPTH", fileDepth);

                // Adding this Workspace to the Progress monitor
                ProgressMonitor.setWorkspaceProgress(workspace,0d);

                Runnable task = () -> {
                    try {
                        // Running the current analysis
                        analysis.execute(workspace);

                        // Getting the number of completed and total tasks
                        incrementCounter();
                        int nComplete = getCounter();
                        double nTotal = pool.getTaskCount();
                        double percentageComplete = (nComplete / nTotal) * 100;

                        // Displaying the current progress
                        String string = "Completed " + dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                                + " (" + dfDec.format(percentageComplete) + "%), " + finalNext.getName();
                        System.out.println(string);

                        if (continuousExport && nComplete % saveNFiles == 0) exporter.exportResults(workspaces, analysis);
                        if (exportMode.equals(OutputControl.ExportModes.INDIVIDUAL_FILES)) exporter.exportResults(workspace, analysis);

                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (Throwable t) {
                        System.err.println("Failed for file " + finalNext.getName());
                        t.printStackTrace(System.err);

                        pool.shutdownNow();

                    }
                };

                loadTotal++;
                tasks.add(task);

            }

            // Displaying the current progress
            System.out.println("Initialising "+dfInt.format(loadTotal)+" jobs");

            next = getNextValidFileInStructure();

        }

        // Starting the jobs
        double nTotal = pool.getTaskCount();
        System.out.println("Started processing "+dfInt.format(loadTotal)+" jobs");
        for (Runnable task:tasks) pool.submit(task);

        // Telling the pool not to accept any more jobs and to wait until all queued jobs have completed
        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        Prefs.setThreads(origThreads);

    }

    private void runSingle(WorkspaceCollection workspaces, Analysis analysis) throws InterruptedException {
        // Setting up the ExecutorService, which will manage the threads
        pool = new ThreadPoolExecutor(1,1,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // For the current file, determining how many series to processAutomatic (and which ones)
        TreeMap<Integer,String> seriesNumbers = analysis.getInputControl().getSeriesNumbers(rootFolder.getFolderAsFile());

        // Only set verbose if a single series is being processed
        Module.setVerbose(seriesNumbers.size() == 1);

        // Iterating over all series to analyse, adding each one as a new workspace
        for (int seriesNumber:seriesNumbers.keySet()) {
            Workspace workspace = workspaces.getNewWorkspace(rootFolder.getFolderAsFile(),seriesNumber);
            String seriesName = seriesNumbers.get(seriesNumber);
            if (seriesName.equals("")) seriesName = "FILE: "+rootFolder.getFolderAsFile().getName();
            workspace.getMetadata().setSeriesName(seriesName);

            // Adding this Workspace to the Progress monitor
            ProgressMonitor.setWorkspaceProgress(workspace,0d);

            Runnable task = () -> {
                try {
                    analysis.execute(workspace);
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }

                // Getting the number of completed and total tasks
                incrementCounter();
                int nComplete = getCounter();
                double nTotal = pool.getTaskCount();
                double percentageComplete = (nComplete / nTotal) * 100;

                // Displaying the current progress
                String string = "Completed " + dfInt.format(nComplete) + "/" + dfInt.format(nTotal)
                        + " (" + dfDec.format(percentageComplete) + "%)";
                System.out.println(string);

                // Clearing images from the workspace to prevent memory leak
                workspace.clearAllImages(true);

            };

            // Submit the jobs for this file, then tell the pool not to accept any more jobs and to wait until all
            // queued jobs have completed
            pool.submit(task);

            // Displaying the current progress
            double nTotal = pool.getTaskCount();
            String string = "Started processing "+dfInt.format(nTotal)+" jobs";
            System.out.println(string);

        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void stopAnalysis() {
        Prefs.setThreads(origThreads);
        Thread.currentThread().getThreadGroup().stop();
        System.out.println("Shutdown complete!");

    }


    // GETTERS AND SETTERS

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getnThreads() {
        return nThreads;
    }

    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }

    synchronized int getCounter() {
        return counter;

    }

    synchronized void incrementCounter() {
        counter++;

    }
}
