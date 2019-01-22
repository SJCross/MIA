package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;

import javax.annotation.Nonnull;

public class ObjectMeasurementP extends ChoiceType {
    private String objectName = "";

    public ObjectMeasurementP(String name, Module module) {
        super(name, module);
    }

    public ObjectMeasurementP(String name, Module module, @Nonnull String choice, @Nonnull String objectName) {
        super(name, module);
        this.objectName = objectName;
        this.choice = choice;

    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String[] getChoices() {
        return GUI.getModules().getObjectMeasurementRefs(objectName,module).getMeasurementNames();
    }
}
