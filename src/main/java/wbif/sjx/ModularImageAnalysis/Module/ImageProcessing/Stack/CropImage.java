// TODO: Figure out why ImageJFunctions.wrap() ImagePlus behaves badly with RunTrackMate tracking

package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.HyperStackMaker;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

public class CropImage < T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String LEFT = "Left coordinate";
    public static final String RIGHT = "Right coordinate";
    public static final String HEIGHT = "Top coordinate";
    public static final String BOTTOM = "Bottom coordinate";
    public static final String SHOW_IMAGE = "Show image";

    @Override
    public String getTitle() {
        return "Crop image";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int left = parameters.getValue(LEFT);
        int right = parameters.getValue(RIGHT);
        int top = parameters.getValue(HEIGHT);
        int bottom = parameters.getValue(BOTTOM);

        Img<T> img = inputImage.getImg();
        long[] min = new long[img.numDimensions()];
        long[] dimsIn = new long[img.numDimensions()];
        min[0] = left;
        min[1] = top;
        dimsIn[0] = right-left;
        dimsIn[1] = bottom-top;
        for (int i=2;i<img.numDimensions();i++) {
            min[i] = 0;
            dimsIn[i] = img.dimension(i);
        }

        long[] dimsOut = new long[5];
        dimsOut[0] = right-left;
        dimsOut[1] = bottom-top;
        dimsOut[2] = inputImagePlus.getNChannels();
        dimsOut[3] = inputImagePlus.getNSlices();
        dimsOut[4] = inputImagePlus.getNFrames();

        // Creating the output image and copying over the pixel coordinates
        ArrayImgFactory<T> factory = new ArrayImgFactory<T>();
        Img<T> outputImg = factory.create(dimsOut,img.firstElement());
        Cursor<T> cropCursor = Views.offsetInterval(img,min,dimsIn).cursor();
        Cursor<T> outputCursor = outputImg.cursor();

        while (cropCursor.hasNext()) outputCursor.next().set(cropCursor.next());

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = new Duplicator().run(ImageJFunctions.wrap(outputImg,outputImageName));
        outputImagePlus.setCalibration(inputImagePlus.getCalibration());

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            ImagePlus dispIpl = new Duplicator().run(outputImagePlus);
            IntensityMinMax.run(dispIpl,true);
            dispIpl.show();
        }

        // If the image is being saved as a new image, adding it to the workspace
        writeMessage("Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(LEFT, Parameter.INTEGER,0));
        parameters.add(new Parameter(RIGHT, Parameter.INTEGER,512));
        parameters.add(new Parameter(HEIGHT, Parameter.INTEGER,0));
        parameters.add(new Parameter(BOTTOM, Parameter.INTEGER,512));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
