package waterdetection.usf.waterdetectionandroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import waterdetection.usf.waterdetectionandroid.callbacks.CameraCaptureSessionCaptureCallback;
import waterdetection.usf.waterdetectionandroid.callbacks.CameraCaptureSessionStateCallback;
import waterdetection.usf.waterdetectionandroid.callbacks.CameraStateCallback;
import waterdetection.usf.waterdetectionandroid.callbacks.ImageAvailableCallback;
import waterdetection.usf.waterdetectionandroid.callbacks.OpenCvCallback;
import waterdetection.usf.waterdetectionandroid.detection.modes.DetectorFactory;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

/**
 * Created by Panos on 11/11/2016.
 */
/**************************************************************************************
 * Yueng. The code below allows for the continuous retrieval of frames
 * from the camera. Camera2 API by Google is used. The files are being saved as JPEG's. Also,
 * you will find something you might be unfamiliar with in this code. The unfamiliarity at hand
 * is known as... a... comment. Currently, what you are reading, is a comment. The gray lines
 * in the code, are comments.  They are used to help you understand unfamiliar code,
 * or code you have forgotten all about.
 *
 * This code will take you on a journey of self discovery, self loathing,
 * fear, anxiety, and finally, a false sense of accomplishment.
 *
 * Enjoy!
 * **********************************************************************************/
//class definition for the camera service, extending a service
public class Camera2Service extends Service {
    /*the following variables define the tag for logs, the camera, the camera device,
     * the session for the capture session of the camera,an image reader to handle the image,
     * a handler thread to run the service on a separate thread to not block the ui,
     * and a handler to run with the new thread*/
    private static final String TAG = "Camera2Service";
    private static final int CAMERA = CameraCharacteristics.LENS_FACING_BACK;
    private CameraDevice cameraDevice;
    private CameraCaptureSession session;
    private ImageReader imageReader;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraCharacteristics mCharacteristics;
    // Callbacks
    private OpenCvCallback opencv_callback = new OpenCvCallback(this);
    private CameraStateCallback cameraStateCallback = new CameraStateCallback(this);
    private CameraCaptureSessionStateCallback cameraCaptureSessionStateCallback = new CameraCaptureSessionStateCallback(this);
    private CameraCaptureSessionCaptureCallback cameraCaptureSessionCaptureCallback = new CameraCaptureSessionCaptureCallback(this);
    private ImageAvailableCallback onImageAvailableListener;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    // When the Camera2Service service is started, it used to instantly make requests to the phone
    // camera. In some devices the camera takes a little to "warm up" or autocalibrate, and the output
    // pictures would be too dark and out of focus. For this reason, while the camera is auto-adjusting,
    // we make the requests to a dummy surface and these requests will not generate pictures. After WAITING_FRAMES
    // frames have been requested, we change the surface used in the capture session to use the real camera and
    // start saving the real pictures.
    private SurfaceTexture mDummyPreview = new SurfaceTexture(1);
    private Surface mDummySurface = new Surface(mDummyPreview);

    //state of the app once tapped on
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate from camera service");
        super.onCreate();
        //intent to be used with the "main" class of the app
        Intent mainActivity = new Intent(this, Camera_Activity.class);
        final Notification.Builder mBuilder = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Camera Service Notification")
                .setContentText("Processing images in the background");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Camera_Activity.class);
        stackBuilder.addNextIntent(mainActivity);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        this.onImageAvailableListener =  new ImageAvailableCallback(DetectorFactory.createWaterFloorOp2Detector(getAssets(), getAlbumStorageDir("Exec times"), isExternalStorageWritable()));
        startForeground(41413, mBuilder.build());
    }

    public File getAlbumStorageDir(String albumName) {
        // Path is Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),albumName);
        if (!file.mkdirs()) {
            // Shows this error also when directory already existed
            Log.e("Error", "Directory not created");
        }
        return file;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Return the Camera Id which matches the field CAMERA.
     */
    public String getCamera(CameraManager manager) {
        Log.i(TAG, "getCamera from camera service");
        // getCameraIdList() returns a list of currently connected camera devices
        try {
            for (String cameraId : manager.getCameraIdList()) {
                //getCameraCharacteristics() presents the capabilities of the selected camera
                mCharacteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = mCharacteristics.get(CameraCharacteristics.LENS_FACING);
                Log.i("camera2id", cameraId);
                if (cOrientation == CAMERA) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* the following starts the intent for working with the main activity. if everything
    * gets setup as it should, our camera will start reading images at the below specified
    * desired format, size, and repeat interval. */
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"Current thread is " + Thread.currentThread().getName());
        Log.i(TAG, "onStartCommand from camera service");
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, opencv_callback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            opencv_callback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        //background thread started, so we do not block ui thread
        startBackgroundThread();
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            imageReader = ImageReader.newInstance(500, 500, ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);
            Log.i(TAG, "onStartCommand");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraOpenCloseLock.release();
            /* this shows error because it is looking for a request made to the
            * user to allow access of the camera. this check is done in the main activity file,
            * so we can ignore the presented error.
            *
            * or not, whatever...*/
            manager.openCamera(getCamera(manager), cameraStateCallback, null); /** ignore error. warns about checking permissions. this happens in main*/
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //state of the app if it is closed or has crashed
    @Override
    public void onDestroy() {
        closeCamera();
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //a package of setting and outputs needed to capture an image from the camera device
    public CaptureRequest createCaptureRequest(Surface surface) {
        try {
            //"builds" a request to capture an image
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            /* from the built request to capture an image, here we get the surface
             to be used to project the image on*/
            builder.addTarget(surface);
            /** this needs to be fixed*/
            builder.set(CaptureRequest.JPEG_ORIENTATION, mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION));
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            //returns the "build" request made
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /** Stops the background thread and its {@link Handler}.*/
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     *
     * This method also terminates the CameraDevice and the ImageReader.
     */
    private void closeCamera() {
        Log.i(TAG,"Closing Camera");
        Toast.makeText(this, "stopping service", Toast.LENGTH_SHORT).show();
        try {
            mCameraOpenCloseLock.acquire();
            if (null != session) {
                session.close();
                session = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            stopBackgroundThread();
            mCameraOpenCloseLock.release();
            Log.i(TAG,"Closed Camera");
        }
    }

    public CameraDevice getCameraDevice() {
        return this.cameraDevice;
    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }

    public Surface getmDummySurface() {
        return this.mDummySurface;
    }

    public ImageReader getImageReader() {
        return this.imageReader;
    }

    public CameraCaptureSessionStateCallback getCameraCaptureSessionStateCallback() {
        return this.cameraCaptureSessionStateCallback;
    }

    public CameraCaptureSession getCameraCaptureSession() {
        return this.session;
    }

    public void setCameraCaptureSession(CameraCaptureSession session) {
        this.session = session;
    }

    public Handler getmBackgroundHandler() {
        return this.mBackgroundHandler;
    }

    public CameraCaptureSessionCaptureCallback getCameraCaptureSessionCaptureCallback() {
        return this.cameraCaptureSessionCaptureCallback;
    }
}