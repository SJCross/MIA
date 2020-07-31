package wbif.sjx.MIA.Module.Miscellaneous;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

public class GlobalVariables extends Module {
    public static final String ADD_NEW_VARIABLE = "Add new variable";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_VALUE = "Variable value";

    private static final HashMap<StringP, String> globalParameters = new HashMap<>();

    public GlobalVariables(ModuleCollection modules) {
        super("Global variables", modules);
    }

    public static String convertString(String string, ModuleCollection modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            for (StringP nameP : globalParameters.keySet()) {
                if (nameP.getValue().equals(metadataName)) {
                    String value = globalParameters.get(nameP);
                    string = string.replace(fullName, value);
                    break;
                }
            }
        }

        return string;

    }

    public static boolean variablesPresent(String string, ModuleCollection modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        // Re-compiling the matcher
        matcher = pattern.matcher(string);
        if (matcher.find()) {
            String metadataName = matcher.group(1);

            boolean found = false;
            for (StringP name : globalParameters.keySet()) {
                if (name.getValue().equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current parameter wasn't found, return false
            return found;

        }

        return true;

    }

    public static boolean containsValue(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static int count() {
        return globalParameters.size();
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        return Status.PASS;
    }

    @Override
    protected void initialiseParameters() {
        ParameterCollection parameterCollection = new ParameterCollection();
        parameterCollection.add(new StringP(VARIABLE_NAME, this));
        parameterCollection.add(new StringP(VARIABLE_VALUE, this));

        parameters.add(new ParameterGroup(ADD_NEW_VARIABLE, this, parameterCollection));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterGroup group = parameters.getParameter(ADD_NEW_VARIABLE);
        if (group == null)
            return parameters;

        LinkedHashMap<Integer, ParameterCollection> collections = group.getCollections(false);
        for (ParameterCollection collection : collections.values()) {
            StringP variableName = (StringP) collection.get(VARIABLE_NAME);
            if (isRunnable() && isEnabled())
                globalParameters.put(variableName, collection.getValue(VARIABLE_VALUE));
            else if (globalParameters.containsKey(variableName))
                globalParameters.remove(variableName);
        }

        return parameters;

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
