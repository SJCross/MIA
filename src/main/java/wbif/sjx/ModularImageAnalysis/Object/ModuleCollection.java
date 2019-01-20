// TODO: Could add optional argument to getParametersMatchingType for the removal type (i.e. if it matches type 1 add
// to the list, but if it matches type 2 remove the same parameter from the list.  Would need to compare Parameters for
// value.

package wbif.sjx.ModularImageAnalysis.Object;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 03/05/2017.
 */
public class ModuleCollection extends ArrayList<Module> implements Serializable {
    public MeasurementReferenceCollection getImageMeasurementReferences(String imageName) {
        return getImageMeasurementReferences(imageName,null);
    }

    public MeasurementReferenceCollection getImageMeasurementReferences(String imageName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current image
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences = module.updateAndGetImageMeasurementReferences();

            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences.values()) {
                if (measurementReference.getImageObjName() == null) continue;
                if (measurementReference.getImageObjName().equals(imageName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

    public MeasurementReferenceCollection getObjectMeasurementReferences(String objectName) {
        return getObjectMeasurementReferences(objectName,null);

    }

    public MeasurementReferenceCollection getObjectMeasurementReferences(String objectName, Module cutoffModule) {
        MeasurementReferenceCollection measurementReferences = new MeasurementReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MeasurementReferenceCollection currentMeasurementReferences =
                    module.updateAndGetObjectMeasurementReferences();
            if (currentMeasurementReferences == null) continue;

            for (MeasurementReference measurementReference:currentMeasurementReferences.values()) {
                if (measurementReference.getImageObjName() == null) continue;
                if (measurementReference.getImageObjName().equals(objectName)
                        & measurementReference.isCalculated())
                    measurementReferences.add(measurementReference);

            }
        }

        return measurementReferences;

    }

    public MetadataReferenceCollection getMetadataReferences(Module cutoffModule) {
        MetadataReferenceCollection metadataReferences = new MetadataReferenceCollection();

        // Iterating over all modules, collecting any measurements for the current objects
        for (Module module:this) {
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            MetadataReferenceCollection currentMetadataReferences = module.updateAndGetMetadataReferences();
            if (currentMetadataReferences == null) continue;

            metadataReferences.putAll(currentMetadataReferences);

        }

        return metadataReferences;

    }

    /*
     * Returns an LinkedHashSet of all parameters of a specific type
     */
    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type, Module cutoffModule) {
        LinkedHashSet<T> parameters = new LinkedHashSet<>();

        for (Module module:this) {
            // If the current module is the cutoff the loop terminates.  This prevents the system offering measurements
            // that are created after this module or are currently unavailable.
            if (module == cutoffModule) break;
            if (!module.isEnabled()) continue;
            if (!module.isRunnable()) continue;

            // Running through all parameters, adding all images to the list
            ParameterCollection currParameters = module.updateAndGetParameters();
            if (currParameters != null) {
                for (Parameter currParameter : currParameters) {
                    if (type.isInstance(currParameter)) {
                        parameters.add((T) currParameter);
                    }
                }
            }
        }

        return parameters;

    }

    public <T extends Parameter> LinkedHashSet<T> getParametersMatchingType(Class<T> type) {
        return getParametersMatchingType(type,null);
    }

    public LinkedHashSet<OutputObjectsParam> getAvailableObjects(Module cutoffModule, boolean ignoreRemoved) {
        // Getting a list of available images
        LinkedHashSet<OutputObjectsParam> objects = getParametersMatchingType(OutputObjectsParam.class,cutoffModule);

        if (!ignoreRemoved) return objects;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<RemovedObjects> removedObjects = getParametersMatchingType(RemovedObjects.class,cutoffModule);
        for (Parameter removedObject:removedObjects) objects.remove(removedObject);

        return objects;

    }

    public LinkedHashSet<OutputObjectsParam> getAvailableObjects(Module cutoffModule) {
        return getAvailableObjects(cutoffModule,true);
    }

    public LinkedHashSet<OutputImageParam> getAvailableImages(Module cutoffModule) {
        return getAvailableImages(cutoffModule,true);
    }

    public LinkedHashSet<OutputImageParam> getAvailableImages(Module cutoffModule, boolean ignoreRemoved) {
        // Getting a list of available images
        LinkedHashSet<OutputImageParam> images = getParametersMatchingType(OutputImageParam.class,cutoffModule);

        if (!ignoreRemoved) return images;

        // Removing any objects which have since been removed from the workspace
        LinkedHashSet<RemovedImageParam> removedImageParams = getParametersMatchingType(RemovedImageParam.class,cutoffModule);
        for (Parameter removedImage: removedImageParams) images.remove(removedImage);

        return images;

    }

    public RelationshipCollection getRelationships(Module cutoffModule) {
        RelationshipCollection relationships = new RelationshipCollection();

        for (Module module:this) {
            if (module == cutoffModule) {
                break;
            }

            if (module.isEnabled()) module.addRelationships(relationships);

        }

        return relationships;

    }

    public RelationshipCollection getRelationships() {
        return getRelationships(null);

    }

}
