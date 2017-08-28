package com.example.anirban.accelerometer_storedata;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
        import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileReader;
        import java.io.FileWriter;
        import java.io.IOException;

        import android.app.Activity;
        import android.content.Context;
        import android.graphics.Color;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.HandlerThread;
        import android.os.Message;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.jjoe64.graphview.GraphView;
        import com.jjoe64.graphview.LegendRenderer;
        import com.jjoe64.graphview.series.DataPoint;
        import com.jjoe64.graphview.series.LineGraphSeries;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

class Acceleration_data{
    float x;
    float y;
    float z;
    int index;
    double normal;
    public static int index_static = 0;
    public Acceleration_data(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
        this.index=index_static++;
        this.normal=sqrt((x*x)+(y*y)+(z*z));

    }
}
class Proximity_data{
    float p;
    int index;
    public static int index_static = 0;
    public Proximity_data(float p){
        this.p=p;
        this.index=index_static++;

    }
}

class Temperature_data{
    float t;
    int index;
    public static int index_static = 0;
    public Temperature_data(float t){
        this.t=t;
        this.index=index_static++;

    }
}

class Light_data{
    float l;
    int index;
    public static int index_static = 0;
    public Light_data(float p){
        this.l=l;
        this.index=index_static++;

    }
}

public class MainActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private SensorManager PSensorManager;
    private SensorManager tSensorManager;
    private SensorManager lSensorManager;
    private Sensor mAccelerometer;
    private Sensor mproximity;
    private Sensor mtemperature;
    private Sensor mlight;
    TextView title,tvx,tvy,tvz,tvp,tvl,tvt;
    EditText etshowval;
    RelativeLayout layout;
    private String acc;
    private String read_str = "";
    //private final String filepath = "/storage/extSdCard/aa.txt"; ///sdcard/AData/accelaration.txt";
    private BufferedWriter mBufferedWriter;
    private BufferedReader mBufferedReader;
   // public List<accelerationData>Data=new ArrayList<>();
    public List<Acceleration_data> accel_data=new ArrayList<>();
    public List<Proximity_data> prox_data=new ArrayList<>();
    public List<Temperature_data> temperature_data=new ArrayList<>();
    public List<Light_data> light_data=new ArrayList<>();
    List<Double> normalData =new ArrayList<>(500);
    double filteredData=0;
    List<Double>filteredDataList=new ArrayList<>();
    private float x;
    private float y;
    private float z;
    private float p;
    private float t;
    private float l;

    GraphView graphView_normal,graphView_normalf,graphView_light,graphView_temperature;
    public static final int MSG_DONE = 1;
    public static final int MSG_ERROR = 2;
    public static final int MSG_STOP = 3;

    private boolean mrunning;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            String str = (String) msg.obj;
            switch (msg.what)
            {
                case MSG_DONE:
                    int num=accel_data.size();
                    int nump=prox_data.size();//accel_data.get(num).index,accel_data.get(num).normal
                    series_normal.appendData(new DataPoint(num,accel_data.get(num-1).normal), false, 200);
                    series_normalf.appendData(new DataPoint(nump,prox_data.get(num-1).p),false,200);
                    Toast.makeText(getBaseContext(), str,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_ERROR:
                    Toast.makeText(getBaseContext(),str,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_STOP:
                    Toast.makeText(getBaseContext(), str,
                            Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private LineGraphSeries<DataPoint> series_normal,series_normalf,series_normall,series_normalt;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        PSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mproximity = PSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mlight = lSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        tSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mtemperature = tSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        PSensorManager.registerListener(this, mproximity,SensorManager.SENSOR_DELAY_NORMAL);

        lSensorManager.registerListener(this, mlight, SensorManager.SENSOR_DELAY_NORMAL);
        tSensorManager.registerListener(this, mtemperature,SensorManager.SENSOR_DELAY_NORMAL);
        //get layout
        graphView_normal =(GraphView)findViewById(R.id.graph_normal);
        graphView_normal.getViewport().setScalable(true);
        graphView_normal.getViewport().setYAxisBoundsManual(true);
        graphView_normal.getViewport().setMinY(0);
        graphView_normal.getViewport().setMaxY(30);

        graphView_normalf =(GraphView)findViewById(R.id.graph_normalf);
        graphView_normalf.getViewport().setScalable(true);
        graphView_normalf.getViewport().setYAxisBoundsManual(true);
        graphView_normalf.getViewport().setMinY(0);
        graphView_normalf.getViewport().setMaxY(30);

        series_normal = new LineGraphSeries<DataPoint>();
        series_normal.setTitle("sqrt(x*x+y*y+z*z)");
        series_normal.setColor(Color.RED);
        series_normalf = new LineGraphSeries<DataPoint>();
        series_normalf.setTitle("PROXIMITY");
        series_normalf.setColor(Color.BLUE);

        /*graphView_normal.addSeries(series_normal);*/
     //   graphView_normal.getLegendRenderer().setVisible(true);
  //  graphView_normal.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

      //  graphView_normalf.addSeries(series_normalf);
   //     graphView_normalf.getLegendRenderer().setVisible(true);
    /*    graphView_normalf.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
*/
        layout = (RelativeLayout) findViewById(R.id.relative);

        //get textviews
        title = (TextView)findViewById(R.id.name);
        tvx = (TextView)findViewById(R.id.xval);
        tvy = (TextView)findViewById(R.id.yval);
        tvz = (TextView)findViewById(R.id.zval);
        tvp = (TextView)findViewById(R.id.aval);
        tvl = (TextView)findViewById(R.id.bval);
        tvt = (TextView)findViewById(R.id.cval);

        //etshowval = (EditText)findViewById(R.id.showval);
        title.setText("Accelerator");

        mHandlerThread = new HandlerThread("Working Thread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(r);
    }

    private Runnable r = new Runnable(){
        @Override
        public void run ()
        {
            while(true)
            {
                if (mrunning)
                {
                    Message msg1 = new Message();
                    try
                    {
                       if( mrunning)
       //                 WriteFile(filepath,acc);
                        //for(int i=0;i<20;i++){//
                        //int num=accel_data.get(0).index_static;//accel_data.get(num).index,accel_data.get(num).normal
                        //    series_normal.appendData(new DataPoint(5,1), false, 200);
                        //}
                        msg1.what = MSG_DONE;
                       // msg1.obj = "Start to write to SD 'acc.txt'";
                    }
                    catch (Exception e)
                    {
                        msg1.what = MSG_ERROR;
                        msg1.obj = e.getMessage();
                    }
                    uiHandler.sendMessage(msg1);
                }
                else
                {
                    Message msg2 = new Message();
                    msg2.what = MSG_STOP;
                    //System.out.println("aaaaa");
                    msg2.obj = "sensor";
                    uiHandler.sendMessage(msg2);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    public void onStartClick(View view)
    {
        start();
    }

  /*  public void onStopClick(View view)
    {
        stop();
    }

    public void onReadClick(View view)
    {
        etshowval.setText(ReadFile(filepath));
    }
*/
    private synchronized void start()
    {
        mrunning = true;
    }

/*    private synchronized void stop()
    {
        mrunning = false;
    }
*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        // TODO Auto-generated method stub

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];

            acc= String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
            if (mrunning)
                accel_data.add(new Acceleration_data(x,y,z));
            tvx.setText("X = "+ String.valueOf(x));
            tvy.setText("Y = "+ String.valueOf(y));
            tvz.setText("Z = "+ String.valueOf(z));
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY )
        {
            p = sensorEvent.values[0];
            if (mrunning)
                prox_data.add(new Proximity_data(p));
            tvp.setText("proximity = "+ String.valueOf(p));
        }/*
        if (sensorEvent.sensor.getType()== Sensor.TYPE_LIGHT)
        {l = sensorEvent.values[0];
            if (mrunning)
                light_data.add(new Light_data(l));
            tvl.setText("light = "+ String.valueOf(l));
        }
        if (sensorEvent.sensor.getType()== Sensor.TYPE_AMBIENT_TEMPERATURE)
        {t = sensorEvent.values[0];
            if (mrunning)
                temperature_data.add(new Temperature_data(t));
            tvt.setText("ambient_temp = "+ String.valueOf(t));
        }*/
    }
/*
    public void CreateFile(String path)
    {
        File f = new File(path);
        try {
            Log.d("ACTIVITY", "Create a File.");
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
*/
/*    public String ReadFile (String filepath)
    {
        mBufferedReader = null;
        String tmp = null;

        if (!FileIsExist(filepath))
            CreateFile(filepath);

        try
        {
            mBufferedReader = new BufferedReader(new FileReader(filepath));
            // Read string
            while ((tmp = mBufferedReader.readLine()) != null)
            {
                tmp += "\n";
                read_str += tmp;
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return read_str;
    }
*/
    /*public void WriteFile(String filepath, String str)
    {   mBufferedWriter = null;

        if (!FileIsExist(filepath))
            CreateFile(filepath);

        try
        {
            mBufferedWriter = new BufferedWriter(new FileWriter(filepath, true));
            mBufferedWriter.write(str);
            mBufferedWriter.newLine();
            mBufferedWriter.flush();
            mBufferedWriter.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/

  /*  public boolean FileIsExist(String filepath)
    {
        File f = new File(filepath);

        if (! f.exists())
        {
            Log.e("ACTIVITY", "File does not exist.");
            return false;
        }
        else
            return true;
    }
*/
    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        mSensorManager.unregisterListener(this);
        Toast.makeText(this, "Unregister accelerometerListener", Toast.LENGTH_LONG).show();
        super.onPause();
    }
}
