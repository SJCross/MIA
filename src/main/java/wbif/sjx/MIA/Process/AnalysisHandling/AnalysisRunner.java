package wbif.sjx.MIA.Process.AnalysisHandling;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.MIA.GUI.InputOutput.InputControl;
import wbif.sjx.MIA.GUI.InputOutput.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FileFolderPathP;
import wbif.sjx.MIA.Object.Parameters.IntegerP;
import wbif.sjx.MIA.Object.Parameters.StringP;
import wbif.sjx.MIA.Object.ProgressMonitor;
import wbif.sjx.MIA.Process.BatchProcessor;
import wbif.sjx.MIA.Process.Exporter;

import java.io.File;
import java.io.IOException;

/**
 * Created by sc13967 on 22/06/2018.
 */
public class AnalysisRunner {
    private static BatchProcessor batchProcessor;

    public static void startAnalysis(Analysis analysis) throws IOException, InterruptedException {
        // Getting input/output controls
        InputControl inputControl = analysis.getModules().getInputControl();
        OutputControl outputControl = analysis.getModules().getOutputControl();

        // Getting input file
        File inputFile = getInputFile(inputControl);
        if (inputFile == null) return;

        // Initialising Exporter
        String exportName = getExportName(inputControl,outputControl,inputFile);
        Exporter exporter = initialiseExporter(outputControl,exportName);

        // Initialising BatchProcessor
        int nThreads = inputControl.getParameterValue(InputControl.SIMULTANEOUS_JOBS);
        batchProcessor = new BatchProcessor(inputFile);
        batchProcessor.setnThreads(nThreads);
        inputControl.addFilenameFilters(batchProcessor);

        // Resetting progress monitor
        ProgressMonitor.resetProgress();
        GUI.setProgress(0);

        // Running the analysis
        batchProcessor.run(analysis,exporter);

        // Cleaning up
        System.out.println("Complete!");

    }

    public static File getInputFile(InputControl inputControl) {
        String inputPath = ((FileFolderPathP) inputControl.getParameter(InputControl.INPUT_PATH)).getPath();

        if (!checkInputFileValidity(inputPath)) return null;
        return new File(inputPath);

    }

    public static boolean checkInputFileValidity(String path) {
        // Checking if a file/folder had been selected
        if (path == null) {
            System.err.println("Select an input file/folder first");
            return false;
        }

        // Checking if the specified input file is present
        if (!new File(path).exists()) {
            System.err.println("Selected input file/folder can't be found");
            return false;
        }

        return true;

    }

    public static String getExportName(InputControl inputControl, OutputControl outputControl, File inputFile) {
        String seriesMode = ((ChoiceP) inputControl.getParameter(InputControl.SERIES_MODE)).getChoice();
        String seriesList = ((StringP) inputControl.getParameter(InputControl.SERIES_LIST)).getValue();
        String saveLocation = outputControl.getParameterValue(OutputControl.SAVE_LOCATION);
        String saveFilePath = outputControl.getParameterValue(OutputControl.SAVE_FILE_PATH);
        String saveNameMode = outputControl.getParameterValue(OutputControl.SAVE_NAME_MODE);
        String saveFileName = outputControl.getParameterValue(OutputControl.SAVE_FILE_NAME);

        // Determining the file path
        String path = "";
        switch (saveLocation) {
            case OutputControl.SaveLocations.SAVE_WITH_INPUT:
                if (inputFile.isFile()) {
                    path = inputFile.getParent() + MIA.getSlashes();
                } else {
                    path = inputFile.getAbsolutePath() + MIA.getSlashes();
                }
                break;

            case OutputControl.SaveLocations.SPECIFIC_LOCATION:
                path = saveFilePath + MIA.getSlashes();
                break;
        }

        // Determining the file name
        String name = "";
        switch (saveNameMode) {
            case OutputControl.SaveNameModes.MATCH_INPUT:
                if (inputFile.isFile()) {
                    name = FilenameUtils.removeExtension(inputFile.getName());
                } else {
                    name = inputFile.getName();
                }
                break;

            case OutputControl.SaveNameModes.SPECIFIC_NAME:
                name = saveFileName;
                break;
        }

        // Determining the suffix
        String suffix = "";
        if (seriesMode.equals(InputControl.SeriesModes.SERIES_LIST)) {
            suffix = "_S" + seriesList.replace(" ", "");
        }

        return path + name + suffix;

    }

    public static Exporter initialiseExporter(OutputControl outputControl, String exportName) {
        String exportMode = outputControl.getParameterValue(OutputControl.EXPORT_MODE);
        String metadataItemForGrouping = outputControl.getParameterValue(OutputControl.METADATA_ITEM_FOR_GROUPING);
        boolean exportXLS = outputControl.isEnabled();
        boolean exportSummary = outputControl.getParameterValue(OutputControl.EXPORT_SUMMARY);
        String summaryType = outputControl.getParameterValue(OutputControl.SUMMARY_MODE);
        boolean exportIndividualObjects = outputControl.getParameterValue(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        String appendDateTimeMode = outputControl.getParameterValue(OutputControl.APPEND_DATETIME_MODE);
        boolean showObjectCounts = outputControl.getParameterValue(OutputControl.SHOW_OBJECT_COUNTS);

        // Initialising the exporter (if one was requested)
        Exporter exporter = exportXLS ? new Exporter(exportName) : null;
        if (exporter != null) {
            exporter.setMetadataItemForGrouping(metadataItemForGrouping);
            exporter.setExportSummary(exportSummary);
            exporter.setShowObjectCounts(showObjectCounts);
            exporter.setExportIndividualObjects(exportIndividualObjects);

            switch (exportMode) {
                case OutputControl.ExportModes.ALL_TOGETHER:
                    exporter.setExportMode(Exporter.ExportMode.ALL_TOGETHER);
                    break;

                case OutputControl.ExportModes.GROUP_BY_METADATA:
                    exporter.setExportMode(Exporter.ExportMode.GROUP_BY_METADATA);
                    break;

                case OutputControl.ExportModes.INDIVIDUAL_FILES:
                    exporter.setExportMode(Exporter.ExportMode.INDIVIDUAL_FILES);
                    break;
            }

            switch (summaryType) {
                case OutputControl.SummaryModes.ONE_AVERAGE_PER_FILE:
                    exporter.setSummaryMode(Exporter.SummaryMode.PER_FILE);
                    break;

                case OutputControl.SummaryModes.AVERAGE_PER_TIMEPOINT:
                    exporter.setSummaryMode(Exporter.SummaryMode.PER_TIMEPOINT_PER_FILE);
                    break;

                case OutputControl.SummaryModes.GROUP_BY_METADATA:
                    exporter.setSummaryMode(Exporter.SummaryMode.GROUP_BY_METADATA);
                    exporter.setMetadataItemForSummary(outputControl.getParameterValue(OutputControl.METADATA_ITEM_FOR_SUMMARY));
                    break;
            }

            switch (appendDateTimeMode) {
                case OutputControl.AppendDateTimeModes.ALWAYS:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.ALWAYS);
                    break;

                case OutputControl.AppendDateTimeModes.IF_FILE_EXISTS:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.IF_FILE_EXISTS);
                    break;

                case OutputControl.AppendDateTimeModes.NEVER:
                    exporter.setAppendDateTimeMode(Exporter.AppendDateTimeMode.NEVER);
                    break;
            }
        }

        return exporter;

    }

    public static void stopAnalysis() {
        System.err.println("STOPPING");
        if (batchProcessor != null) {
            batchProcessor.stopAnalysis();
        } else {
            GUI.setModuleBeingEval(-1);
            GUI.updateModules();
            Thread.currentThread().getThreadGroup().stop();
            System.out.println("Shutdown complete!");
        }
    }
}
