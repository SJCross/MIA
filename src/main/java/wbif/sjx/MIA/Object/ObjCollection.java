package wbif.sjx.MIA.Object;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by sc13967 on 12/05/2017.
 */
public class ObjCollection extends LinkedHashMap<Integer,Obj> {
    private String name;
    private int maxID = 0;

    public interface ColourModes {
        String SINGLE_COLOUR = "Single colour";
        String RANDOM_COLOUR = "Random colour";
        String MEASUREMENT_VALUE = "Measurement value";
        String ID = "ID";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, ID, PARENT_ID, PARENT_MEASUREMENT_VALUE};

    }


    public ObjCollection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Obj object) {
        put(object.getID(),object);

    }

    public int getAndIncrementID() {
        maxID++;
        return maxID;
    }

    public Obj getFirst() {
        if (size() == 0) return null;

        return values().iterator().next();

    }

    public int[][] getSpatialLimits() {
        int[][] limits = new int[][]{
                {Integer.MAX_VALUE,-Integer.MAX_VALUE},
                {Integer.MAX_VALUE,-Integer.MAX_VALUE},
                {Integer.MAX_VALUE,Integer.MIN_VALUE}};

        for (Obj object:values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            for (int i=0;i<x.size();i++) {
                limits[0][0] = Math.min(limits[0][0],x.get(i));
                limits[0][1] = Math.max(limits[0][1],x.get(i));
                limits[1][0] = Math.min(limits[1][0],y.get(i));
                limits[1][1] = Math.max(limits[1][1],y.get(i));
                limits[2][0] = Math.min(limits[2][0],z.get(i));
                limits[2][1] = Math.max(limits[2][1],z.get(i));

            }
        }

        return limits;

    }

    public int[] getTemporalLimits() {
        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = new int[2];
        limits[0] = Integer.MAX_VALUE;
        limits[1] = -Integer.MAX_VALUE;

        for (Obj object:values()) {
            if (object.getT() < limits[0]) limits[0] = object.getT();
            if (object.getT() > limits[1]) limits[1] = object.getT();

        }

        return limits;

    }

    public int getLargestID() {
        int largestID = 0;
        for (Obj obj:values()) {
            if (obj.getID() > largestID) largestID = obj.getID();
        }

        return largestID;

    }

    public Image convertObjectsToImage(String outputName, @Nullable Image templateImage, HashMap<Integer,Float> hues, int bitDepth, boolean nanBackground) {
        ImagePlus templateIpl = templateImage == null ? null : templateImage.getImagePlus();
        ImagePlus ipl;

        if (templateIpl == null) {
            // Getting range of object pixels
            int[][] spatialLimits = getSpatialLimits();
            int[] temporalLimits = getTemporalLimits();

            // Creating a new image
            ipl = IJ.createHyperStack(outputName, spatialLimits[0][1] + 1,spatialLimits[1][1] + 1,
                    1, spatialLimits[2][1] + 1, temporalLimits[1] + 1,bitDepth);
        } else {
            ipl = IJ.createHyperStack(outputName,templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),bitDepth);
        }

        // If it's a 32-bit image, set all background pixels to NaN
        if (bitDepth == 32 && nanBackground) {
            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int c = 1; c <= ipl.getNChannels(); c++) {
                    for (int t = 1; t <= ipl.getNFrames(); t++) {
                        for (int x=0;x<ipl.getWidth();x++) {
                            for (int y=0;y<ipl.getHeight();y++) {
                                ipl.setPosition(c,z,t);
                                ipl.getProcessor().putPixelValue(x,y,Double.NaN);
                            }
                        }
                    }
                }
            }
        }

        // Labelling pixels in image
        for (Obj object:values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();
            Integer tPos = object.getT();

            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(1,zPos+1,tPos+1);

                float hue = hues.get(object.getID());
                switch (bitDepth) {
                    case 8:
                    case 16:
                        ipl.getProcessor().putPixel(x.get(i), y.get(i), Math.round(hue*255));
                        break;
                    case 32:
                        ipl.getProcessor().putPixelValue(x.get(i), y.get(i), hue);
                        break;
                }
            }
        }

        // Assigning the spatial calibration from the template image
        if (templateIpl != null) {
            ipl.getCalibration().pixelWidth = templateIpl.getCalibration().getX(1);
            ipl.getCalibration().pixelHeight = templateIpl.getCalibration().getY(1);
            ipl.getCalibration().pixelDepth = templateIpl.getCalibration().getZ(1);
            ipl.getCalibration().setUnit(templateIpl.getCalibration().getUnit());
        } else if (getFirst() != null) {
            Obj first = getFirst();
            ipl.getCalibration().pixelWidth = first.getDistPerPxXY();
            ipl.getCalibration().pixelHeight = first.getDistPerPxXY();
            ipl.getCalibration().pixelDepth = first.getDistPerPxZ();
            ipl.getCalibration().setUnit(first.getCalibratedUnits());
        }

        return new Image(outputName,ipl);

    }

    /*
     * Returns the Obj with coordinates matching the Obj passed as an argument.  Useful for unit tests.
     */
    public Obj getByEquals(Obj referenceObj) {
        for (Obj testObj:values()) {
            if (testObj.equals(referenceObj)) return testObj;
        }

        return null;

    }

    public void resetCollection() {
        clear();
        maxID = 0;
    }

    /**
     * Displays measurement values from a specific Module
     * @param module
     */
    public void showMeasurements(Module module, ModuleCollection modules) {
        // Getting MeasurementReferences
        MeasurementRefCollection measRefs = module.updateAndGetObjectMeasurementRefs(modules);

        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Getting a list of all measurements relating to this object collection
        LinkedHashSet<String> measNames = new LinkedHashSet<>();
        for (MeasurementRef measRef:measRefs.values()) {
            if (measRef.getImageObjName().equals(name) && measRef.isCalculated()) measNames.add(measRef.getName());
        }

        // Iterating over each measurement, adding all the values
        int row = 0;
        for (Obj obj:values()) {
            if (row != 0) rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID",row,obj.getID());
            rt.setValue("X_CENTROID (PX)",row,obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)",row,obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)",row,obj.getZMean(true,false));
            rt.setValue("TIMEPOINT",row,obj.getT());

            // Setting the measurements from the Module
            for (String measName : measNames) {
                Measurement measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName,row,value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("\""+module.getTitle()+" \"measurements for \""+name+"\"");

    }

    public void showAllMeasurements() {
        // Creating a new ResultsTable for these values
        ResultsTable rt = new ResultsTable();

        // Iterating over each measurement, adding all the values
        int row = 0;
        for (Obj obj:values()) {
            if (row != 0) rt.incrementCounter();

            // Setting some common values
            rt.setValue("ID",row,obj.getID());
            rt.setValue("X_CENTROID (PX)",row,obj.getXMean(true));
            rt.setValue("Y_CENTROID (PX)",row,obj.getYMean(true));
            rt.setValue("Z_CENTROID (SLICE)",row,obj.getZMean(true,false));
            rt.setValue("TIMEPOINT",row,obj.getT());

            // Setting the measurements from the Module
            Set<String> measNames = obj.getMeasurements().keySet();
            for (String measName : measNames) {
                Measurement measurement = obj.getMeasurement(measName);
                double value = measurement == null ? Double.NaN : measurement.getValue();

                // Setting value
                rt.setValue(measName,row,value);

            }

            row++;

        }

        // Displaying the results table
        rt.show("All measurements for \""+name+"\"");

    }
}
