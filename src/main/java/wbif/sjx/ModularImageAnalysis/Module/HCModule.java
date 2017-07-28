// TODO: Module to save images and to save objects (could roll this into ShowImage and ShowObjects)
// TODO: Module to plot histograms of measurements (e.g. mean intensity for objects)
// TODO: Module to calculate size metrics of objects (can used Blob class)
// TODO: Module to calculate radial intensity distribution of objects

package wbif.sjx.ModularImageAnalysis.Module;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.io.Serializable;

/**
 * Created by sc13967 on 02/05/2017.
 */
public abstract class HCModule implements Serializable {
    public ParameterCollection parameters = new ParameterCollection();
    private String notes = "";
    private boolean enabled = true;


    // CONSTRUCTOR

    public HCModule() {
        initialiseParameters();

    }


    // PUBLIC METHODS

    public abstract String getTitle();

    public abstract String getHelp();

    public abstract void execute(Workspace workspace, boolean verbose) throws GenericMIAException;

    /**
     * Get a ParameterCollection of all the possible parameters this class requires (not all may be used).  This returns
     * the ParameterCollection, rather than just setting the local variable directly, which helps ensure the correct
     * operation is included in the method.
     * @return
     */
    public abstract void initialiseParameters();

    /**
     * Return a ParameterCollection of the currently active parameters.  This is run each time a parameter is changed.
     * For example, if "Export XML" is set to "false" a sub-parameter specifying the measurements to export won't be
     * included in the ParameterCollection.  A separate rendering class will take this ParameterCollection and generate
     * an appropriate GUI panel.
     * @return
     */
    public abstract ParameterCollection getActiveParameters();

    /**
     * Takes an existing collection of measurements and adds any created
     * @param measurements
     * @return
     */
    public abstract void addMeasurements(MeasurementCollection measurements);

    /**
     * Returns a LinkedHashMap containing the parents (key) and their children (value)
     * @return
     */
    public abstract void addRelationships(RelationshipCollection relationships);

    public void updateParameterValue(String name, Object value) {
        parameters.updateValue(name,value);

    }

    public <T> T getParameterValue(String name) {
        return parameters.getParameter(name).getValue();

    }

    public int getParameterType(String name) {
        return parameters.get(name).getType();

    }

    public void setParameterVisibility(String name, boolean visible) {
        parameters.updateVisible(name,visible);

    }


    // PRIVATE METHODS

    void execute(Workspace workspace) throws GenericMIAException {
        execute(workspace,false);

    }

    public String getNotes() {
        return notes;

    }

    public void setNotes(String notes) {
        this.notes = notes;

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
