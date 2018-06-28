package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.common.Analysis.TextureCalculator;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Takes a set of objects and measures intensity texture values on a provided image.  Measurements are stored with the
 * objects.
 */
public class MeasureObjectTexture extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String POINT_MEASUREMENT = "Measurements based on centroid point";
    public static final String MEASUREMENT_RADIUS = "Measurement radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";
    public static final String X_OFFSET = "X-offset";
    public static final String Y_OFFSET = "Y-offset";
    public static final String Z_OFFSET = "Z-offset";
    public static final String CALIBRATED_OFFSET = "Calibrated offset";

    public interface Measurements {
        String ASM = "ASM";
        String CONTRAST = "CONTRAST";
        String CORRELATION = "CORRELATION";
        String ENTROPY = "ENTROPY";

    }


    public static String getFullName(String imageName, String measurement) {
        return "TEXTURE // "+imageName+"_"+measurement;
    }


    int[] convertCalibratedOffsets(double[] offsIn, Obj referenceObject) {
        double dppXY = referenceObject.getDistPerPxXY();
        double dppZ = referenceObject.getDistPerPxZ();

        int[] offsOut = new int[3];
        offsOut[0] = (int) Math.round(offsIn[0]/dppXY);
        offsOut[1] = (int) Math.round(offsIn[1]/dppXY);
        offsOut[2] = (int) Math.round(offsIn[2]/dppZ);

        return offsOut;

    }

    ObjCollection getLocalObjectRegion(ObjCollection objects, double radius, boolean calibrated, ImagePlus inputImagePlus) {
        // Getting local object region
        GetLocalObjectRegion getLocalObjectRegion = (GetLocalObjectRegion) new GetLocalObjectRegion()
                .updateParameterValue(GetLocalObjectRegion.OUTPUT_OBJECTS,objects.getName())
                .updateParameterValue(GetLocalObjectRegion.LOCAL_RADIUS,radius)
                .updateParameterValue(GetLocalObjectRegion.CALIBRATED_RADIUS,calibrated)
                .updateParameterValue(GetLocalObjectRegion.USE_MEASUREMENT,false);

        objects = getLocalObjectRegion.getLocalRegions(objects,inputImagePlus);

        return objects;

    }

    public static void processObject(Obj object, Image image, TextureCalculator textureCalculator, boolean centroidMeasurement) {
        ImagePlus inputImagePlus = image.getImagePlus();

        int t = object.getT()+1;
        inputImagePlus.setPosition(1,1,t);
        textureCalculator.calculate(inputImagePlus.getImageStack(),object);

        // Acquiring measurements
        Measurement ASMMeasurement = new Measurement(getFullName(image.getName(), Measurements.ASM),textureCalculator.getASM());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(ASMMeasurement);
        } else {
            object.addMeasurement(ASMMeasurement);
        }

        Measurement contrastMeasurement = new Measurement(getFullName(image.getName(), Measurements.CONTRAST),textureCalculator.getContrast());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(contrastMeasurement);
        } else {
            object.addMeasurement(contrastMeasurement);
        }

        Measurement correlationMeasurement = new Measurement(getFullName(image.getName(), Measurements.CORRELATION),textureCalculator.getCorrelation());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(correlationMeasurement);
        } else {
            object.addMeasurement(correlationMeasurement);
        }

        Measurement entropyMeasurement = new Measurement(getFullName(image.getName(), Measurements.ENTROPY),textureCalculator.getEntropy());
        if (centroidMeasurement) {
            object.getParent(object.getName()).addMeasurement(entropyMeasurement);
        } else {
            object.addMeasurement(entropyMeasurement);
        }
    }

    @Override
    public String getTitle() {
        return "Measure object texture";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // If no objects were detected, skipping this module
        if (inputObjects.size() == 0) return;

        // Getting parameters
        double xOffsIn = parameters.getValue(X_OFFSET);
        double yOffsIn = parameters.getValue(Y_OFFSET);
        double zOffsIn = parameters.getValue(Z_OFFSET);
        boolean calibratedOffset = parameters.getValue(CALIBRATED_OFFSET);
        boolean centroidMeasurement = parameters.getValue(POINT_MEASUREMENT);
        double radius = parameters.getValue(MEASUREMENT_RADIUS);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);

        // If using calibrated offset values, determining the closest pixel offset
        if (calibratedOffset) {
            int[] offs = convertCalibratedOffsets(new double[]{xOffsIn,yOffsIn,zOffsIn},inputObjects.getFirst());
            xOffsIn = offs[0]; yOffsIn = offs[1]; zOffsIn = offs[2];
        }
        int xOffs = (int) xOffsIn; int yOffs = (int) yOffsIn; int zOffs = (int) zOffsIn;

        // If a centroid region is being used calculate the local region and reassign that to inputObjects reference
        if (centroidMeasurement) {
            inputObjects = getLocalObjectRegion(inputObjects,radius,calibrated,inputImagePlus);
        }

        // Running texture measurement
        writeMessage("Calculating co-occurance matrix");
        writeMessage("X-offset: "+xOffs);
        writeMessage("Y-offset: "+yOffs);
        writeMessage("Z-offset: "+zOffs);

        TextureCalculator textureCalculator = new TextureCalculator(xOffs,yOffs,zOffs);

        int nObjects = inputObjects.size();
        int iter = 1;
        for (Obj object:inputObjects.values()) {
            writeMessage("Processing object "+(iter++)+" of "+nObjects);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(POINT_MEASUREMENT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT_RADIUS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(X_OFFSET, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(Y_OFFSET, Parameter.DOUBLE,0d));
        parameters.add(new Parameter(Z_OFFSET, Parameter.DOUBLE,0d));
        parameters.add(new Parameter(CALIBRATED_OFFSET, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(POINT_MEASUREMENT));

        if (parameters.getValue(POINT_MEASUREMENT)) {
            returnedParameters.add(parameters.getParameter(CALIBRATED_RADIUS));
            returnedParameters.add(parameters.getParameter(MEASUREMENT_RADIUS));
        }

        returnedParameters.add(parameters.getParameter(X_OFFSET));
        returnedParameters.add(parameters.getParameter(Y_OFFSET));
        returnedParameters.add(parameters.getParameter(Z_OFFSET));
        returnedParameters.add(parameters.getParameter(CALIBRATED_OFFSET));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        String name = getFullName(inputImageName,Measurements.ASM);
        MeasurementReference asm = objectMeasurementReferences.getOrPut(name);
        asm.setImageObjName(inputObjectsName);
        asm.setCalculated(true);

        name = getFullName(inputImageName,Measurements.CONTRAST);
        MeasurementReference contrast = objectMeasurementReferences.getOrPut(name);
        contrast.setImageObjName(inputObjectsName);
        contrast.setCalculated(true);

        name = getFullName(inputImageName,Measurements.CORRELATION);
        MeasurementReference correlation = objectMeasurementReferences.getOrPut(name);
        correlation.setImageObjName(inputObjectsName);
        correlation.setCalculated(true);

        name = getFullName(inputImageName,Measurements.ENTROPY);
        MeasurementReference entropy = objectMeasurementReferences.getOrPut(name);
        entropy.setImageObjName(inputObjectsName);
        entropy.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
