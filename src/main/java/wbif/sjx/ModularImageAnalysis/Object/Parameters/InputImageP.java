package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ImageNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class InputImageP extends ImageNamesType {
    public InputImageP(String name, Module module) {
        super(name, module);
    }

    public InputImageP(String name, Module module, @Nonnull String imageName) {
        super(name, module);
        this.choice = imageName;
    }

    public InputImageP(String name, Module module, @Nonnull String imageName, String description) {
        super(name, module, description);
        this.choice = imageName;
    }

    public String getImageName() {
        return choice;
    }

    public void setImageName(String imageName) {
        this.choice = imageName;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new InputImageP(name,module,getImageName(),getDescription());
    }
}
