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
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    TextView gyroXview;
    TextView gyroYview;
    TextView gyroZview;
    TextView pressureView;
    TextView tempView;
    ScrollView dataView;
    TextView dataTextView;
    Button startButton;
    Button stopButton;
    //Button viewButton;

    /**********
     file stuff
     **********/
    Boolean save = false;//should onSensordChange save sensor values to a file
    File path;//the directory
    File accelDataFile;//the file that will be saved
    File baroDataFile;
    OutputStream accelFOS = null;
    OutputStream baroFOS = null;

    /***********
     sensor stuff
     ***********/
    private SensorManager sensManager;
    Sensor myAccel;
    Sensor myGeo;
    Sensor myBaro;
    Sensor myTemp;

    /***********
     timing stuff - for adding time since start of data collection in csv
     ***********/
    /*Date startTime;
    Date currTime;
    Long elapsedTime;
    List<Float> timeStamps = new ArrayList<>();*/
    long startTime;
    long currTime;
    float elapsedTime;
    float totalTime;
    //List<Float> timeStamps = new ArrayList<>();

    /*****************************************
     things that happen when the program starts
     *****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT>22){
            requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            //Log.i("request", "requested permission");
        }

        accelXview = (TextView) findViewById(R.id.accelXview);
        accelYview = (TextView) findViewById(R.id.accelYview);
        accelZview = (TextView) findViewById(R.id.accelZview);
        gyroXview = (TextView) findViewById(R.id.gyroXview);
        gyroYview = (TextView) findViewById(R.id.gyroYview);
        gyroZview = (TextView) findViewById(R.id.gyroZview);
        pressureView = (TextView) findViewById(R.id.pressureView);
        tempView = (TextView) findViewById(R.id.tempView);
        dataView = (ScrollView) findViewById(R.id.dataView);
        dataTextView = (TextView) findViewById(R.id.dataTextView);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        //viewButton = (Button) findViewById(R.id.viewButton);

        sensManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        myAccel = sensManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myGeo = sensManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        myBaro = sensManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        myTemp = sensManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensManager.registerListener(this, myAccel, SensorManager.SENSOR_DELAY_NORMAL); //must implement SensorEventListener
        sensManager.registerListener(this, myGeo, SensorManager.SENSOR_DELAY_NORMAL);
        sensManager.registerListener(this, myBaro, SensorManager.SENSOR_DELAY_NORMAL);
        sensManager.registerListener(this, myTemp, SensorManager.SENSOR_DELAY_NORMAL);

        path = getExternalStoragePublicDirectory(DIRECTORY_DCIM);
        try{
            accelDataFile = new File(path, "accelData.txt");//make a new file to be saved
            baroDataFile = new File(path, "baroData.txt");
        } catch (Exception e){
            //Log.e("error", "asdf", e);
        }

        /*if(path.exists()){
            Log.w("Log: directory exists", "directory exists");
        } else {
            Log.w("Log: directory no exist", "directory no exist");
        }*/

        //testTimer();

        //dataTextView.setText(startTime.toString());

//        if(isExternalStorageWritable()){
//            dataTextView.setText("storage available");
//        } else {
//            dataTextView.setText("storage NOT available");
//        }
        /************
         start button
         ************/
        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put accelerometer data into csv
                dataTextView.setText("Collecting Data");

                //File dataFile = new File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "accelData.txt");
                save = true; //allows onSensorChanged method to save accel data
                //startTime = new Date();
                startTime = System.currentTimeMillis();
                //dataFile = new File("data/data/com.example.isuitedude.accelcollectsave/accelData.txt");

                try{

                    accelFOS = new FileOutputStream(accelDataFile);
                    baroFOS = new FileOutputStream(baroDataFile);
                    //Log.w("Log: file created", "file created");
                } catch(Exception e){
                    //Log.e("did not create", "could not create file", e);
                }

            }
        });

        /************
         stop button
         *************/
        stopButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //close the file
                currTime = System.currentTimeMillis();
                totalTime = (currTime - startTime) * (1/1000f);
                dataTextView.setText("Stopped Collecting Data\nData collection ran for " + totalTime + " seconds.");
                save = false;
                try{
                    accelFOS.close();
                    baroFOS.close();
                    Log.w("closed files", "closed files");
                    MediaScannerConnection.scanFile(
                            getApplicationContext(),
                            new String[]{accelDataFile.getAbsolutePath(), baroDataFile.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.w("balls",
                                            "file " + path + " was scanned seccessfully: " + uri);
                                }
                            });
                    Log.w("got here", "got here");
                }catch(Exception e){
                    Log.w("Log: problem closing", "problem closing");
                }
            }
        });

       /* viewButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //put file in dateTextView
                dataTextView.setText(getExternalStoragePublicDirectory(DIRECTORY_DCIM).toString());
            }
        });*/
    }//end onCeate method

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){

    }
    /*************
     sensor changed
     **************/
    @Override
    public final void onSensorChanged(SensorEvent event){
        currTime = System.currentTimeMillis();
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelXview.setText(String.format("%.2f", event.values[0]));
            accelYview.setText(String.format("%.2f", event.values[1]));
            accelZview.setText(String.format("%.2f", event.values[2]));
            if(save) {
                elapsedTime = (currTime - startTime) * (1 / 1000f);
            /*
            Log.w("startTime", Long.toString(startTime));
            Log.w("currTime", Long.toString(currTime));
            Log.w("elapsedTime", Float.toString(elapsedTime));
            */
                StringBuilder str = new StringBuilder(elapsedTime + "," + event.values[0] + "," + event.values[1] + "," + event.values[2] + "\n");
                try {
                    accelFOS.write(str.toString().getBytes());
                } catch (Exception e) {
                    //Log.w("Log: problem writing", "problem writing");
                }
            }
        }
        else if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroXview.setText(String.format("%.2f", event.values[0]));
            gyroYview.setText(String.format("%.2f", event.values[1]));
            gyroZview.setText(String.format("%.2f", event.values[2]));
        }
        else if(sensor.getType() == Sensor.TYPE_PRESSURE){
            pressureView.setText(Float.toString(event.values[0]));
            if(save){
                elapsedTime = (currTime - startTime) * (1 / 1000f);
                StringBuilder str = new StringBuilder(elapsedTime + "," + event.values[0] + "\n");
                try{
                    baroFOS.write(str.toString().getBytes());
                } catch (Exception e){}
            }
        }
        else if(sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
            tempView.setText(Float.toString(event.values[0]));
        }//end if

    }//end onSensorChanged method

    /* Checks if external storage is available for read and write */
   /* public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }*/

    /*void testTimer(){
        for(int i = 0; i < 100000000; i++);
        currTime = new Date();
        elapsedTime = currTime.getTime() - startTime.getTime();
        dataTextView.setText(elapsedTime.toString());
    }*/


}//end MainActivity