package fallprevention.usf.neuralnetwork.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.util.Log;

import fallprevention.usf.neuralnetwork.Camera2Service;

/*the following is a callback object about the state of the camera capture session*/
public class CameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback  {
    private static final String TAG = "Camera Capture State";
    private Camera2Service cameraService;

    public CameraCaptureSessionStateCallback(Camera2Service cameraService) {
        this.cameraService = cameraService;
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        Log.i(TAG, "onConfigured");
            /*the following creates a session for the camera to
             * repeatedly make requests to capture an image (will go until
             * stopped by the user or app crashes) */
        cameraService.setCameraCaptureSession(session);
        try {
            // In the beginning, the capture request works with a dummy surface while the real camera is adjusting
            session.setRepeatingRequest(cameraService.createCaptureRequest(cameraService.getmDummySurface()),
                    cameraService.getCameraCaptureSessionCaptureCallback(), cameraService.getmBackgroundHandler());
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {
        Log.i(TAG, "onConfiguredFailed");
    }
}