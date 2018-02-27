package wbif.sjx.ModularImageAnalysis.Object;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

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

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, ID, PARENT_ID};

    }

    public interface LabelModes {
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";

        String[] ALL = new String[]{ID,MEASUREMENT_VALUE,PARENT_ID};

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

    public int getNextID() {
        maxID++;
        return maxID;
    }

    public int[][] getSpatialLimits() {
        int[][] limits = new int[][]{
                {Integer.MAX_VALUE,Integer.MIN_VALUE},
                {Integer.MAX_VALUE,Integer.MIN_VALUE},
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

    public int[] getTimepointLimits() {
        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = new int[2];
        limits[0] = Integer.MAX_VALUE;
        limits[1] = Integer.MIN_VALUE;

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

    public Image convertObjectsToImage(String outputName, ImagePlus templateIpl, String colourMode, HashMap<Integer,Float> hues, boolean hideMissing) {
        ImagePlus ipl;
        int bitDepth = 8;
        switch (colourMode){
            case ColourModes.RANDOM_COLOUR:
            case ColourModes.SINGLE_COLOUR:
                bitDepth = 8;
                break;

            case ColourModes.MEASUREMENT_VALUE:
            case ColourModes.ID:
            case ColourModes.PARENT_ID:
                bitDepth = 32;
                break;

        }

        if (templateIpl == null) {
            // Getting range of object pixels
            int[][] coordinateRange = new int[4][2];

            for (Obj object : values()) {
                // Getting range of XYZ
                int[][] currCoordinateRange = object.getCoordinateRange();
                for (int dim = 0; dim < currCoordinateRange.length; dim++) {
                    if (currCoordinateRange[dim][0] < coordinateRange[dim][0]) {
                        coordinateRange[dim][0] = currCoordinateRange[dim][0];
                    }

                    if (currCoordinateRange[dim][1] > coordinateRange[dim][1]) {
                        coordinateRange[dim][1] = currCoordinateRange[dim][1];
                    }
                }

                // Getting range of timepoints
                int currTimepoint = object.getT();
                if (currTimepoint < coordinateRange[3][0]) {
                    coordinateRange[3][0] = currTimepoint;
                }

                if (currTimepoint > coordinateRange[3][1]) {
                    coordinateRange[3][1] = currTimepoint;
                }
            }

            // Creating a new image
            ipl = IJ.createHyperStack(outputName, coordinateRange[0][1] + 1,coordinateRange[1][1] + 1,
                    1, coordinateRange[2][1] + 1, coordinateRange[3][1] + 1,bitDepth);

        } else {
            ipl = IJ.createHyperStack(outputName,templateIpl.getWidth(),templateIpl.getHeight(),
                    templateIpl.getNChannels(),templateIpl.getNSlices(),templateIpl.getNFrames(),bitDepth);

        }

        // If it's a 32-bit image, set all background pixels to NaN
        if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
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

                if (colourMode.equals(ColourModes.SINGLE_COLOUR) | colourMode.equals(ColourModes.RANDOM_COLOUR)) {
                    ipl.getProcessor().putPixel(x.get(i), y.get(i), (int) Math.round(hues.get(object.getID())*255));

                } else if (colourMode.equals(ColourModes.MEASUREMENT_VALUE) | colourMode.equals(ColourModes.ID)
                        | colourMode.equals(ColourModes.PARENT_ID)) {
                    ipl.getProcessor().putPixelValue(x.get(i), y.get(i), hues.get(object.getID()));

                }
            }
        }

        // Assigning the spatial calibration from the template image
        if (templateIpl != null) {
            ipl.getCalibration().pixelWidth = templateIpl.getCalibration().getX(1);
            ipl.getCalibration().pixelHeight = templateIpl.getCalibration().getY(1);
            ipl.getCalibration().pixelDepth = templateIpl.getCalibration().getZ(1);
            ipl.getCalibration().setUnit(templateIpl.getCalibration().getUnit());

        }

        return new Image(outputName,ipl);

    }

    public HashMap<Integer,String> getIDs(String labelMode, String measurementForID, String parentObjectsForID, int nDecimalPlaces) {
        HashMap<Integer,String> IDs = new HashMap<>();

        DecimalFormat df;
        if (nDecimalPlaces == 0) {
            df = new DecimalFormat("0");
        } else {
            String zeros = "0.";
            for (int i=0;i<nDecimalPlaces;i++) {
                zeros = zeros + "0";
            }
            zeros = zeros+"E0";
            df = new DecimalFormat(zeros);
        }

        for (Obj object:values()) {
            switch (labelMode) {
                case LabelModes.ID:
                    IDs.put(object.getID(),df.format(object.getID()));
                    break;

                case LabelModes.MEASUREMENT_VALUE:
                    IDs.put(object.getID(), df.format(object.getMeasurement(measurementForID).getValue()));
                    System.out.println(IDs);
                    break;

                case LabelModes.PARENT_ID:
                    IDs.put(object.getID(), df.format(object.getParent(parentObjectsForID).getID()));
                    break;
            }
        }

        return IDs;

    }

    public HashMap<Integer,Float> getHue(String colourMode, String measurementForColour, String parentObjectsForColour, boolean normalised) {
        HashMap<Integer,Float> hues = new HashMap<>();

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
            values().forEach(e -> cs.addMeasure(e.getMeasurement(measurementForColour).getValue()));
        }

        for (Obj object:values()) {
            int ID = object.getID();

            // Default hue value in case none is assigned
            float H = 0f;

            switch (colourMode) {
                case ColourModes.SINGLE_COLOUR:
                   H = 1f;
                   break;

                case ColourModes.RANDOM_COLOUR:
                    // Random colours
                    H = new Random().nextFloat();
                    break;

                case ColourModes.MEASUREMENT_VALUE:
                    H = (float) object.getMeasurement(measurementForColour).getValue();
                    if (normalised) {
                        double startH = 0;
                        double endH = 120d / 255d;
                        H = (float) ((H - cs.getMin()) * (endH - startH) / (cs.getMax() - cs.getMin()) + startH);
                    }
                    break;

                case ColourModes.ID:
                    H = (float) object.getID();
                    if (normalised) H = (H* 1048576 % 255) / 255;
                    break;

                case ColourModes.PARENT_ID:
                    if (object.getParent(parentObjectsForColour) == null) {
                        H = 0.2f;
                    } else {
                        H = (float) object.getParent(parentObjectsForColour).getID();
                    }

                    if (normalised) H = (H* 1048576 % 255) / 255;

                    break;

            }

            hues.put(ID,H);

        }

        return hues;

    }

    /**
     * Returns the Obj with coordinates matching the Obj passed as an argument.  Useful for unit tests.
     * @param referenceObj
     * @return
     */
    public Obj getByEquals(Obj referenceObj) {
        for (Obj testObj:values()) {
            if (testObj.equals(referenceObj)) return testObj;
        }

        return null;

    }
}
