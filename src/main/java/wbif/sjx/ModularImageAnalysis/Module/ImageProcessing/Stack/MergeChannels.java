package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import fiji.stacks.Hyperstack_rearranger;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.process.LUT;
import ij3d.ColorTable;
import net.imagej.ImgPlus;
import net.imagej.autoscale.DefaultAutoscaleMethod;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.IdentityAxis;
import net.imagej.display.ColorTables;
import net.imagej.interval.DefaultCalibratedRealInterval;
import net.imglib2.Cursor;
import net.imglib2.RealInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sc13967 on 22/02/2018.
 */
public class MergeChannels< T extends RealType< T > & NativeType< T >> extends Module {
    public static final String INPUT_IMAGE1 = "Input image 1";
    public static final String INPUT_IMAGE2 = "Input image 2";
    public static final String OUTPUT_IMAGE = "Output image";


//    public void forceSameType(Image inputImage1, Image inputImage2) {
//        ImgPlus<T> img1 = inputImage1.getImgPlus();
//        ImgPlus<T> img2 = inputImage2.getImgPlus();
//
//    }

    public Image combineImages(Image inputImage1, Image inputImage2, String outputImageName) {
        ImgPlus<T> img1 = inputImage1.getImgPlus();
        ImgPlus<T> img2 = inputImage2.getImgPlus();

        int xDim1 = img1.dimensionIndex(Axes.X);
        int yDim1 = img1.dimensionIndex(Axes.Y);
        int cDim1 = img1.dimensionIndex(Axes.CHANNEL);
        int zDim1 = img1.dimensionIndex(Axes.Z);
        int tDim1 = img1.dimensionIndex(Axes.TIME);
        int xDim2 = img2.dimensionIndex(Axes.X);
        int yDim2 = img2.dimensionIndex(Axes.Y);
        int cDim2 = img2.dimensionIndex(Axes.CHANNEL);
        int zDim2 = img2.dimensionIndex(Axes.Z);
        int tDim2 = img2.dimensionIndex(Axes.TIME);

        long[] dimsIn1 = new long[5];
        long[] dimsIn2 = new long[5];
        long[] dimsOut = new long[5];
        long[] offsetOut1 = new long[5];
        long[] offsetOut2 = new long[5];

        dimsIn1[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsIn1[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsIn1[2] = cDim1 == -1 ? 1 : img1.dimension(cDim1);
        dimsIn1[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsIn1[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        dimsIn2[0] = xDim2 == -1 ? 1 : img2.dimension(xDim2);
        dimsIn2[1] = yDim2 == -1 ? 1 : img2.dimension(yDim2);
        dimsIn2[2] = cDim2 == -1 ? 1 : img2.dimension(cDim2);
        dimsIn2[3] = zDim2 == -1 ? 1 : img2.dimension(zDim2);
        dimsIn2[4] = tDim2 == -1 ? 1 : img2.dimension(tDim2);

        dimsOut[0] = xDim1 == -1 ? 1 : img1.dimension(xDim1);
        dimsOut[1] = yDim1 == -1 ? 1 : img1.dimension(yDim1);
        dimsOut[2] = (cDim1 == -1 ? 1 : img1.dimension(cDim1)) + (cDim2 == -1 ? 1 : img2.dimension(cDim2));
        dimsOut[3] = zDim1 == -1 ? 1 : img1.dimension(zDim1);
        dimsOut[4] = tDim1 == -1 ? 1 : img1.dimension(tDim1);

        Arrays.fill(offsetOut1,0);
        Arrays.fill(offsetOut2,0);
        offsetOut2[2] = dimsIn1[2];

        // Creating the composite image
        T type = img1.firstElement();
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        ImgPlus<T> mergedImg = new ImgPlus<>(factory.create(dimsOut, type));

        // Assigning the relevant dimensions
        CalibratedAxis xAxis = xDim1 == -1 ? new IdentityAxis(Axes.X) : img1.axis(xDim1);
        mergedImg.setAxis(xAxis,0);
        CalibratedAxis yAxis = yDim1 == -1 ? new IdentityAxis(Axes.Y) : img1.axis(yDim1);
        mergedImg.setAxis(yAxis,1);
        CalibratedAxis cAxis = cDim1 == -1 ? new IdentityAxis(Axes.CHANNEL) : img1.axis(cDim1);
        mergedImg.setAxis(cAxis,2);
        CalibratedAxis zAxis = zDim1 == -1 ? new IdentityAxis(Axes.Z) : img1.axis(zDim1);
        mergedImg.setAxis(zAxis,3);
        CalibratedAxis tAxis = tDim1 == -1 ? new IdentityAxis(Axes.TIME) : img1.axis(tDim1);
        mergedImg.setAxis(tAxis,4);

        Cursor<T> cursorIn = img1.cursor();
        Cursor<T> cursorOut = Views.offsetInterval(mergedImg, offsetOut1, dimsIn1).cursor();
        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

        cursorIn = img2.cursor();
        cursorOut = Views.offsetInterval(mergedImg, offsetOut2, dimsIn2).cursor();
        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

//        ImagePlus ipl;
//        if (mergedImg.firstElement().getClass().isInstance(new UnsignedByteType())) {
//            ipl = ImageJFunctions.wrapUnsignedByte(mergedImg,outputImageName);
//        } else if (mergedImg.firstElement().getClass().isInstance(new UnsignedShortType())) {
//            ipl = ImageJFunctions.wrapUnsignedShort(mergedImg,outputImageName);
//        } else {
//            ipl = ImageJFunctions.wrapFloat(mergedImg,outputImageName);
//        }

        ImagePlus ipl = ImageJFunctions.wrap(mergedImg,outputImageName);
        ipl = new Duplicator().run(HyperStackConverter.toHyperStack(ipl,ipl.getNChannels(),ipl.getNSlices(),ipl.getNFrames(),"xyczt","Composite"));

        // Updating the display range to help show all the colours
        IntensityMinMax.run(ipl,true,0.001);

        // Spatial calibration has to be reapplied, as it's lost in the translation between ImagePlus and ImgPlus
        ipl.setCalibration(inputImage1.getImagePlus().getCalibration());

        return new Image(outputImageName,ipl);

    }

    private Img<T> createComposite(Image inputImageRed, Image inputImageGreen, Image inputImageBlue) {
        long dimX = 0;
        long dimY = 0;
        long dimZ = 0;
        T type = null;

        Img<T> redImg = null;
        if (inputImageRed != null) {
            redImg = inputImageRed.getImgPlus();
            dimX = redImg.dimension(0);
            dimY = redImg.dimension(1);
            dimZ = redImg.dimension(2);
            type = redImg.firstElement();
        }

        Img<T> greenImg = null;
        if (inputImageGreen != null) {
            greenImg = inputImageGreen.getImgPlus();
            dimX = greenImg.dimension(0);
            dimY = greenImg.dimension(1);
            dimZ = greenImg.dimension(2);
            type = greenImg.firstElement();
        }

        Img<T> blueImg = null;
        if (inputImageBlue != null) {
            blueImg = inputImageBlue.getImgPlus();
            dimX = blueImg.dimension(0);
            dimY = blueImg.dimension(1);
            dimZ = blueImg.dimension(2);
            type = blueImg.firstElement();
        }

        // Creating the composite image
        long[] dimensions = new long[]{dimX,dimY,3, dimZ,1};
        final ImgFactory< T > factory = new ArrayImgFactory<>();
        Img<T> rgbImg = factory.create(dimensions, type);

        // Adding values view
        if (inputImageRed != null) {
            Cursor<T> cursorSingle = redImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 0, 0,0}, new long[]{dimX,dimY,1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageGreen != null) {
            Cursor<T> cursorSingle = greenImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 1, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        if (inputImageBlue != null) {
            Cursor<T> cursorSingle = blueImg.cursor();
            Cursor<T> cursorRGB = Views.offsetInterval(rgbImg, new long[]{0, 0, 2, 0,0}, new long[]{dimX, dimY, 1, dimZ,1}).cursor();
            while (cursorSingle.hasNext()) cursorRGB.next().set(cursorSingle.next());
        }

        return rgbImg;

    }

    @Override
    public String getTitle() {
        return "Merge channels";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting parameters
        String inputImage1Name = parameters.getValue(INPUT_IMAGE1);
        String inputImage2Name = parameters.getValue(INPUT_IMAGE2);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        Image inputImage1 = workspace.getImage(inputImage1Name);
        Image inputImage2 = workspace.getImage(inputImage2Name);

        // Ensuring the two image types are the same.  If they're not, they're set to the highest common type
//        forceSameType(inputImage1,inputImage2);

        Image mergedImage = combineImages(inputImage1,inputImage2,outputImageName);
        workspace.addImage(mergedImage);

        if (showOutput) {
            ImagePlus showIpl = new Duplicator().run(mergedImage.getImagePlus());
            showIpl.setTitle(outputImageName);
            showIpl.show();
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE1,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_IMAGE2,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
