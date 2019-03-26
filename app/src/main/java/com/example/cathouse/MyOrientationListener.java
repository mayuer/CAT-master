package com.example.cathouse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MyOrientationListener implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;
    private float lastX;
    private OnOrientationListener mOnOrientationListener;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    public MyOrientationListener(Context context)
    {
        this.mContext=context;
    }
    public void start()
    {
        mSensorManager= (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!= null)
        {
            //获得方向传感器
            accelerometer = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 初始化地磁场传感器
            magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            calculateOrientation();

        }
        //判断是否有方向传感器
        if(mSensor!=null)
        {
            //注册监听器
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);

        }


    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);


    }








    public void stop()
    {
        mSensorManager.unregisterListener(this);

    }
    //方向改变
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION)
        {
            float x=event.values[SensorManager.DATA_X];
            if(Math.abs(x-lastX)>1.0)
            {
                if(mOnOrientationListener!=null)
                {
                    mOnOrientationListener.onOrientationChanged(x);
                }
            }
            lastX=x;

        }

    }
    public void setOnOrientationListener(OnOrientationListener listener)
    {
        mOnOrientationListener=listener;
    }

    public interface OnOrientationListener
    {
        void onOrientationChanged(float x);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
