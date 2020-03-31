package wbif.sjx.MIA.Object.Parameters;

import javax.annotation.Nonnull;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

public class PartnerObjectsP extends ChoiceType {
    private String partnerObjectsName = "";

    public PartnerObjectsP(String name, Module module) {
        super(name, module);
    }

    public PartnerObjectsP(String name, Module module, String description) {
        super(name, module, description);
    }

    public PartnerObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String partnerObjectsName) {
        super(name, module);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    public PartnerObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String partnerObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.partnerObjectsName = partnerObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        PartnerObjectsP newParameter = new PartnerObjectsP(name,newModule,getChoice(),partnerObjectsName,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    public String getPartnerObjectsName() {
        return partnerObjectsName;
    }

    public void setPartnerObjectsName(String partnerObjectsName) {
        this.partnerObjectsName = partnerObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (partnerObjectsName == null) return null;

        ModuleCollection modules = module.getModules();
        PartnerRefCollection relationships = modules.getPartnerRefs(module);

        return relationships.getPartnerNamesArray(partnerObjectsName);

    }
}
