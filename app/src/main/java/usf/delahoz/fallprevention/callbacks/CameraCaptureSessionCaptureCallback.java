package usf.delahoz.fallprevention.callbacks;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Log;
import android.view.Surface;

import usf.delahoz.fallprevention.Camera2Service;

/**
 * The capture callback has methods that are called when a capture has been progressed and when a
 * capture has been completed. In this case, the callback is used to keep track of the number of
 * frames made initially to the dummy surface while the camera is warming up or auto-adjusting, and
 * after a number of frames have been completed and the camera is ready, the surface used in the
 * capture session is changed from the dummy surface to the real camera surface.
 */
public class CameraCaptureSessionCaptureCallback extends CameraCaptureSession.CaptureCallback {
    /**
     * Number of frames to wait while the camera is warming up before switching from the dummy surface
     * to the real camera surface
     */
    private static final int WAIT_FRAMES = 120;
    private int m = 0;
    private final static String TAG = "Camera Capture callback";
    private Camera2Service cameraService;

    public CameraCaptureSessionCaptureCallback(Camera2Service cameraService) {
        this.cameraService = cameraService;
    }

    @Override
    public void onCaptureProgressed( CameraCaptureSession session,
                                     CaptureRequest request,
                                     CaptureResult partialResult) {
        Log.i(TAG, "CAPTURE PROGRESSED: " + m++);
    }

    @Override
    public void onCaptureCompleted( CameraCaptureSession session,
                                    CaptureRequest request,
                                    TotalCaptureResult result) {
        Log.i(TAG, "CAPTURE COMPLETED: " + m++);
        if (m == WAIT_FRAMES) {
            try {
                // Change the surface of the request session to use the real camera surface and start saving the real pictures
                Surface surface = cameraService.getImageReader().getSurface();
                CaptureRequest captureRequest = cameraService.createCaptureRequest(surface);
                cameraService.getCameraCaptureSession().setRepeatingRequest(captureRequest, null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}