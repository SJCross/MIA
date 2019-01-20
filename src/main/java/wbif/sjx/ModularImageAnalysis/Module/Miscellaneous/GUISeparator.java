package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanParam;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module{
    private static boolean verbose = false;
    
    public static final String SHOW_BASIC = "Show basic";
    public static final String EXPANDED_BASIC = "Expanded basic GUI";
    public static final String EXPANDED_EDITING = "Expanded editing GUI";


    @Override
    public String getTitle() {
        return "GUI separator";
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        return true;
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new BooleanParam(SHOW_BASIC,true));
        parameters.add(new BooleanParam(EXPANDED_BASIC,true));
        parameters.add(new BooleanParam(EXPANDED_EDITING,true));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
