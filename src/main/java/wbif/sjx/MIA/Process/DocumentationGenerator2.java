package wbif.sjx.MIA.Process;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.pull.XmlPullParserException;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.ModuleCollection;

public class DocumentationGenerator2 {
    private String version = "";

    public static void main(String[] args) {
        try {
            DocumentationGenerator2 generator = new DocumentationGenerator2();
            generator.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DocumentationGenerator2() {
        try {
            FileReader reader = new FileReader("pom.xml");
            Model model = new MavenXpp3Reader().read(reader);
            reader.close();
            version = new MavenProject(model).getVersion();
        } catch (XmlPullParserException | IOException | org.codehaus.plexus.util.xml.pull.XmlPullParserException e) {
            version = getClass().getPackage().getImplementationVersion();
        }
    }

    public void run() throws IOException {
        // Clearing existing HTML files
        File root = new File("docs/html");
        deleteFolders(root);
        root.mkdir();

        generateIndexPage();
        generateGettingStartedPage();
        generateGuidesPage();

        // Generating module pages
        Category rootCategory = Categories.getRootCategory();
        generateModuleCategoryPage(rootCategory);

        generateAboutPage();

        // Creating README.md
        generateReadmeMarkdown();
    }

    private void deleteFolders(File root) {
        if (root.isFile()) {
            root.delete();

        } else if (root.isDirectory()) {
            for (File file : root.listFiles())
                deleteFolders(file);

            root.delete();
        }
    }

    private String getPageTemplate(String pathToTemplate, String pathToRoot) {
        try {
            String page = new String(Files.readAllBytes(Paths.get(pathToTemplate)));
            page = insertPathToRoot(page, pathToRoot);
            page = insertMIAVersion(page);
            return page;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void generateIndexPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = ".";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);
        String mainContent = getPageTemplate("src/main/resources/templatehtml/indextemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter("docs/index.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateGettingStartedPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GETTING STARTED");

        FileWriter writer = new FileWriter("docs/html/gettingstarted.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateGuidesPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "GUIDES");

        FileWriter writer = new FileWriter("docs/html/guides.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public void generateModuleCategoryPage(Category category) throws IOException {
        // Setting the path and ensuring the folder exists
        String pathToRoot = getCatgoryPathToRoot(category) + "..";
        String categoryPath = getCategoryPath(category);
        String path = "docs/html/";
        new File(path + categoryPath).mkdirs();

        // Initialise HTML document
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        // Populate module packages content
        String mainContent = getPageTemplate("src/main/resources/templatehtml/modulestemplate.html", pathToRoot);
        mainContent = mainContent.replace("${CATEGORY_NAME}", category.getName());
        mainContent = mainContent.replace("${CATEGORY_DESCRIPTION}", category.getDescription());

        // Adding a card for each child category
        String categoryContent = "";
        for (Category childCategory : category.getChildren()) {
            String cardContent = getPageTemplate("src/main/resources/templatehtml/categorycardtemplate.html", pathToRoot);
            cardContent = cardContent.replace("${CARD_TITLE}", childCategory.getName());
            cardContent = cardContent.replace("${CARD_TEXT}", childCategory.getDescription());
            cardContent = cardContent.replace("${TARGET_PATH}",
                    getCategoryPath(childCategory) + "/" + childCategory.getName().toLowerCase());
            categoryContent = categoryContent + cardContent;
        }
        mainContent = mainContent.replace("${CATEGORY_CARDS}", categoryContent);

        // Finding modules in this category and adding them to this page
        TreeMap<String, Module> modules = getModules();
        String moduleContent = "";
        for (Module module : modules.values()) {
            if (module.getCategory() == category) {
                String cardContent = getPageTemplate("src/main/resources/templatehtml/modulecardtemplate.html", pathToRoot);
                cardContent = cardContent.replace("${CARD_TITLE}", module.getName());
                cardContent = cardContent.replace("${CARD_TEXT}", module.getDescription());
                cardContent = cardContent.replace("${TARGET_PATH}",
                        getCategoryPath(category) + "/" + module.getName().toLowerCase());
                moduleContent = moduleContent + cardContent;
            }
        }
        mainContent = mainContent.replace("${MODULE_CARDS}", moduleContent);

        // Add packages content to page
        page = page.replace("${MAIN_CONTENT}", mainContent);

        FileWriter writer = new FileWriter(path + categoryPath + "/" + category.getName().toLowerCase() + ".html");
        writer.write(page);
        writer.flush();
        writer.close();

        // For each child category, repeating the same process
        for (Category childCategory : category.getChildren())
            generateModuleCategoryPage(childCategory);

    }

    public void generateAboutPage() throws IOException {
        // Generate module list HTML document
        String pathToRoot = "..";
        String page = getPageTemplate("src/main/resources/templatehtml/pagetemplate.html", pathToRoot);

        page = page.replace("${MAIN_CONTENT}", "ABOUT");

        FileWriter writer = new FileWriter("docs/html/about.html");
        writer.write(page);
        writer.flush();
        writer.close();

    }

    public static void generateReadmeMarkdown() {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/githubBadges.md"))));
            sb.append("\n\n");

            sb.append(
                    "[![Wolfson Bioimaging](./src/main/resources/Images/Logo_text_UoB_128.png)](http://www.bristol.ac.uk/wolfson-bioimaging/)");
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/introduction.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/installation.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/creatingWorkflow.md"))));
            sb.append("\n\n");
            sb.append(new String(
                    Files.readAllBytes(Paths.get("src/main/resources/templatemd/usingExistingWorkflow.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/acknowledgements.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/citing.md"))));
            sb.append("\n\n");
            sb.append(new String(Files.readAllBytes(Paths.get("src/main/resources/templatemd/note.md"))));
            sb.append("\n\n");

            FileWriter writer = new FileWriter("README.md");
            writer.write(sb.toString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String insertPathToRoot(String str, String pathToRoot) {
        return str.replace("${PTR}", pathToRoot);
    }

    String insertMIAVersion(String str) {
        return str.replace("${MIA_VERSION}", version);
    }

    String getCategoryPath(Category category) {
        if (category == null)
            return "";

        return (getCategoryPath(category.getParent()) + "/" + category.getName()).toLowerCase();

    }

    String getCatgoryPathToRoot(Category category) {
        if (category == null)
            return "";

        return getCatgoryPathToRoot(category.getParent()) + "../";
    }

    private static TreeMap<String, Module> getModules() {
        // Get a list of Modules
        List<String> classNames = ClassHunter.getModules(false);

        // Converting the list of classes to a list of Modules
        TreeMap<String, Module> modules = new TreeMap<>();
        ModuleCollection tempCollection = new ModuleCollection();
        for (String className : classNames) {
            try {
                Class<Module> clazz = (Class<Module>) Class.forName(className);

                // Skip any abstract Modules
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                Constructor constructor = clazz.getDeclaredConstructor(ModuleCollection.class);
                Module module = (Module) constructor.newInstance(tempCollection);
                modules.put(module.getName(), module);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                MIA.log.writeError(e);
            }
        }

        return modules;

    }

}
