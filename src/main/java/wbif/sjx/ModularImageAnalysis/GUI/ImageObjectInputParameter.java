package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ImageObjectInputParameter extends WiderDropDownCombo {
    private HCModule module;
    private Parameter parameter;

    public ImageObjectInputParameter(HCModule module, Parameter parameter) {
        this.module = module;
        this.parameter = parameter;

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
