package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;

public class DetectorFactory {

    public static Detector createFloorDetector(AssetManager assetManager) {
        return new FloorDetector(assetManager);
    }

//    public static Detector createWaterDetector(AssetManager assetManager) {
//        return new WaterDetector(assetManager);
//    }
//
//    public static Detector createWaterFloorOp1Detector(AssetManager assetManager) {
//        return new WaterFloorOp1Detector(assetManager);
//    }
//
//    public static Detector createWaterFloorOp2Detector(AssetManager assetManager) {
//        return new WaterFloorOp2Detector(assetManager);
//    }
    public static Detector createRemoteDetector(Context context) {
        return new RemoteDetector(context);
    }

}
