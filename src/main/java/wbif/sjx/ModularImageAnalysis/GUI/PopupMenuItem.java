package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class PopupMenuItem extends JMenuItem {
    private HCModule module = null;

    public PopupMenuItem(HCModule module) {
        this.module = module;
        if (module != null) setText(module.getTitle());

    }

    public HCModule getModule() {
        return module;
    }
}
