package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class FileParameter extends JButton {
    private HCModule module;
    private Parameter parameter;

    public FileParameter(HCModule module, Parameter parameter) {
        this.module = module;
        this.parameter = parameter;
        setFocusPainted(false);

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
