package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;

public class DetectorFactory {

    public static Detector createFloorDetector(AssetManager assetManager, File albumStorageDir, boolean isWritable) {
        return new FloorDetector(assetManager, albumStorageDir, isWritable);
    }

    public static Detector createWaterDetector(AssetManager assetManager, File albumStorageDir, boolean isWritable) {
        return new WaterDetector(assetManager, albumStorageDir, isWritable);
    }

    public static Detector createWaterFloorOp1Detector(AssetManager assetManager, File albumStorageDir, boolean isWritable) {
        return new WaterFloorOp1Detector(assetManager, albumStorageDir, isWritable);
    }

    public static Detector createWaterFloorOp2Detector(AssetManager assetManager, File albumStorageDir, boolean isWritable) {
        return new WaterFloorOp2Detector(assetManager, albumStorageDir, isWritable);
    }
    public static Detector createRemoteDetector(Context context) {
        return new RemoteDetector(context);
    }

}
