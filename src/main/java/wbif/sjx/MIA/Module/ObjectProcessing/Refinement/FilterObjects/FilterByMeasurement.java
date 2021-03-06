package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;

public class FilterByMeasurement extends AbstractNumericObjectFilter {    
    public static final String MEASUREMENT = "Measurement to filter on";   
    
    public FilterByMeasurement(ModuleCollection modules) {
        super("Based on measurement",modules);
    }
        
        

    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }
    
    @Override
    public String getDescription() {
        return "Filter an object collection based on a measurement value associated with this object.  The threshold (reference) value can be either a fixed value (same for all objects), a measurement associated with an image (same for all objects within a single analysis run) or a measurement associated with a parent object (potentially different for all objects).  Objects which satisfy the specified numeric filter (less than, equal to, greater than, etc.) can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.";
    }
    
    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        
        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String measName = parameters.getValue(MEASUREMENT);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS);
        
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);
        
        
        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName,inputObjects) : null;
        
        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            
            // Skipping this object if it doesn't have the measurement
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null) continue;
            
            double value = measurement.getValue();
            double refValue = getReferenceValue(workspace, inputObject);
            
            // Checking for blank measurements
            if (Double.isNaN(refValue) || Double.isNaN(value)) continue;
                       
            // Checking the main filter
            boolean conditionMet = testFilter(value,refValue,filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = getIndividualMeasurementName(measName);
                inputObject.addMeasurement(new Measurement(measurementName,conditionMet ? 1 : 0));
            }

            if (conditionMet) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
        
        // If moving objects, addRef them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);
        
        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String metadataName = getSummaryMeasurementName(measName);
            workspace.getMetadata().put(metadataName,count);
        }
        
        // Showing objects
        if (showOutput) inputObjects.convertToImageRandomColours().showImage();
        
        return Status.PASS;
        
    }
    
    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

        addParameterDescriptions();
                
    }
    
    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addAll(super.updateAndGetParameters());

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
                
        returnedParameters.addAll(updateAndGetMeasurementParameters());

        return returnedParameters;
        
    }
    
    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }
    
    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = super.updateAndGetObjectMeasurementRefs();
        
        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS)) {
            String measName = parameters.getValue(MEASUREMENT);
            String measurementName = getIndividualMeasurementName(measName);
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

            returnedRefs.add(new ObjMeasurementRef(measurementName,inputObjectsName));
            if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
                returnedRefs.add(new ObjMeasurementRef(measurementName,outputObjectsName));
            }            
        }
        
        return returnedRefs;
        
    }
    
    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS)) {
            String measName = parameters.getValue(MEASUREMENT);
            String metadataName = getSummaryMeasurementName(measName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(MEASUREMENT).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""+new FilterWithWithoutMeasurement(null).getName()+"\".");

    }
}
