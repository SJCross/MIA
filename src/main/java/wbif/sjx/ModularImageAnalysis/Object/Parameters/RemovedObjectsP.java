package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ObjectNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class RemovedObjectsP extends ObjectNamesType {
    public RemovedObjectsP(String name, Module module) {
        super(name,module);
    }

    public RemovedObjectsP(String name, Module module, @Nonnull String choice) {
        super(name,module);
        this.choice = choice;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new RemovedObjectsP(name,module,choice);
    }
}
