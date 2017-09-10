package waterdetection.usf.waterdetectionandroid.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.media.ImageReader;
import android.util.Log;
import android.view.Surface;

import java.util.Arrays;

import waterdetection.usf.waterdetectionandroid.Camera2Service;

/*the following is a callback object for the states of the camera device*/
public class CameraStateCallback extends CameraDevice.StateCallback {
    private final static String TAG = "Camera State Callback";
    private Camera2Service cameraService;

    public CameraStateCallback(Camera2Service cameraService) {
        this.cameraService = cameraService;
    }

    @Override
    public void onOpened(CameraDevice camera) {
        Log.i(TAG, "onOpened");
        //sets the cameraDevice variable to the opened camera
        cameraService.setCameraDevice(camera);
            /*the following  gives a surface to the images captured by the camera.
            * this is important so we can display the images later. We pass a dummy surface we use while the
            * camera is auto-adjusting or warming up, and the read camera surface to use later */
        try {
            cameraService.getCameraDevice().createCaptureSession(Arrays.asList(cameraService.getmDummySurface(),
                    cameraService.getImageReader().getSurface()), cameraService.getCameraCaptureSessionStateCallback(), null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            cameraService.stopBackgroundThread();
        }
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        Log.i(TAG, "onDisconnected");
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        Log.e(TAG, "onError");
    }

        /* onOpened, onDisconnected, and onError are states the camera
        * device can be in, similar to the states of an app*/
}