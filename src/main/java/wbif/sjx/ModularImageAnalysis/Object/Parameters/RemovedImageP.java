package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ImageNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class RemovedImageP extends ImageNamesType {
    public RemovedImageP(String name, Module module) {
        super(name,module);
    }

    public RemovedImageP(String name, Module module, @Nonnull String choice) {
        super(name,module);
        this.choice = choice;
    }

    public RemovedImageP(String name, Module module, @Nonnull String choice, String description) {
        super(name,module,description);
        this.choice = choice;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new RemovedImageP(name,module,choice,getDescription());
    }
}
