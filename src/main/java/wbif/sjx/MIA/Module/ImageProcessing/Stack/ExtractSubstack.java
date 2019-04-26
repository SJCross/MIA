package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import javax.annotation.Nullable;import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by sc13967 on 18/01/2018.
 */
public class ExtractSubstack extends Module implements ActionListener {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SELECTION_MODE = "Selection mode";
    public static final String CHANNELS = "Channels";
    public static final String SLICES = "Slices";
    public static final String FRAMES = "Frames";
    public static final String ENABLE_CHANNELS_SELECTION = "Enable channels selection";
    public static final String ENABLE_SLICES_SELECTION = "Enable slices selection";
    public static final String ENABLE_FRAMES_SELECTION = "Enable frames selection";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField channelsNumberField;
    private JTextField slicesNumberField;
    private JTextField framesNumberField;

    private int elementHeight = 40;
    private boolean active = false;


    public interface SelectionModes {
        String FIXED = "Fixed";
        String MANUAL = "Manual";

        String[] ALL = new String[]{FIXED,MANUAL};

    }

    private void showOptionsPanel(@Nullable String inputChannelsRange, @Nullable String inputSlicesRange, @Nullable String inputFramesRange) {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.insets = new Insets(5,5,5,5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Set the range for each type.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        headerLabel.setPreferredSize(new Dimension(300,elementHeight));
        frame.add(headerLabel,c);

        if (inputChannelsRange != null) {
            JLabel channelsLabel = new JLabel("Channel range");
            channelsLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridy++;
            c.gridwidth = 1;
            frame.add(channelsLabel, c);

            channelsNumberField = new JTextField(inputChannelsRange);
            channelsNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(channelsNumberField, c);
        }

        if (inputSlicesRange != null) {
            JLabel slicesLabel = new JLabel("Slice range");
            slicesLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 5, 5, 5);
            frame.add(slicesLabel, c);

            slicesNumberField = new JTextField(inputSlicesRange);
            slicesNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(slicesNumberField, c);
        }

        if (inputFramesRange != null) {
            JLabel framesLabel = new JLabel("Frame range");
            framesLabel.setPreferredSize(new Dimension(100, elementHeight));
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.insets = new Insets(0, 5, 5, 5);
            frame.add(framesLabel, c);

            framesNumberField = new JTextField(inputFramesRange);
            framesNumberField.setPreferredSize(new Dimension(200, elementHeight));
            c.gridx++;
            c.insets = new Insets(0, 0, 5, 5);
            frame.add(framesNumberField, c);
        }

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this);
        okButton.setActionCommand(OK);
        okButton.setPreferredSize(new Dimension(300,elementHeight));
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.insets = new Insets(0,5,5,5);
        frame.add(okButton,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public static int[] extendRangeToEnd(int[] inputRange, int end) {
        // Adding the numbers to a TreeSet, then returning as an array
        TreeSet<Integer> values = new TreeSet<>();

        // Adding the explicitly-named values
        for (int i=0;i<inputRange.length-3;i++) values.add(inputRange[i]);

        // Adding the range values
        int start = inputRange[inputRange.length-3];
        int interval = inputRange[inputRange.length-2] - start;
        for (int i=start;i<=end;i=i+interval) values.add(i);

        return values.stream().mapToInt(Integer::intValue).toArray();

    }

    public static Image extractSubstack(Image inputImage, String outputImageName, String channels, String slices, String frames) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        int[] channelsList = CommaSeparatedStringInterpreter.interpretIntegers(channels,true);
        if (channelsList[channelsList.length-1] == Integer.MAX_VALUE) channelsList = extendRangeToEnd(channelsList,inputImagePlus.getNChannels());

        int[] slicesList = CommaSeparatedStringInterpreter.interpretIntegers(slices,true);
        if (slicesList[slicesList.length-1] == Integer.MAX_VALUE) slicesList = extendRangeToEnd(slicesList,inputImagePlus.getNSlices());

        int[] framesList = CommaSeparatedStringInterpreter.interpretIntegers(frames,true);
        if (framesList[framesList.length-1] == Integer.MAX_VALUE) framesList = extendRangeToEnd(framesList,inputImagePlus.getNFrames());

        List<Integer> cList = java.util.Arrays.stream(channelsList).boxed().collect(Collectors.toList());
        List<Integer> zList = java.util.Arrays.stream(slicesList).boxed().collect(Collectors.toList());
        List<Integer> tList = java.util.Arrays.stream(framesList).boxed().collect(Collectors.toList());

        // Generating the substack and adding to the workspace
        ImagePlus outputImagePlus =  SubHyperstackMaker.makeSubhyperstack(inputImagePlus,cList,zList,tList).duplicate();
        outputImagePlus.setCalibration(inputImagePlus.getCalibration());
        return new Image(outputImageName,outputImagePlus);

    }

    @Override
    public String getTitle() {
        return "Extract substack";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String selectionMode = parameters.getValue(SELECTION_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String channels = parameters.getValue(CHANNELS);
        String slices = parameters.getValue(SLICES);
        String frames = parameters.getValue(FRAMES);
        boolean enableChannels = parameters.getValue(ENABLE_CHANNELS_SELECTION);
        boolean enableSlices = parameters.getValue(ENABLE_SLICES_SELECTION);
        boolean enableFrames = parameters.getValue(ENABLE_FRAMES_SELECTION);

        switch (selectionMode) {
            case SelectionModes.MANUAL:
                // Displaying the image and control panel
                ImagePlus displayImagePlus = inputImage.getImagePlus().duplicate();
                displayImagePlus.show();

                String inputChannelsRange = enableChannels ? channels : null;
                String inputSlicesRange = enableSlices ? slices : null;
                String inputFramesRange = enableFrames ? frames : null;

                showOptionsPanel(inputChannelsRange,inputSlicesRange,inputFramesRange);

                // All the while the control is open, do nothing
                while (active) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (enableChannels) channels = channelsNumberField.getText();
                if (enableSlices) slices = slicesNumberField.getText();
                if (enableFrames) frames = framesNumberField.getText();

                frame.dispose();
                frame = null;
                displayImagePlus.close();

                break;
        }

        Image outputImage = extractSubstack(inputImage,outputImageName,channels,slices,frames);

        workspace.addImage(outputImage);

        // If selected, displaying the image
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(SELECTION_MODE,this, SelectionModes.FIXED, SelectionModes.ALL));
        parameters.add(new StringP(CHANNELS,this,"1-end"));
        parameters.add(new StringP(SLICES,this,"1-end"));
        parameters.add(new StringP(FRAMES,this,"1-end"));
        parameters.add(new BooleanP(ENABLE_CHANNELS_SELECTION,this,true));
        parameters.add(new BooleanP(ENABLE_SLICES_SELECTION,this,true));
        parameters.add(new BooleanP(ENABLE_FRAMES_SELECTION,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(CHANNELS));
        returnedParameters.add(parameters.getParameter(SLICES));
        returnedParameters.add(parameters.getParameter(FRAMES));

        returnedParameters.add(parameters.getParameter(SELECTION_MODE));
        switch ((String) parameters.getValue(SELECTION_MODE)) {
            case SelectionModes.MANUAL:
                returnedParameters.add(parameters.getParameter(ENABLE_CHANNELS_SELECTION));
                returnedParameters.add(parameters.getParameter(ENABLE_SLICES_SELECTION));
                returnedParameters.add(parameters.getParameter(ENABLE_FRAMES_SELECTION));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (OK):
                active = false;
                break;
        }
    }
}
