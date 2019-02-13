package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.FileParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.FileFolderType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import java.io.File;

public class FileFolderPathP extends FileFolderType {
    public FileFolderPathP(String name, Module module) {
        super(name,module);
    }

    public FileFolderPathP(String name, Module module, @Nonnull String fileFolderPath) {
        super(name,module,fileFolderPath);
    }

    @Override
    public boolean isDirectory() {
        String fileFolderPath = getPath();
        if (fileFolderPath == null) return false;
        return new File(fileFolderPath).isDirectory();
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new FileParameter(this,FileParameter.FileTypes.EITHER_TYPE);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new FileFolderPathP(name,module,getPath());
    }
}
