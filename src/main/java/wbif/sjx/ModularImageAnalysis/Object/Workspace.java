package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.common.Object.HCMetadata;

import java.io.File;
import java.util.*;

/**
 * Created by sc13967 on 02/05/2017.
 */
public class Workspace {
    private LinkedHashMap<String, ObjCollection> objects = new LinkedHashMap<>();
    private LinkedHashMap<String, Image> images = new LinkedHashMap<>();
    private HCMetadata metadata = new HCMetadata();
    private int ID;

    // CONSTRUCTOR

    public Workspace(int ID, File currentFile) {
        this.ID = ID;
        metadata.put(HCMetadata.FILE,currentFile);

    }

    // PUBLIC METHODS

    public void addObjects(ObjCollection object) {
        objects.put(object.getName(), object);
    }

    /**
     * Adds the provided Obj to the relevant ObjCollection.
     * If there isn't already such a collection, one is created.
     * @param obj
     */
    public void addObject(Obj obj) {
        String objectName = obj.getName();
        objects.putIfAbsent(objectName,new ObjCollection(objectName));
        objects.get(objectName).add(obj);

    }

    public void removeObject(String name) {
        objects.remove(name);

        // Running garbage collector
        Runtime.getRuntime().gc();

    }

    public void addImage(Image image) {
        images.put(image.getName(), image);
    }

    public void removeImage(String name) {
        images.remove(name);

        // Running garbage collector
        Runtime.getRuntime().gc();

    }

    /**
     * Used to reduce memory of the workspace (particularly for batch processing).
     * @param retainMeasurements Delete image data, but leave measurements
     */
    public void clearAllImages(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (Image image:images.values()) {
                image.setImagePlus(null);
            }

        } else {
            // Removes all the data
            images = null;
        }
    }

    public void clearAllObjects(boolean retainMeasurements) {
        if (retainMeasurements) {
            // Sets the ImagePlus to null, but leaves measurements
            for (ObjCollection objCollection :objects.values()) {
                for (Obj obj: objCollection.values()) {
                    obj.setPoints(null);
                    obj.clearSurface();

                }
            }

        } else {
            // Removes all the data
            objects = null;
        }
    }

    public Image getImage(String name) {
        return images.get(name);

    }

    public ObjCollection getObjectSet(String name) {
        return objects.get(name);

    }

    /**
     * Creates a structure containing new Workspaces, each of which represent a different time point
     * @return
     */
    public HashMap<Integer,Workspace> getSingleTimepointWorkspaces() {
        HashMap<Integer,Workspace> workspaces = new HashMap<>();

        for (ObjCollection collection:objects.values()) {
            for (Obj obj:collection.values()) {
                int t = obj.getT();

                // If there isn't already a Workspace for this time point, add one
                if (!workspaces.containsKey(t)) {
                    Workspace workspace = new Workspace(ID,null);
                    workspace.setMetadata(metadata);
                    workspace.setImages(images);
                    workspaces.put(t,workspace);
                }

                // Adding the current Obj to the new Workspace
                workspaces.get(t).addObject(obj);

            }
        }

        return workspaces;

    }


    // GETTERS AND SETTERS

    public HashMap<String, ObjCollection> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<String, ObjCollection> objects) {
        this.objects = objects;
    }

    public HashMap<String, Image> getImages() {
        return images;
    }

    public void setImages(LinkedHashMap<String, Image> images) {
        this.images = images;
    }

    public HCMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(HCMetadata metadata) {
        this.metadata = metadata;
    }

    public int getID() {
        return ID;
    }
}
