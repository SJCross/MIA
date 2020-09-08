package wbif.sjx.MIA.Module.WorkflowHandling;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ModuleP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class ModuleIsEnabled extends CoreWorkspaceHandler {
    public static final String CONDITION_SEPARATOR = "Condition";
    public static final String TEST_MODE = "Test mode";
    public static final String TEST_MODULE = "Test module";

    public static final String RESULT_SEPARATOR = "Result";

    public ModuleIsEnabled(ModuleCollection modules) {
        super("Module is enabled condition", modules);
    }

    public interface TestModes {
        String MODULE_IS_ENABLED = "Module is enabled";
        String MODULE_IS_NOT_ENABLED = "Module is not enabled";

        String[] ALL = new String[] { MODULE_IS_ENABLED, MODULE_IS_NOT_ENABLED };

    }

    public boolean testDoRedirect() {
        String testMode = parameters.getValue(TEST_MODE);
        Module testModule = parameters.getValue(TEST_MODULE);

        if (testModule == null)
            return false;

        boolean terminate = false;
        switch (testMode) {
            case TestModes.MODULE_IS_ENABLED:
                terminate = testModule.isEnabled();
                break;
            case TestModes.MODULE_IS_NOT_ENABLED:
                terminate = !testModule.isEnabled();
                break;
        }
        
        return terminate;

    }

    @Override
    public String getPackageName() {
        return PackageNames.WORKFLOW_HANDLING;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE);

        if (testDoRedirect())
            return processTermination(parameters, workspace, showRedirectMessage);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ChoiceP(TEST_MODE, this, TestModes.MODULE_IS_ENABLED, TestModes.ALL));
        parameters.add(new ModuleP(TEST_MODULE, this, true));
        
        parameters.add(new ParamSeparatorP(RESULT_SEPARATOR, this));
        parameters.addAll(super.updateAndGetParameters());

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TEST_MODE));
        returnedParameters.add(parameters.getParameter(TEST_MODULE));

        returnedParameters.add(parameters.getParameter(RESULT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONTINUATION_MODE));
        switch ((String) parameters.getValue(CONTINUATION_MODE)) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                returnedParameters.add(parameters.getParameter(REDIRECT_MODULE));
                redirectModule = parameters.getValue(REDIRECT_MODULE);
                returnedParameters.add(parameters.getParameter(SHOW_REDIRECT_MESSAGE));
                if ((boolean) parameters.getValue(SHOW_REDIRECT_MESSAGE)) {
                    returnedParameters.add(parameters.getParameter(REDIRECT_MESSAGE));
                }
                break;
            case ContinuationModes.TERMINATE:
                returnedParameters.add(parameters.getParameter(EXPORT_WORKSPACE));
                returnedParameters.add(parameters.getParameter(REMOVE_IMAGES));
                returnedParameters.add(parameters.getParameter(REMOVE_OBJECTS));
                redirectModule = null;
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
