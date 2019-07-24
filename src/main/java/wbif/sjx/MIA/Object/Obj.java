package wbif.sjx.MIA.Object;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume;
import wbif.sjx.common.Object.Volume2.PointVolume;
import wbif.sjx.common.Object.Volume2.QuadTreeVolume;
import wbif.sjx.common.Object.Volume2.Volume2;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;

/**
 * Created by Stephen on 30/04/2017.
 */
public class Obj extends QuadTreeVolume {
    private String name;

    /**
     * Unique instance ID for this object
     */
    private int ID;

    /**
     * Each instance of an object is only present in XYZ and a single timepoint.  Any other dimensionality
     * (e.g. channel) must be added as a measurement.
     */
    private int T = 0;

    private LinkedHashMap<String, Obj> parents = new LinkedHashMap<>();
    private LinkedHashMap<String, ObjCollection> children = new LinkedHashMap<>();
    private LinkedHashMap<String, Measurement> measurements = new LinkedHashMap<>();


    // CONSTRUCTORS

    public Obj(String name, int ID, int width, int height, int nSlices, double dppXY, double dppZ, String calibratedUnits) {
        super(width,height,nSlices,dppXY,dppZ,calibratedUnits);

        this.name = name;
        this.ID = ID;

    }

    public Obj(String name, int ID, Volume2 exampleVolume) {
        super(exampleVolume);

        this.name = name;
        this.ID = ID;

    }


    // PUBLIC METHODS

    public void addMeasurement(Measurement measurement) {
        if (measurement == null) return;
        measurements.put(measurement.getName(), measurement);

    }

    public Measurement getMeasurement(String name) {
        if (measurements.get(name) == null) return null;
        return measurements.get(name);

    }

    public void removeMeasurement(String name) {
        measurements.remove(name);

    }

    @Override
    public String toString() {
        return "Object " + name + ", ID = "+ID + ", frame = "+ T ;

    }

    public String getName() {
        return name;
    }

    public Obj setName(String name) {
        this.name = name;
        return this;
    }

    public int getID() {
        return ID;
    }

    public Obj setID(int ID) {
        this.ID = ID;
        return this;
    }

    public int getT() {
        return T;
    }

    public Obj setT(int t) {
        T = t;
        return this;
    }

    public LinkedHashMap<String, Obj> getParents(boolean useFullHierarchy) {
        if (!useFullHierarchy) return parents;

        // Adding each parent and then the parent of that
        LinkedHashMap<String,Obj> parentHierarchy = new LinkedHashMap<>(parents);

        // Going through each parent, adding the parents of that.
        for (Obj parent:parents.values()) {
            if (parent == null) continue;

            LinkedHashMap<String,Obj> currentParents = parent.getParents(true);
            if (currentParents == null) continue;

            parentHierarchy.putAll(currentParents);

        }

        return parentHierarchy;

    }

    public void setParents(LinkedHashMap<String, Obj> parents) {
        this.parents = parents;
    }

    public Obj getParent(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first parent
        Obj parent = parents.get(elements[0]);

        // If the first parent was the only one listed, returning this
        if (elements.length == 1) return parent;

        // If there are additional parents listed, re-constructing the string and running this method on the parent
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=1;i<elements.length;i++) {
            stringBuilder.append(elements[i]);
            if (i != elements.length-1) stringBuilder.append(" // ");
        }

        if (parent == null) return null;

        return parent.getParent(stringBuilder.toString());

    }

    public void addParent(Obj parent) {
        parents.put(parent.getName(), parent);
    }

    public void addParent(String name, Obj parent) {
        parents.put(name, parent);
    }

    public void removeParent(String name) {
        parents.remove(name);

    }

    public LinkedHashMap<String, ObjCollection> getChildren() {
        return children;
    }

    public ObjCollection getChildren(String name) {
        // Split name down by " // " tokenizer
        String[] elements = name.split(" // ");

        // Getting the first set of children
        ObjCollection allChildren = children.get(elements[0]);
        if (allChildren == null) return new ObjCollection(elements[0]);

        // If the first set of children was the only one listed, returning this
        if (elements.length == 1) return allChildren;

        // If there are additional parents listed, re-constructing the string and running this method on the parent
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=1;i<elements.length;i++) {
            stringBuilder.append(elements[i]);
            if (i != elements.length-1) stringBuilder.append(" // ");
        }

        // Going through each child in the current set, then adding all their children to the output set
        ObjCollection outputChildren = new ObjCollection(name);
        for (Obj child:allChildren.values()) {
            ObjCollection currentChildren = child.getChildren(stringBuilder.toString());
            for (Obj currentChild:currentChildren.values()) outputChildren.add(currentChild);
        }

        return outputChildren;

    }

    public void setChildren(LinkedHashMap<String, ObjCollection> children) {
        this.children = children;
    }

    public void addChildren(ObjCollection childSet) {
        children.put(childSet.getName(), childSet);
    }

    public void removeChildren(String name) {
        children.remove(name);
    }

    public void addChild(Obj child) {
        String childName = child.getName();

        children.computeIfAbsent(childName, k -> new ObjCollection(childName));
        children.get(childName).put(child.getID(), child);

    }

    public void removeChild(Obj child) {
        String childName = child.getName();
        children.get(childName).values().remove(child);

    }

    /**
     * Removes itself from any other objects as a parent or child.
     */
    public void removeRelationships() {
        // Removing itself as a child from its parent
        if (parents != null) {
            for (Obj parent:parents.values()) {
                if (parent != null) parent.removeChild(this);

            }
        }

        // Removing itself as a parent from any children
        if (children != null) {
            for (ObjCollection childSet:children.values()) {
                for (Obj child:childSet.values()) {
                    child.removeParent(name);

                }
            }
        }
    }

    public LinkedHashMap<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(LinkedHashMap<String, Measurement> measurements) {
        this.measurements = measurements;

    }

    public Roi getRoi(int slice) {
        // Getting the image corresponding to this slice
        TreeSet<Point<Integer>> slicePoints = getSlicePoints(slice);
        Obj sliceObj = new Obj("Slice",ID,width,height,nSlices,dppXY,dppZ,calibratedUnits);
        sliceObj.setPoints(slicePoints);

        ObjCollection objectCollection = new ObjCollection("ProjectedObjects");
        objectCollection.add(sliceObj);

        double[][] extents = getExtents(true,false);
        ImagePlus sliceIpl = IJ.createImage("SliceIm",(int)extents[0][1]+1,(int)extents[1][1]+1,1,8);

        HashMap<Integer,Float> hues = ColourFactory.getSingleColourHues(objectCollection,ColourFactory.SingleColours.WHITE);
        Image objectImage = objectCollection.convertObjectsToImage("Output",new Image("Template",sliceIpl), hues, 8,false);
        IJ.run(objectImage.getImagePlus(), "Invert", "stack");

        ImageProcessor ipr = objectImage.getImagePlus().getProcessor();
        ipr.setThreshold(0,0, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection selection = new ThresholdToSelection();

        return selection.convert(objectImage.getImagePlus().getProcessor());

    }

    public void addPointsFromRoi(Roi roi, int z) throws IntegerOverflowException {
        for (java.awt.Point point:roi.getContainedPoints()) {
            add((int) point.getX(),(int) point.getY(),z);
        }
    }

    public Image getAsImage(String imageName) {
        double[][] range = getExtents(true,false);
        int width = (int) (range[0][1]-range[0][0]+1);
        int height = (int) (range[1][1]-range[1][0]+1);
        int depth = (int) (range[2][1]-range[2][0]+1);

        ImagePlus ipl = IJ.createImage(imageName,width,height,depth,8);

        for (Point<Integer> point:getPoints()) {
            int x = point.getX()- (int) range[0][0];
            int y = point.getY()- (int) range[1][0];
            int z = point.getZ()- (int) range[2][0];

            ipl.setPosition(z+1);
            ipl.getProcessor().putPixel(x,y,255);

        }

        return new Image(imageName,ipl);

    }

    public TreeSet<Point<Integer>> getSlicePoints(int slice) {
        TreeSet<Point<Integer>> slicePoints = new TreeSet<>();

        for (Point<Integer> point:getPoints()) {
            if (point.getZ()==slice) slicePoints.add(point);
        }

        return slicePoints;

    }

    public Image convertObjToImage(String outputName, @Nullable Image templateImage) {
        // Creating an ObjCollection to hold this image
        ObjCollection tempObj = new ObjCollection(outputName);
        tempObj.add(this);

        // Getting the image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(tempObj,ColourFactory.SingleColours.WHITE);
        return tempObj.convertObjectsToImage(outputName,templateImage,hues,8,false);

    }

    public Image convertObjToImage(String outputName) {
        // Creating an ObjCollection to hold this image
        ObjCollection tempObj = new ObjCollection(outputName);
        tempObj.add(this);

        // Getting the image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(tempObj,ColourFactory.SingleColours.WHITE);
        return tempObj.convertObjectsToImage(outputName,null,hues,8,false);

    }

    public void cropToImageSize(Image templateImage) {
        int width = templateImage.getImagePlus().getWidth();
        int height = templateImage.getImagePlus().getHeight();
        int nSlices = templateImage.getImagePlus().getNSlices();

        getPoints().removeIf(point -> point.getX() < 0 || point.getX() >= width
                || point.getY() < 0 || point.getY() >= height
                || point.getZ() < 0 || point.getZ() >= nSlices);

    }

    @Override
    public int hashCode() {
        // Updating the hash for time-point.  ID, measurements and relationships aren't included; only spatial location.
        return super.hashCode()*31 + T;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;

        if (obj == this) return true;
        if (!(obj instanceof Obj)) return false;

        return (T == ((Obj) obj).T);

    }
}
