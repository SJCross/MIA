package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import ij.measure.ResultsTable;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;

import java.util.LinkedHashSet;

public abstract class CoreMacroRunner extends Module {
    protected CoreMacroRunner(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public static String getFullName(String assignedName) {
        return "MACRO // " + assignedName;
    }

    public static LinkedHashSet<String> expectedMeasurements(ParameterGroup group,String measurementHeading) {
        LinkedHashSet<String> addedMeasurements = new LinkedHashSet<>();

        LinkedHashSet<ParameterCollection> collections = group.getCollections();
        for (ParameterCollection collection:collections) {
            String heading = collection.getValue(measurementHeading);
            addedMeasurements.add(heading);
        }

        return addedMeasurements;

    }

    public static Measurement interceptMeasurement(ResultsTable table, String heading) {
        if (table == null || table.getColumn(0) == null) return new Measurement(getFullName(heading),Double.NaN);

        int nRows = table.getColumn(0).length;
        double value = table.getValue(heading,nRows-1);

        return new Measurement(getFullName(heading),value);

    }
}
