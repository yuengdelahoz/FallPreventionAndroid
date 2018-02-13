package usf.delahoz.fallprevention.nn_models;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;

import java.util.Arrays;

import usf.delahoz.fallprevention.Utils;

/**
 * Created by yuengdelahoz on 2/9/18.
 */

public class RemoteDetector implements Detector{
    private RequestQueue requestQueue = null;
    private String URL ="http://research.jadorno.com:8100/fallprevention";
    private String KEY_IMAGE = "image";
    private Context context;
    private long inferenceTime = -1l;
    private final String TAG = getClass().getName();
    private JSONArray nn_models;
    private String filename = null;
    private int exeption_counter = 10;

    public RemoteDetector(Context context, String[] models) {
        this.context = context;
        nn_models = new JSONArray(Arrays.asList(models));
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        filename = activeNetwork.getTypeName()+'_'+Arrays.toString(models)+"_inference_times_trial_"+System.currentTimeMillis()+".csv";
    }

    @Override
    public float[] runInference(Mat image, long start_time) {
        /**
         * This method sends the image to the Python WebAPi and saves the output as a jpg file. It also updates the log file with
         * information useful for debugging purposes (Downloads/Logs/Log.txt)
         * @param im - The image captured
         */
        float [] superpixels = null;
        JSONObject params = new JSONObject();
        try {
            final String encodedImage = Utils.createEncodedImage(image);
            params.put(KEY_IMAGE,encodedImage);
            params.put("models",nn_models);
            params.put("start_time",start_time);
            params.put("filename",filename);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL,params,future,future);

        //Creating a Request Queue
        //Adding request to the queue
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this.context);
        }
            requestQueue.add(jsonRequest);
            JSONObject response = future.get(); // this will block
            JSONArray result = response.getJSONArray("result");
             superpixels = fillData(result.getJSONArray(0));
            Log.d(TAG, "superpixels " + Arrays.toString(superpixels));

        } catch (Exception e) {
            e.printStackTrace();
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            return null;

        }

        this.inferenceTime = (System.currentTimeMillis() - start_time);
        return superpixels;
    }

    @Override
    public long getInferenceRuntime() {
        return inferenceTime;
    }

    private float[] fillData(JSONArray jsonArray){

        float[] fData = new float[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                fData[i] = Float.parseFloat(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return fData;
    }

    @Override
    public String getInferenceRuntimeFilename() {
        return filename;
    }

}
