package wbif.sjx.MIA.Macro.InputOutput;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_LoadImageFromImageJ extends MacroOperation {
    public MIA_LoadImageFromImageJ(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        // Create Module
        ImageLoader imageLoader = new ImageLoader(modules);

        // Updating parameters
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_IMAGE,(String) objects[0]);
        imageLoader.updateParameterValue(ImageLoader.IMPORT_MODE,ImageLoader.ImportModes.IMAGEJ);
        imageLoader.updateParameterValue(ImageLoader.SEQUENCE_ROOT_NAME,"");
        imageLoader.updateParameterValue(ImageLoader.NAME_FORMAT,ImageLoader.NameFormats.HUYGENS);
        imageLoader.updateParameterValue(ImageLoader.COMMENT,"");
        imageLoader.updateParameterValue(ImageLoader.EXTENSION,"");
        imageLoader.updateParameterValue(ImageLoader.INCLUDE_SERIES_NUMBER,false);
        imageLoader.updateParameterValue(ImageLoader.FILE_PATH,"");
        imageLoader.updateParameterValue(ImageLoader.CHANNELS,"1-end");
        imageLoader.updateParameterValue(ImageLoader.SLICES,"1-end");
        imageLoader.updateParameterValue(ImageLoader.FRAMES,"1-end");
        imageLoader.updateParameterValue(ImageLoader.CHANNEL,0);
        imageLoader.updateParameterValue(ImageLoader.CROP_MODE,ImageLoader.CropModes.NONE);
        imageLoader.updateParameterValue(ImageLoader.REFERENCE_IMAGE,"");
        imageLoader.updateParameterValue(ImageLoader.LEFT,0);
        imageLoader.updateParameterValue(ImageLoader.TOP,0);
        imageLoader.updateParameterValue(ImageLoader.WIDTH,0);
        imageLoader.updateParameterValue(ImageLoader.HEIGHT,0);
        imageLoader.updateParameterValue(ImageLoader.SET_SPATIAL_CAL,false);
        imageLoader.updateParameterValue(ImageLoader.XY_CAL,0d);
        imageLoader.updateParameterValue(ImageLoader.Z_CAL,0d);
        imageLoader.updateParameterValue(ImageLoader.FORCE_BIT_DEPTH,false);
        imageLoader.updateParameterValue(ImageLoader.OUTPUT_BIT_DEPTH,ImageLoader.OutputBitDepths.EIGHT);
        imageLoader.updateParameterValue(ImageLoader.MIN_INPUT_INTENSITY,0d);
        imageLoader.updateParameterValue(ImageLoader.MAX_INPUT_INTENSITY,0d);
        imageLoader.updateParameterValue(ImageLoader.READER,ImageLoader.Readers.BIOFORMATS);
        // imageLoader.updateParameterValue(ImageLoader.THREE_D_MODE,ImageLoader.ThreeDModes.ZSTACK);

        // Running Module
        imageLoader.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName";
    }

    @Override
    public String getDescription() {
        return "Load currently-selected image into MIA workspace.";
    }
}
