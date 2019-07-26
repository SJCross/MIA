package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.WorkflowParameters;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;
import wbif.sjx.MIA.Process.Logging.LogRenderer;

public class DoubleP extends TextType {
    protected String value;

    public DoubleP(String name, Module module, double value) {
        super(name,module);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value) {
        super(name,module);
        this.value = value;
    }

    public DoubleP(String name, Module module, double value, String description) {
        super(name,module,description);
        this.value = String.valueOf(value);
    }

    public DoubleP(String name, Module module, String value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(double value) {
        this.value = String.valueOf(value);
    }

    public void setValue(String value) throws NumberFormatException {
        // Checking this is valid
        if (WorkflowParameters.containsMetadata(value)) {
            this.value = value;
        } else {
            try {
                Double.parseDouble(value);
                this.value = value;
            } catch (NumberFormatException e) {
                MIA.log.write("Module: \""+module.getName()+"\", parameter: \""+getName()+"\". Must be a double-precision number or metadata handle (e.g. ${name})", LogRenderer.Level.WARNING);
            }
        }
    }

    @Override
    public String getRawStringValue() {
        return value;

    }

    @Override
    public <T extends Parameter> T duplicate() {
        DoubleP newParameter = new DoubleP(name,module,value,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public void setValueFromString(String value) {
        setValue(value);
    }

    @Override
    public <T> T getValue() throws NumberFormatException {
        return (T) (Double) Double.parseDouble(module.getModules().getWorkflowParameters().convertString(value));
    }

    @Override
    public <T> void setValue(T value) {
        this.value = String.valueOf(value);

    }
}
