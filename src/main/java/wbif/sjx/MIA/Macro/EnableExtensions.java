package wbif.sjx.MIA.Macro;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.macro.Functions;
import wbif.sjx.MIA.Process.DependencyValidator;


@Plugin(type = Command.class, menuPath = "Plugins>Bristol WBIF>Enable MIA Extensions")
public class EnableExtensions implements Command {
    @Override
    public void run() {
        // Run the dependency validator.  If updates were required, return.
        if (DependencyValidator.run()) return;

        MacroHandler macroHandler = MacroHandler.getMacroHandler();

        if (!IJ.macroRunning()) {
            IJ.error("Cannot install extensions from outside a macro.");
            return;
        }

        Functions.registerExtensions(macroHandler);

    }
}