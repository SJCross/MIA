package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MetadataExtractors.*;
import wbif.sjx.common.Object.HCMetadata;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MetadataExtractor extends HCModule {
    public static final String EXTRACTOR_MODE = "Extractor mode";
    private static final String FILENAME_EXTRACTOR = "Filename extractor";
    private static final String FOLDERNAME_EXTRACTOR = "Foldername extractor";
    private static final String METADATA_FILE_EXTRACTOR = "Metadata file extractor";

    public interface ExtractorModes {
        String FILENAME_MODE = "Filename";
        String FOLDERNAME_MODE = "Foldername";
        String METADATA_FILE_MODE = "Metadata file";

        String[] ALL = new String[]{FILENAME_MODE, FOLDERNAME_MODE, METADATA_FILE_MODE};

    }

    public interface FilenameExtractors {
        String NONE = "None";
        String CELLVOYAGER_FILENAME_EXTRACTOR = "Cell Voyager filename";
        String INCUCYTE_LONG_FILENAME_EXTRACTOR = "IncuCyte long filename";
        String INCUCYTE_SHORT_FILENAME_EXTRACTOR = "IncuCyte short filename";
        String OPERA_FILENAME_EXTRACTOR = "Opera filename";

        String[] ALL = new String[]{NONE, CELLVOYAGER_FILENAME_EXTRACTOR, INCUCYTE_LONG_FILENAME_EXTRACTOR,
            INCUCYTE_SHORT_FILENAME_EXTRACTOR, OPERA_FILENAME_EXTRACTOR};

    }

    public interface FoldernameExtractors {
        String NONE = "None";
        String CELLVOYAGER_FOLDERNAME_EXTRACTOR = "Cell Voyager foldername";
        String OPERA_FOLDERNAME_EXTRACTOR = "Opera foldername";

        String[] ALL = new String[]{NONE, CELLVOYAGER_FOLDERNAME_EXTRACTOR, OPERA_FOLDERNAME_EXTRACTOR};
    }

    public interface MetadataFileExtractors {
        String NONE = "None";
        String OPERA_METADATA_FILE_EXTRACTOR = "Opera file (.flex)";

        String[] ALL = new String[]{NONE, OPERA_METADATA_FILE_EXTRACTOR};

    }


    @Override
    public String getTitle() {
        return "Extract metadata";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting current result
        HCMetadata metadata = workspace.getMetadata();

        // Getting parameters
        String metadataExtractorMode = parameters.getValue(EXTRACTOR_MODE);

        switch (metadataExtractorMode) {
            case ExtractorModes.FILENAME_MODE:
                // Getting filename extractor
                String filenameExtractorName = parameters.getValue(FILENAME_EXTRACTOR);
                NameExtractor filenameExtractor = null;

                switch (filenameExtractorName) {
                    case FilenameExtractors.CELLVOYAGER_FILENAME_EXTRACTOR:
                        filenameExtractor = new CellVoyagerFilenameExtractor();
                        break;

                    case FilenameExtractors.INCUCYTE_LONG_FILENAME_EXTRACTOR:
                        filenameExtractor = new IncuCyteLongFilenameExtractor();
                        break;

                    case FilenameExtractors.INCUCYTE_SHORT_FILENAME_EXTRACTOR:
                        filenameExtractor = new IncuCyteShortFilenameExtractor();
                        break;

                    case FilenameExtractors.OPERA_FILENAME_EXTRACTOR:
                        filenameExtractor = new OperaFilenameExtractor();
                        break;

                }

                if (filenameExtractor != null) filenameExtractor.extract(metadata, metadata.getFile().getName());
                break;

            case ExtractorModes.FOLDERNAME_MODE:
                // Getting folder name extractor
                String foldernameExtractorName = parameters.getValue(FOLDERNAME_EXTRACTOR);
                NameExtractor foldernameExtractor = null;
                switch (foldernameExtractorName) {
                    case FoldernameExtractors.CELLVOYAGER_FOLDERNAME_EXTRACTOR:
                        foldernameExtractor = new CellVoyagerFoldernameExtractor();
                        break;

                    case FoldernameExtractors.OPERA_FOLDERNAME_EXTRACTOR:
                        foldernameExtractor = new OperaFoldernameExtractor();
                        break;
                }

                if (foldernameExtractor != null) foldernameExtractor.extract(metadata,metadata.getFile().getParent());
                break;

            case ExtractorModes.METADATA_FILE_MODE:
                // Getting metadata file extractor
                String metadataFileExtractorName = parameters.getValue(METADATA_FILE_EXTRACTOR);
                FileExtractor metadataFileExtractor = null;
                switch (metadataFileExtractorName) {
                    case MetadataFileExtractors.OPERA_METADATA_FILE_EXTRACTOR:
                        metadataFileExtractor = new OperaFileExtractor();
                        break;
                }

                if (metadataFileExtractor != null) metadataFileExtractor.extract(metadata,metadata.getFile());
                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(EXTRACTOR_MODE,Parameter.CHOICE_ARRAY,ExtractorModes.FILENAME_MODE,ExtractorModes.ALL));
        parameters.addParameter(new Parameter(FILENAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FilenameExtractors.NONE,FilenameExtractors.ALL));
        parameters.addParameter(new Parameter(FOLDERNAME_EXTRACTOR, Parameter.CHOICE_ARRAY,FoldernameExtractors.NONE,FoldernameExtractors.ALL));
        parameters.addParameter(new Parameter(METADATA_FILE_EXTRACTOR,Parameter.CHOICE_ARRAY,MetadataFileExtractors.NONE,MetadataFileExtractors.ALL));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(EXTRACTOR_MODE));

        if (parameters.getValue(EXTRACTOR_MODE).equals(ExtractorModes.FILENAME_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(FILENAME_EXTRACTOR));

        } else if (parameters.getValue(EXTRACTOR_MODE).equals(ExtractorModes.FOLDERNAME_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(FOLDERNAME_EXTRACTOR));

        }if (parameters.getValue(EXTRACTOR_MODE).equals(ExtractorModes.METADATA_FILE_MODE)) {
            returnedParameters.addParameter(parameters.getParameter(METADATA_FILE_EXTRACTOR));

        }

        return returnedParameters;

    }

    @Override
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}


