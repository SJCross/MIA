//package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;
//
//import ij.ImagePlus;
//import ij.plugin.Duplicator;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//
///**
// * Created by sc13967 on 19/09/2017.
// */
//public class ImageMath extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String APPLY_TO_INPUT = "Apply to input image";
//    public static final String OUTPUT_IMAGE = "Output image";
//    public static final String CALCULATION_TYPE = "Calculation";
//    public static final String VALUE_SOURCE = "Value source";
//    public static final String MEASUREMENT = "Measurement";
//    public static final String MATH_VALUE = "Value";
//    public static final String SHOW_IMAGE = "Show image";
//
//    public interface CalculationTypes {
//        String ADD = "Add";
//        String DIVIDE = "Divide";
//        String INVERT = "Invert";
//        String MULTIPLY = "Multiply";
//        String SUBTRACT = "Subtract";
//
//        String[] ALL = new String[]{ADD,DIVIDE,INVERT,MULTIPLY,SUBTRACT};
//
//    }
//
//    public interface ValueSources {
//        String FIXED = "Fixed value";
//        String MEASUREMENT = "Measurement value";
//
//        String[] ALL = new String[]{FIXED,MEASUREMENT};
//
//    }
//
//    @Override
//    public String getTitle() {
//        return "Image math";
//    }
//
//    @Override
//    public String getHelp() {
//        return null;
//    }
//
//    @Override
//    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
//        // Getting input image
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        Image inputImage = workspace.getImages().get(inputImageName);
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        // Getting parameters
//        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
//        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
//        String calculationType = parameters.getValue(CALCULATION_TYPE);
//        String valueSource = parameters.getValue(VALUE_SOURCE);
//        String measurement = parameters.getValue(MEASUREMENT);
//        double mathValue = parameters.getValue(MATH_VALUE);
//        boolean showImage = parameters.getValue(SHOW_IMAGE);
//
//        // If applying to a new image, the input image is duplicated
//        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}
//
//        // Updating value if taken from a measurement
//        switch (valueSource) {
//            case ValueSources.MEASUREMENT:
//                mathValue = inputImage.getMeasurement(measurement).getValue();
//                break;
//        }
//
//        int nChannels = inputImagePlus.getNChannels();
//        int nSlices = inputImagePlus.getNSlices();
//        int nFrames = inputImagePlus.getNFrames();
//
//        // Checking the number of dimensions.  If a dimension of image2 is 1 this dimension is used for all images.
//        for (int z = 1; z <= nSlices; z++) {
//            for (int c = 1; c <= nChannels; c++) {
//                for (int t = 1; t <= nFrames; t++) {
//                    inputImagePlus.setPosition(c,z,t);
//
//                    switch (calculationType) {
//                        case CalculationTypes.ADD:
//                            inputImagePlus.getProcessor().add(mathValue);
//                            break;
//
//                        case CalculationTypes.DIVIDE:
//                            inputImagePlus.getProcessor().multiply(1/mathValue);
//                            break;
//
//                        case CalculationTypes.INVERT:
//                            inputImagePlus.getProcessor().invert();
//                            break;
//
//                        case CalculationTypes.MULTIPLY:
//                            inputImagePlus.getProcessor().multiply(mathValue);
//                            break;
//
//                        case CalculationTypes.SUBTRACT:
//                            inputImagePlus.getProcessor().subtract(mathValue);
//                            break;
//
//                    }
//                }
//            }
//        }
//
//        inputImagePlus.setPosition(1,1,1);
//
//        // If selected, displaying the image
//        if (showImage) {
//            new Duplicator().run(inputImagePlus).show();
//        }
//
//        // If the image is being saved as a new image, adding it to the workspace
//        if (!applyToInput) {
//            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
//            Image outputImage = new Image(outputImageName,inputImagePlus);
//            workspace.addImage(outputImage);
//
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
//        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
//        parameters.addParameter(
//                new Parameter(CALCULATION_TYPE,Parameter.CHOICE_ARRAY,CalculationTypes.ADD,CalculationTypes.ALL));
//        parameters.addParameter(
//                new Parameter(VALUE_SOURCE,Parameter.CHOICE_ARRAY, ValueSources.FIXED, ValueSources.ALL));
//        parameters.addParameter(new Parameter(MEASUREMENT,Parameter.IMAGE_MEASUREMENT,null));
//        parameters.addParameter(new Parameter(MATH_VALUE,Parameter.DOUBLE,1.0));
//        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//
//        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
//        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));
//
//        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
//            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
//        }
//
//        returnedParameters.addParameter(parameters.getParameter(CALCULATION_TYPE));
//        returnedParameters.addParameter(parameters.getParameter(VALUE_SOURCE));
//
//        if (parameters.getValue(VALUE_SOURCE).equals(ValueSources.MEASUREMENT)) {
//            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));
//
//            if (parameters.getValue(INPUT_IMAGE) != null) {
//                parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_IMAGE));
//
//            }
//        }
//
//        returnedParameters.addParameter(parameters.getParameter(MATH_VALUE));
//        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public void initialiseImageReferences() {
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetImageReferences() {
//        return null;
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetObjectReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
