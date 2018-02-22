package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;

public class DetectorFactory {

    public static Detector createFloorDetector(AssetManager assetManager) {
        return new FloorDetector(assetManager);
    }

    public static Detector createObjectDetectorSixNoFloor(AssetManager assetManager) {
        return new ObjectOnFloorDetector(assetManager, ObjectOnFloorDetector.ObjectOnFloorMode.SIX_NO_FLOOR);
    }

    public static Detector createObjectDetectorSixFloor(AssetManager assetManager) {
        return new ObjectOnFloorDetector(assetManager, ObjectOnFloorDetector.ObjectOnFloorMode.SIX_FLOOR);
    }

    public static Detector createObjectDetectorNineHundredNoFloor(AssetManager assetManager) {
        return new ObjectOnFloorDetector(assetManager, ObjectOnFloorDetector.ObjectOnFloorMode.NINE_HUNDRED_NO_FLOOR);
    }

    public static Detector createObjectDetectorNineHundredFloor(AssetManager assetManager) {
        return new ObjectOnFloorDetector(assetManager, ObjectOnFloorDetector.ObjectOnFloorMode.NINE_HUNDRED_FLOOR);
    }

    public static Detector createDistanceDetector(AssetManager assetManager) {
        return new DistanceDetector(assetManager);
    }

    public static Detector createWaterDetector(AssetManager assetManager) {
        return new WaterDetector(assetManager);
    }

    public static Detector createWaterFloorOp1Detector(AssetManager assetManager) {
        return new WaterFloorOp1Detector(assetManager);
    }

    public static Detector createWaterFloorOp2Detector(AssetManager assetManager) {
        return new WaterFloorOp2Detector(assetManager);
    }

    public static Detector createRemoteDetector(Context context, String[] models) {
        return new RemoteDetector(context, models);
    }

    public static Detector createAllDetector(AssetManager assetManager, Context context) {
        return new AllDetector(assetManager,context);
    }


}
