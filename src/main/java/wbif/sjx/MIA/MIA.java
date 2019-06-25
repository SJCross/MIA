// TODO: Have global parameters for things like overlay line width
// TODO: Add input/output parameters to .mia file (and condition to avoid problems if this isn't present in files)
// TODO: Set "Run" button to simply start processing.  File path should be set at input options

package wbif.sjx.MIA;

import net.imagej.ImageJ;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.xml.sax.SAXException;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Hidden.GlobalVariables;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Process.ClassHunter;
import wbif.sjx.MIA.Process.Logging.*;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisReader;
import wbif.sjx.MIA.Process.AnalysisHandling.AnalysisRunner;
import wbif.sjx.MIA.Process.DependencyValidator;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;


/**
 * Created by Stephen Cross on 14/07/2017.
 */
@Plugin(type = Command.class, menuPath = "Plugins>Bristol WBIF>MIA (Modular Image Analysis)")
public class MIA implements Command {
    private static final ErrorLog errorLog = new ErrorLog();
    private static ArrayList<String> pluginPackageNames = new ArrayList<>();
    private static String version = "";
    private static boolean debug = false;
    private static GlobalVariables globalVariables = new GlobalVariables(null);
    public static Log log = new BasicLog(); // This is effectively just for test methods

    /*
    Gearing up for the transition from ImagePlus to ImgLib2 formats.  Modules can use this to addRef compatibility.
     */
    private static final boolean imagePlusMode = true;

    @Parameter
    private UIService uiService;


    public static void main(String[] args) throws Exception {
        debug = true;

        try {
            if (args.length == 0) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                ImageJ ij = new ImageJ();
                ij.ui().showUI();
                ij.command().run("wbif.sjx.MIA.MIA",false);

            } else {
                Analysis analysis = AnalysisReader.loadAnalysis(args[0]);
                AnalysisRunner.startAnalysis(analysis);
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException | SAXException |
                UnsupportedLookAndFeelException | ParserConfigurationException | InterruptedException e) {
            e.printStackTrace(System.err);

        }
    }

    @Override
    public void run() {
        log = new ConsoleLog(uiService);
        log.setWriteEnabled(Log.Level.DEBUG,debug);

        // Determining the version number from the pom file
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            reader.close();
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException e) {
            version = getClass().getPackage().getImplementationVersion();
        }

        // Run the dependency validator.  If updates were required, return.
        if (DependencyValidator.run()) return;

        // Redirecting the standard output and error streams, so they are formatted by for the console
//        System.setOut(new PrintStream(new MessageLog()));
//        System.setErr(new PrintStream(new ErrorLog()));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            new GUI();

        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    public static ErrorLog getErrorLog() {
        return errorLog;
    }

    public static boolean isImagePlusMode() {
        return imagePlusMode;
    }

    public static void addPluginPackageName(String packageName) {
        pluginPackageNames.add(packageName);
    }

    public static ArrayList<String> getPluginPackages() {
        return pluginPackageNames;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        MIA.debug = debug;
    }

    public static String getSlashes() {
        // Setting the file path slashes depending on the operating system
        if (SystemUtils.IS_OS_WINDOWS) return  "\\";
        else if (SystemUtils.IS_OS_MAC_OSX) return  "/";
        else if (SystemUtils.IS_OS_LINUX) return  "/";

        return "\\";

    }

    public static void setGlobalVariables(GlobalVariables globalVariables) {
        MIA.globalVariables = globalVariables;
    }

    public static GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    public static Log getLog() {
        return log;
    }

}
