package wbif.sjx.ModularImageAnalysis.Process;

import org.apache.commons.io.FilenameUtils;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.common.FileConditions.ExtensionMatchesString;

import java.io.File;
import java.io.IOException;

/**
 * Created by sc13967 on 22/06/2018.
 */
public class AnalysisRunner {
    private static BatchProcessor batchProcessor;

    public static void startAnalysis(Analysis analysis) throws IOException, GenericMIAException, InterruptedException {
        // Getting input/output controls
        InputControl inputControl = analysis.getInputControl();
        OutputControl outputControl = analysis.getOutputControl();

        // Getting input file
        File inputFile = getInputFile(inputControl);
        if (inputFile == null) return;

        // Initialising Exporter
        String exportName = getExportName(inputControl,inputFile);
        Exporter exporter = initialiseExporter(outputControl,exportName);

        // Initialising BatchProcessor
        batchProcessor = new BatchProcessor(inputFile);
        batchProcessor.setnThreads(inputControl.getParameterValue(InputControl.NUMBER_OF_THREADS));
        addFilenameFilters(inputControl);

        // Running the analysis
        batchProcessor.runAnalysisOnStructure(analysis,exporter);

        // Cleaning up
        Runtime.getRuntime().gc();
        System.out.println("Complete!");

    }

    public static File getInputFile(InputControl inputControl) {
        String inputMode = inputControl.getParameterValue(InputControl.INPUT_MODE);
        String singleFile = inputControl.getParameterValue(InputControl.SINGLE_FILE_PATH);
        String batchFolder = inputControl.getParameterValue(InputControl.BATCH_FOLDER_PATH);

        switch (inputMode) {
            case InputControl.InputModes.SINGLE_FILE:
                if (!checkInputFileValidity(singleFile)) return null;
                return new File(singleFile);

            case InputControl.InputModes.BATCH:
                if (!checkInputFileValidity(batchFolder)) return null;
                return new File(batchFolder);
        }

        return null;

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

    public static String getExportName(InputControl inputControl, File inputFile) {
        String inputMode = inputControl.getParameterValue(InputControl.INPUT_MODE);
        String seriesMode = inputControl.getParameterValue(InputControl.SERIES_MODE);
        int seriesNumber = inputControl.getParameterValue(InputControl.SERIES_NUMBER);

        switch (inputMode) {
            case InputControl.InputModes.SINGLE_FILE:
                switch (seriesMode) {
                    case InputControl.SeriesModes.ALL_SERIES:
                        return FilenameUtils.removeExtension(inputFile.getAbsolutePath());
                    case InputControl.SeriesModes.SINGLE_SERIES:
                        return FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + "_S" + seriesNumber;
                }
            case InputControl.InputModes.BATCH:
                switch (seriesMode) {
                    case InputControl.SeriesModes.ALL_SERIES:
                        return inputFile.getAbsolutePath() + "\\" + inputFile.getName();
                    case InputControl.SeriesModes.SINGLE_SERIES:
                        return inputFile.getAbsolutePath() + "\\" + inputFile.getName() + "_S" + seriesNumber;
                }
            default:
                return "";
        }
    }

    public static void addFilenameFilters(InputControl inputControl) {
        String extension = inputControl.getParameterValue(InputControl.FILE_EXTENSION);
        boolean useFilenameFilter1 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_1);
        String filenameFilter1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_1);
        String filenameFilterType1 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_1);
        boolean useFilenameFilter2 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_2);
        String filenameFilter2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_2);
        String filenameFilterType2 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_2);
        boolean useFilenameFilter3 = inputControl.getParameterValue(InputControl.USE_FILENAME_FILTER_3);
        String filenameFilter3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_3);
        String filenameFilterType3 = inputControl.getParameterValue(InputControl.FILENAME_FILTER_TYPE_3);

        // Adding extension filter
        batchProcessor.addFileCondition(new ExtensionMatchesString(new String[]{extension}));

        // Adding filename filters
        if (useFilenameFilter1) batchProcessor.addFilenameFilter(filenameFilterType1,filenameFilter1);
        if (useFilenameFilter2) batchProcessor.addFilenameFilter(filenameFilterType2,filenameFilter2);
        if (useFilenameFilter3) batchProcessor.addFilenameFilter(filenameFilterType3,filenameFilter3);

    }

    public static Exporter initialiseExporter(OutputControl outputControl, String exportName) {
        boolean exportXLSX = outputControl.isEnabled();
        boolean exportSummary = outputControl.getParameterValue(OutputControl.EXPORT_SUMMARY);
        String summaryType = outputControl.getParameterValue(OutputControl.SUMMARY_TYPE);
        boolean exportIndividualObjects = outputControl.getParameterValue(OutputControl.EXPORT_INDIVIDUAL_OBJECTS);
        boolean showObjectCounts = outputControl.getParameterValue(OutputControl.SHOW_OBJECT_COUNTS);
        boolean showChildCounts = outputControl.getParameterValue(OutputControl.SHOW_NUMBER_OF_CHILDREN);
        boolean calculateMean = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_MEAN);
        boolean calculateMin = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_MIN);
        boolean calculateMax = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_MAX);
        boolean calculateStd = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_STD);
        boolean calculateSum = outputControl.getParameterValue(OutputControl.CALCULATE_SUMMARY_SUM);

        // Initialising the exporter (if one was requested)
        Exporter exporter = exportXLSX ? new Exporter(exportName, Exporter.XLSX_EXPORT) : null;
        if (exporter != null) {
            exporter.setExportSummary(exportSummary);
            exporter.setShowObjectCounts(showObjectCounts);
            exporter.setShowChildCounts(showChildCounts);
            exporter.setCalculateMean(calculateMean);
            exporter.setCalculateMin(calculateMin);
            exporter.setCalculateMax(calculateMax);
            exporter.setCalculateStd(calculateStd);
            exporter.setCalculateSum(calculateSum);
            exporter.setExportIndividualObjects(exportIndividualObjects);

            switch (summaryType) {
                case OutputControl.SummaryTypes.ONE_AVERAGE_PER_FILE:
                    exporter.setSummaryType(Exporter.SummaryType.PER_FILE);
                    break;

                case OutputControl.SummaryTypes.AVERAGE_PER_TIMEPOINT:
                    exporter.setSummaryType(Exporter.SummaryType.PER_TIMEPOINT_PER_FILE);
                    break;
            }
        }

        return exporter;

    }

    public static void stopAnalysis() {
        batchProcessor.stopAnalysis();

    }
}
