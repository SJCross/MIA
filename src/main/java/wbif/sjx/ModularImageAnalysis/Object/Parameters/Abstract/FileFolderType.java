package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.annotation.Nonnull;
import java.io.File;

public abstract class FileFolderType extends Parameter {
    private String path;

    public FileFolderType(String name, Module module, @Nonnull String path) {
        super(name, module);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path= path;
    }

    public abstract boolean isDirectory();

    @Override
    public <T> T getValue() {
        return (T) path;
    }

    @Override
    public boolean verify() {
        // Checking the file exists
        return new File(path).exists();
    }
}
