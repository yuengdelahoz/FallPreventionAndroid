package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import usf.delahoz.fallprevention.Utils;

/**
 * Created by yuengdelahoz on 2/9/18.
 */

public class RemoteDetector implements Detector {
    private List<Long> requests = new ArrayList<>();
    private String URL ="http://enb302.online:8001/delahoz/";
    private String KEY_IMAGE = "image";
    private Context context;


    public RemoteDetector(Context context) {
        this.context = context;

    }

    @Override
    public Mat runInference(Mat image) {
        /**
         * This method sends the image to the Python WebAPi and saves the output as a jpg file. It also updates the log file with
         * information useful for debugging purposes (Downloads/Logs/Log.txt)
         * @param im - The image captured
         */
        final String encodedImage = createEncodedImage(image);
        final Long startEndtime = System.currentTimeMillis();
        requests.add(startEndtime);
        Utils.mSaveData("Log.txt", "RAUL WEB API SEND PIC de " + startEndtime, Utils.getAlbumStorageDir("Logs"));
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onWebApiResponse(startEndtime, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onWebApiError(startEndtime, error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String,String> params = new Hashtable<>();
                //Adding parameters
                params.put(KEY_IMAGE, encodedImage);
                //returning parameters
                return params;
            }
        };
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        //Adding request to the queue
        requestQueue.add(stringRequest);
        return null;
    }

    /**
     * Creates a base64 encoded image from the captured image
     * @param im - The map object representing the captured image
     * @return - The image encoded as base64 string
     */
    private String createEncodedImage(Mat im) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", im, matOfByte);
        byte[] imageBytes = matOfByte.toArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    /**
     * This method is called when we receive the response from the WebApi server. We update the log file, save the base64 image as a .jpg image in the phone and
     * check if there are any more pending requests whose responses have not been received. If there is any pending request still, we will keep using the dummysurface
     * instead of the real camera so that we do not capture any more images because if we do, we would be sending more requests to the WEbApi server than we can handle.
     * If there are no pending requests, then we change the capture requests so that it uses the real camera and the app starts capturing images again.
     * @param startEndtime - Time when the request was sent
     * @param response - Base64 encoded image
     */
    private void onWebApiResponse(Long startEndtime, String response) {
        Long endTime = System.currentTimeMillis();
        Utils.mSaveData("Log.txt", "RAUL WEB API RESPONSE de " + startEndtime  + ". Lista de requests pendientes: " + Arrays.toString(requests.toArray()), Utils.getAlbumStorageDir("Logs"));
        Utils.mSaveData("WebApiOP1.txt", (endTime - startEndtime) + "\n", Utils.getAlbumStorageDir("Exec times"));
        Utils.SaveImage(response);
        requests.remove(startEndtime);
//        if (requests.isEmpty() && getImageReader() != null) {
//            Surface surface = getImageReader().getSurface();
//            CaptureRequest captureRequest = createCaptureRequest(surface);
//            try {
//                getCameraCaptureSession().setRepeatingRequest(captureRequest, null, null);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * This method is called when there was an error with the WebApi HTTP request. We update the log file (Downloads/Logs/Log.txt)
     * This file can be used for debugging purposes to see how the app is working with the WebApi requests and responses, and to check for errors.
     * @param startEndtime - The time when we sent the request, which is also the request id
     * @param error - The error returned
     */
    private void onWebApiError(Long startEndtime, VolleyError error) {
        Utils.mSaveData("Log.txt", "RAUL WEB API ERROR de" + startEndtime, Utils.getAlbumStorageDir("Logs"));
        if (error != null) {
            Utils.mSaveData("Log.txt", error.getMessage(), Utils.getAlbumStorageDir("Logs"));
        }
    }

//    /**
//     * Changes the capture request so that it uses a dummy surface instead of the camera and therefore the
//     * app does not receive any more captured images
//     */
//    private void assignDummySurface() {
//        CaptureRequest captureRequest = createCaptureRequest(mDummySurface);
//        try {
//            getCameraCaptureSession().setRepeatingRequest(captureRequest, null, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

}
