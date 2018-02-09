package usf.delahoz.fallprevention;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_CAMERA = 113;
    private static final int REQUEST_READ_STORAGE = 114;
    private String TAG = getClass().getName();

    boolean canWrite = false;
    boolean useCamera = false;
    boolean canRead = false;

    private Button btn;
    private Spinner operation_mode;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int [] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canWrite = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "write request");
                }
                /** ****************************************************************/
            case REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useCamera = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "camera request");
                }
                /** ****************************************************************/
            case REQUEST_READ_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canRead = true;
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    Log.i("requests", "read request");
                }
                /** ****************************************************************/

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean hasReadPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasReadPermission){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
        /** ****************************************************************/
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
/** ****************************************************************/
        boolean camPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        if (!camPermission){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }


//        Choose to run models locally or through the webapi
        operation_mode = (Spinner) findViewById(R.id.operation_mode);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operation_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operation_mode.setAdapter(adapter);


        final Intent processImage = new Intent(this,Camera2Service.class);
        btn = (Button) findViewById(R.id.startBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                processImage.putExtra("operation_mode",operation_mode.getSelectedItem().toString());
                if (btn.getText().toString().equals("Launch")) {  //Start
                    startService(processImage);
                    btn.setText("Stop");
                } else if (btn.getText().toString().equals("Stop")) { //Stop
                    stopService(processImage);
                    btn.setText("Launch");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
