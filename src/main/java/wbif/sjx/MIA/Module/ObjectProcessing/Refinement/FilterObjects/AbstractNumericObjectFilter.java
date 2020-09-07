package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;

public abstract class AbstractNumericObjectFilter extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REFERENCE_VAL_IMAGE = "Reference value image";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_VAL_PARENT_OBJECT = "Reference value parent object";
    public static final String REFERENCE_OBJECT_MEASUREMENT = "Reference object measurement";
    public static final String REFERENCE_MULTIPLIER = "Reference value multiplier";

    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";

    public interface FilterMethods {
        String LESS_THAN = "Less than";
        String LESS_THAN_OR_EQUAL_TO = "Less than or equal to";
        String EQUAL_TO = "Equal to";
        String GREATER_THAN_OR_EQUAL_TO = "Greater than or equal to";
        String GREATER_THAN = "Greater than";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[] { LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO,
                GREATER_THAN, NOT_EQUAL_TO };

    }

    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[] { FIXED_VALUE, IMAGE_MEASUREMENT, PARENT_OBJECT_MEASUREMENT };

    }

    protected AbstractNumericObjectFilter(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public String getIndividualFixedValueFullName(String filterMethod, String targetName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + targetName + " " + filterMethodSymbol + " " + referenceValue;
    }

    public String getSummaryFixedValueFullName(String inputObjectsName, String filterMethod, String targetName,
            String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + targetName + " " + filterMethodSymbol + " "
                + referenceValue;
    }

    public String getIndividualImageMeasFullName(String filterMethod, String targetName, String imageName,
            String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + imageName + " " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getSummaryImageMeasFullName(String inputObjectsName, String filterMethod, String targetName,
            String imageName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + imageName + " " + targetName + " " + filterMethodSymbol
                + " " + refName;
    }

    public String getIndividualParentMeasFullName(String filterMethod, String targetName, String parentName,
            String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + parentName + " " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getSummaryParentMeasFullName(String inputObjectsName, String filterMethod, String targetName,
            String parentName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + parentName + " " + targetName + " " + filterMethodSymbol
                + " " + refName;
    }

    public static boolean testFilter(double testValue, double referenceValue, String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return testValue < referenceValue;
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return testValue <= referenceValue;
            case FilterMethods.EQUAL_TO:
                return testValue == referenceValue;
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return testValue >= referenceValue;
            case FilterMethods.GREATER_THAN:
                return testValue > referenceValue;
            case FilterMethods.NOT_EQUAL_TO:
                return testValue != referenceValue;
        }

        return false;

    }

    public static String getFilterMethodSymbol(String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return "<";
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return "<=";
            case FilterMethods.EQUAL_TO:
                return "==";
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return ">=";
            case FilterMethods.GREATER_THAN:
                return ">";
            case FilterMethods.NOT_EQUAL_TO:
                return "!=";
        }

        return "";

    }

    public String getIndividualMeasurementName(String targetName) {
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        double fixedValue = parameters.getValue(REFERENCE_VALUE);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);

        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                return getIndividualFixedValueFullName(filterMethod, targetName, String.valueOf(fixedValue));
            case ReferenceModes.IMAGE_MEASUREMENT:
                return getIndividualImageMeasFullName(filterMethod, targetName, refImage, refImageMeas);
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                return getIndividualParentMeasFullName(filterMethod, targetName, refParent, refParentMeas);
            default:
                return "";
        }
    }

    public String getSummaryMeasurementName(String targetName) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        double fixedValue = parameters.getValue(REFERENCE_VALUE);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);

        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                return getSummaryFixedValueFullName(inputObjectsName, filterMethod, targetName,
                        String.valueOf(fixedValue));
            case ReferenceModes.IMAGE_MEASUREMENT:
                return getSummaryImageMeasFullName(inputObjectsName, filterMethod, targetName, refImage, refImageMeas);
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                return getSummaryParentMeasFullName(inputObjectsName, filterMethod, targetName, refParent,
                        refParentMeas);
            default:
                return "";
        }
    }

    public double getReferenceValue(Workspace workspace, Obj inputObject) {
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        double fixedValue = parameters.getValue(REFERENCE_VALUE);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);
        double refMultiplier = parameters.getValue(REFERENCE_MULTIPLIER);

        // Getting the values to filter on
        double refValue;
        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                refValue = fixedValue;
                refMultiplier = 1;
                break;
            case ReferenceModes.IMAGE_MEASUREMENT:
                refValue = workspace.getImage(refImage).getMeasurement(refImageMeas).getValue();
                break;
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                Obj parentObject = inputObject.getParent(refParent);
                if (parentObject == null)
                    return Double.NaN;
                refValue = parentObject.getMeasurement(refParentMeas).getValue();
                break;
            default:
                return Double.NaN;
        }

        return refValue * refMultiplier;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIXED_VALUE, ReferenceModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 1d));
        parameters.add(new InputImageP(REFERENCE_VAL_IMAGE, this));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(REFERENCE_VAL_PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_OBJECT_MEASUREMENT, this));
        parameters.add(new DoubleP(REFERENCE_MULTIPLIER, this, 1d));

        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));

        addAbstractNumericParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                break;

            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_VAL_IMAGE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                String referenceValueImageName = parameters.getValue(REFERENCE_VAL_IMAGE);
                ((ImageMeasurementP) parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT))
                        .setImageName(referenceValueImageName);
                break;

            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                String referenceValueParentObjectsName = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
                ((ParentObjectsP) parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT))
                        .setChildObjectsName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT))
                        .setObjectName(referenceValueParentObjectsName);
                break;
        }

        return returnedParameters;

    }

    public ParameterCollection updateAndGetMeasurementParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));

        return returnedParameters;

    }

    protected void addAbstractNumericParameterDescriptions() {
        parameters.get(STORE_INDIVIDUAL_RESULTS).setDescription(
                "When selected, each input object will be assigned a measurement reporting if that object passed or failed the filter.  The measurement value is \"1\" for objects that failed the filter (i.e. would be removed if the relevant removal setting was enabled) and \"0\" for objects that passed (i.e. wouldn't be removed).");

        parameters.get(STORE_SUMMARY_RESULTS).setDescription(
                "When selected, a metadata value is stored in the workspace, which records the number of objects which failed the filter and were removed or moved to another object class (depending on the \""+FILTER_MODE+"\" parameter).");

    }
}