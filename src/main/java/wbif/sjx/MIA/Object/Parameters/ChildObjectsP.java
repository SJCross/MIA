package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import javax.annotation.Nonnull;

public class ChildObjectsP extends ChoiceType {
    private String parentObjectsName = "";
    private WiderDropDownCombo control;

    public ChildObjectsP(String name, Module module) {
        super(name, module);
    }

    public ChildObjectsP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ChildObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String parentObjectsName) {
        super(name, module);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    public ChildObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String parentObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        ChildObjectsP newParameter = new ChildObjectsP(name,module,getChoice(),parentObjectsName,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }

    public String getParentObjectsName() {
        return parentObjectsName;
    }

    public void setParentObjectsName(String parentObjectsName) {
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (parentObjectsName == null) return null;

        ModuleCollection modules = module.getModules();
        RelationshipRefCollection relationships = modules.getRelationshipRefs(module);

        return relationships.getChildNames(parentObjectsName, true);

    }
}
