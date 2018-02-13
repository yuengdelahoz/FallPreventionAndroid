package usf.delahoz.fallprevention.nn_models;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

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
import java.util.concurrent.ExecutionException;

import usf.delahoz.fallprevention.ImageCollectorService;
import usf.delahoz.fallprevention.MainActivity;
import usf.delahoz.fallprevention.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by yuengdelahoz on 2/9/18.
 */

public class RemoteDetector implements Detector{
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
        final String encodedImage = Utils.createEncodedImage(image);
        JSONObject params = new JSONObject();
        try {
            params.put(KEY_IMAGE,encodedImage);
            params.put("models",nn_models);
            params.put("start_time",start_time);
            params.put("filename",filename);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL,params,future,future);

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        //Adding request to the queue
        requestQueue.add(jsonRequest);
        float [] superpixels = null;
        try {
            JSONObject response = future.get(); // this will block
            JSONArray result = response.getJSONArray("result");
             superpixels = fillData(result.getJSONArray(0));
            Log.d(TAG, "superpixels " + Arrays.toString(superpixels));

        } catch (Exception e) {
            exeption_counter--;
            if (exeption_counter < 1){
                e.printStackTrace();
                ImageCollectorService imagecollectorservice = (ImageCollectorService) this.context;
                imagecollectorservice.setStopping_reason("due to connectivity issues with the server.");
                imagecollectorservice.stopSelf();


                Intent intent = new Intent(context,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                imagecollectorservice.startActivity(intent);

            }
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
