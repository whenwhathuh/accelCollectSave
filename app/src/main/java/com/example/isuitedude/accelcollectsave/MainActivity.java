package com.example.isuitedude.accelcollectsave;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.util.jar.Manifest;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;


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
    //start time
    //curr time

    /*****************************************
    things that happen when the program starts
    *****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        dataFile = new File(path, "accelData.txt");//make a new file to be saved
        if(path.exists()){
            Log.w("directory exists", "directory exists");
        } else {
            Log.w("directory no exist", "directory no exist");
        }

        if(isExternalStorageWritable()){
            dataTextView.setText("storage available");
        } else {
            dataTextView.setText("storage NOT available");
        }

        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put accelerometer data into csv
                dataTextView.setText("Collecting Data");

                //File dataFile = new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "accelData.txt");
                save = true; //allows onSensorChanged method to save accel data
                //dataFile = new File("data/data/com.example.isuitedude.accelcollectsave/accelData.txt");
                //dataFile = new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/accelData.txt");
                try{

                    fos = new FileOutputStream(dataFile);
                    Log.w("file created", "file created");
                } catch(Exception e){
                    Log.w("problem creating", "problem creating");
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
                }catch(Exception e){
                    Log.w("problem closing", "problem closing");
                }
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put file in dateTextView
                dataTextView.setText(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString());
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
            StringBuilder str = new StringBuilder(event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
            try{
                fos.write(str.toString().getBytes());
            }catch(Exception e){
                Log.w("problem writing", "problem writing");
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


}
