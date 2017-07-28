package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 29/06/2017.
 */
public class MeasureObjectShape extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";

    private static final String N_VOXELS = "N_VOXELS";

    @Override
    public String getTitle() {
        return "Measure object shape";
    }

    @Override
    public String getHelp() {
        return "+++INCOMPLETE+++" +
                "\nCurrently only measures the number of voxels per object";
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) throws GenericMIAException {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectName);

        // Running through each object, making the measurements
        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getCoordinates(Obj.X);

            // Adding the relevant measurements
            inputObject.addMeasurement(new MIAMeasurement(N_VOXELS,x.size(),this));

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        measurements.addMeasurement(inputObjectsName,N_VOXELS);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
