package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.AddParametersButton;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import java.util.LinkedHashSet;

/**
 * The value for this parameter is the number of collections that have been created.  The value source is a comma,
 * separated list of parameter names which it serves.
 */

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class ParameterGroup extends Parameter {
    private LinkedHashSet<ParameterCollection> collections = new LinkedHashSet<>();
    private ParameterCollection templateParameters = new ParameterCollection();


    // CONSTRUCTORS

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters) {
        super(name, module);
        this.templateParameters = templateParameters;

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, String description) {
        super(name, module,description);
        this.templateParameters = templateParameters;

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count) {
        super(name, module);
        this.templateParameters = templateParameters;

        // Initialising the specified number of collections
        for (int i=0;i<count;i++) addParameters();

    }

    public ParameterGroup(String name, Module module, ParameterCollection templateParameters, int count, String description) {
        super(name, module, description);
        this.templateParameters = templateParameters;

        // Initialising the specified number of collections
        for (int i=0;i<count;i++) addParameters();

    }


    // PUBLIC METHODS

    public ParameterCollection addParameters() {
        // Create new copy of template collections
        ParameterCollection newParameters = new ParameterCollection();
        for (Parameter templateParameter:templateParameters.values()) {
            Parameter newParameter = templateParameter.duplicate();

            // New parameters should inherit the visibility of the add button
            newParameter.setVisible(isVisible());

            // Adding parameter to the collection
            newParameters.add(newParameter);

        }

        // Adding the necessary RemoveParameter Parameter
        RemoveParameters removeParameters = new RemoveParameters("Remove collections",module,this,newParameters);
        removeParameters.setVisible(isVisible());
        newParameters.add(removeParameters);

        collections.add(newParameters);

        return newParameters;

    }

    public void addParameters(ParameterCollection collection) {
        // Adding the necessary RemoveParameter Parameter
        collection.add(new RemoveParameters("Remove collections",module,this,collection));

        collections.add(collection);

    }

    public void removeAllParameters() {
        collections = new LinkedHashSet<>();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new AddParametersButton(this);
    }

    @Override
    public <T> T getValue() {
        return (T) collections;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getRawStringValue() {
        return "";
    }

    @Override
    public void setValueFromString(String string) {

    }

    @Override
    public boolean verify() {
        boolean runnable = true;
        for (ParameterCollection collection: collections) {
            for (Parameter parameter:collection.values()) {
                boolean currentRunnable = parameter.verify();
                parameter.setValid(currentRunnable);
                if (!currentRunnable && runnable) runnable = false;
            }
        }
        return runnable;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return null;
    }

    public LinkedHashSet<ParameterCollection> getCollections() {
        return collections;
    }

    public void removeCollection(ParameterCollection collection) {
        collections.remove(collection);
    }

    public ParameterCollection getTemplateParameters() {
        return templateParameters;
    }
}