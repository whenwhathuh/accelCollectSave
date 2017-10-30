package com.example.isuitedude.accelcollectsave;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Manifest;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static javax.xml.xpath.XPathConstants.STRING;


public class MainActivity extends AppCompatActivity implements SensorEventListener {//SensorEventLestener requires methods onAccuracyChanged, onSensorChanged
    /****
    views
    ****/
    TextView accelXview;
    TextView accelYview;
    TextView accelZview;
    ScrollView dataView;
    TextView dataTextView;
    Button startButton;
    Button stopButton;
    Button viewButton;

    /**********
     file stuff
    **********/
    Boolean save = false;//should onSensordChange save sensor values to a file
    File path;//the directory
    File dataFile;//the file that will be saved
    OutputStream fos = null;

    /***********
    sensor stuff
    ***********/
    private SensorManager sensManager;
    Sensor myAccel;

    /***********
    timing stuff - for adding time since start of data collection in csv
    ***********/
    Date startTime;
    Date currTime;
    Long elapsedTime;

    /*****************************************
    things that happen when the program starts
    *****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>22){
            requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            Log.i("request", "requested permission");
        }

        accelXview = (TextView) findViewById(R.id.accelXview);
        accelYview = (TextView) findViewById(R.id.accelYview);
        accelZview = (TextView) findViewById(R.id.accelZview);
        dataView = (ScrollView) findViewById(R.id.dataView);
        dataTextView = (TextView) findViewById(R.id.dataTextView);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        viewButton = (Button) findViewById(R.id.viewButton);

        sensManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        myAccel = sensManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensManager.registerListener(this, myAccel, 100000); //must implement SensorEventListener

        path = getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        try{
            dataFile = new File(path, "accelData.txt");//make a new file to be saved
        } catch (Exception e){
            Log.e("error", "asdf", e);
        }

        if(path.exists()){
            Log.w("Log: directory exists", "directory exists");
        } else {
            Log.w("Log: directory no exist", "directory no exist");
        }

        //testTimer();

        //dataTextView.setText(startTime.toString());

//        if(isExternalStorageWritable()){
//            dataTextView.setText("storage available");
//        } else {
//            dataTextView.setText("storage NOT available");
//        }

        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put accelerometer data into csv
                dataTextView.setText("Collecting Data");

                //File dataFile = new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "accelData.txt");
                save = true; //allows onSensorChanged method to save accel data
                startTime = new Date();
                //dataFile = new File("data/data/com.example.isuitedude.accelcollectsave/accelData.txt");

                try{

                    fos = new FileOutputStream(dataFile);
                    Log.w("Log: file created", "file created");
                } catch(Exception e){
                    Log.e("did not create", "could not create file", e);
                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //close the file
                dataTextView.setText("Stopped Collecting Data");
                save = false;
                try{
                    fos.close();
                    MediaScannerConnection.scanFile(
                            getApplicationContext(),
                            new String[]{dataFile.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.w("grokkingandroid",
                                            "file " + path + " was scanned seccessfully: " + uri);
                                }
                            });
                }catch(Exception e){
                    Log.w("Log: problem closing", "problem closing");
                }
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put file in dateTextView
                dataTextView.setText(getExternalStoragePublicDirectory(DIRECTORY_DCIM).toString());
            }
        });
    }//end onCeate method

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public final void onSensorChanged(SensorEvent event){
        accelXview.setText(String.format("%.2f", event.values[0]));
        accelYview.setText(String.format("%.2f", event.values[1]));
        accelZview.setText(String.format("%.2f", event.values[2]));
        if(save){
            currTime = new Date();
            SimpleDateFormat date = new SimpleDateFormat("SSSS");
            Log.w("startTime", date.format(startTime));
            Log.w("currTime", date.format(currTime));
            elapsedTime = currTime.getTime() - startTime.getTime();
            Log.w("elapsedTime", date.format(elapsedTime));
            StringBuilder str = new StringBuilder(elapsedTime.toString() + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            try{
                fos.write(str.toString().getBytes());
            }catch(Exception e){
                Log.w("Log: problem writing", "problem writing");
            }
        }//end if
    }//end onSensorChanged method

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    void testTimer(){
        for(int i = 0; i < 100000000; i++);
        currTime = new Date();
        elapsedTime = currTime.getTime() - startTime.getTime();
        dataTextView.setText(elapsedTime.toString());
    }


}//end MainActivity
