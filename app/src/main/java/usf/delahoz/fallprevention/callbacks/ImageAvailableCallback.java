package usf.delahoz.fallprevention.callbacks;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import usf.delahoz.fallprevention.Camera2Service;
import usf.delahoz.fallprevention.ImageTools;
import usf.delahoz.fallprevention.detection.modes.Detector;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;

/* the following acquires an image reader from the listener. this in turn lets us call the
        * method to acquire the latest image*/
public class ImageAvailableCallback implements ImageReader.OnImageAvailableListener {
    public enum ImageProcessingMode { WEBAPI, LOCAL};
    private ImageTools mat = new ImageTools();
    private Detector detector;
    private Camera2Service camera2Service;
    private ImageProcessingMode mode;
    private String TAG = getClass().getName();

    public ImageAvailableCallback(Detector detector, Camera2Service camera2Service, ImageProcessingMode mode ) {
        this.detector = detector;
        this.camera2Service = camera2Service;
        this.mode = mode;
        Log.d(TAG,"Constructor: Processing Image using mode " + mode);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
//        Log.d(TAG," onImageAvailable: Processing Image using mode " + mode);
            /* the following variables are used to convert the data we get to bytes,
            * and re-construct them to finally create and save an image file*/
        Image img;
        ByteBuffer buffer;
        //pretty self explanatory. like, c'mon now. read the line. lazy...
        img = reader.acquireLatestImage();
            /*the full code below would also have "if-else" or "else" statements
            * to check for other types of retrieved images/files */
        if (img == null) return;
        if (img.getFormat() == ImageFormat.JPEG) {
            //check if we have external storage to write to. if we do, save acquired image
            if (isExternalStorageWritable())
            {
                    /*method to create a new file/item/object and set
                    it up for a JPEG assignment*/
                //imgFile = CreateJPEG();
                try {
                    Mat im = createInputMat(img);
                    // The two following lines send the image captured to the NN model in the Android app and saves the output
                    //mat.SaveImage(fin,System.currentTimeMillis()); //Save the output image
                    ImageTools.SaveImage(im,System.currentTimeMillis());
                    switch (mode){
                        case LOCAL:
//                            Mat fin = detector.performDetection(im);
                            break;
                        case WEBAPI:
//                            camera2Service.sendPic(im);
                            break;
                    }

                    // The following line sends the image captured to the Python WebApi server over the network

                    img.close();
                } catch (Exception e) {
                    Log.i("Exception e", "ImageFormat.JPEG,,,,,,," + e.getMessage());
                    e.getStackTrace();
                }
            }
        }
    }

    /**
     * From the captured image it creates the 500x500 input Mat object
     * @param img - Captured image
     * @return - TF Model input mat
     */
    private Mat createInputMat(Image img) {
        /*bytebuffer is a class that allows us to read/write bytes
                    * here it is used with "Save(... , ...)" to assign these
                    * collected bytes from the image to the created file from "CreateJpeg()"*/
        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        int h = img.getHeight();
        int w = img.getWidth();
        Mat imgMat = new Mat(w,h, CvType.CV_32FC3);

        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        // Obtain a float array with the values of the image captured. The classifier
        // uses this float array to perform the inference
        MatOfByte m = new MatOfByte(bytes);
        imgMat = imdecode(m, IMREAD_COLOR);
        // We need to resize the image because the floor detection model expects an input
        // image with dimensions 500x500
        Size szResized = new Size(240,240);
        Mat mSource = imgMat;
        Mat mResised = new Mat();
        Imgproc.resize(mSource, mResised, szResized,0,0, Imgproc.INTER_AREA);
        Mat im = new Mat();
        mResised.assignTo(im, CvType.CV_8UC3);
        return im;
    }

    //checks if there is external storage to write to
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}