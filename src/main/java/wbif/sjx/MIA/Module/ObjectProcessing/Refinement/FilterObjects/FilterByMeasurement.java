package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

public class FilterByMeasurement extends AbstractNumericObjectFilter {    
    public static final String MEASUREMENT = "Measurement to filter on";   
    
    public FilterByMeasurement(ModuleCollection modules) {
        super("Based on measurement",modules);
    }
        
        
    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }
    
    @Override
    public String getDescription() {
        return "";
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
            
            // Removing the object if it has no children
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
}
