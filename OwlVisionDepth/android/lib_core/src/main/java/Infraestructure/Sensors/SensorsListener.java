package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.text.DecimalFormat;
import java.text.ParseException;

public class SensorsListener implements SensorEventListener
{
    private SensorManager sensorManager;
    private static final int SENSOR_BUFFER_SIZE = 10;

    private float[] currentPosition = {0f, 0f, 0f};
    private float[] currentVelocity = {0f, 0f, 0f};
    private long timeInterval;

    float[] accelerometerData = new float[3];
    float[] gyroscopeData = new float[3];

    private float[][] accelerometerBuffer = new float[3][SENSOR_BUFFER_SIZE];
    private float[][] gyroscopeBuffer = new float[3][SENSOR_BUFFER_SIZE];
    private int bufferIndex = 0;
    DecimalFormat decimalFormat = new DecimalFormat("#.##");


    private SensorUpdateCallback sensorUpdateCallback;

    public interface SensorUpdateCallback {
        void onSensorUpdate(float[] positions);
    }

    public void setSensorUpdateCallback(SensorUpdateCallback callback) {
        sensorUpdateCallback = callback;
    }

    public SensorsListener(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public void register()
    {
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            accelerometerBuffer[0][bufferIndex] = event.values[0];
            accelerometerBuffer[1][bufferIndex] = event.values[1];
            accelerometerBuffer[2][bufferIndex] = event.values[2];

            timeInterval = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
        {
            gyroscopeData = event.values.clone();

            if (accelerometerData != null && gyroscopeData != null)
            {
                gyroscopeBuffer[0][bufferIndex] = event.values[0];
                gyroscopeBuffer[1][bufferIndex] = event.values[1];
                gyroscopeBuffer[2][bufferIndex] = event.values[2];

                bufferIndex++;

                if (bufferIndex >= SENSOR_BUFFER_SIZE)
                {
                    bufferIndex = 0;

                    float deltaTime = Math.abs( event.timestamp - timeInterval ) / 1_000_000_000.0f;
                    timeInterval = event.timestamp;

                    float[] avgAcceleration = calculateAverage(accelerometerBuffer);
                    float[] avgGyroscope = calculateAverage(gyroscopeBuffer);

                    try {
                        calculatePositionAndVelocity(deltaTime, avgAcceleration, avgGyroscope);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (sensorUpdateCallback != null) {
                        sensorUpdateCallback.onSensorUpdate(currentPosition);
                    }

                }
            }
        }
    }

    private float[] calculateAverage(float[][] buffer) {
        float[] average = new float[3];
        for (int i = 0; i < 3; i++) {
            float sum = 0;
            for (int j = 0; j < SENSOR_BUFFER_SIZE; j++) {
                sum += buffer[i][j];
            }
            average[i] = sum / SENSOR_BUFFER_SIZE;
        }
        return average;
    }

    private void calculatePositionAndVelocity(float deltaTime, float[] avgAcceleration, float[] avgGyroscope) throws ParseException {
        float accelerationX = avgAcceleration[0];
        float accelerationY = avgAcceleration[1];
        float accelerationZ = avgAcceleration[2];

        currentVelocity[0] += accelerationX * deltaTime;
        currentVelocity[1] += accelerationY * deltaTime;
        currentVelocity[2] += accelerationZ * deltaTime;

        /*float rotationX = avgGyroscope[0];
        float rotationY = avgGyroscope[1];
        float rotationZ = avgGyroscope[2];
        */
        currentPosition[0] += currentVelocity[0] * deltaTime * 100;
        currentPosition[1] += currentVelocity[1] * deltaTime * 100;
        currentPosition[2] += currentVelocity[2] * deltaTime * 100;

        currentPosition[0] =  decimalFormat.parse(decimalFormat.format(currentPosition[0])).floatValue();
        currentPosition[1] =  decimalFormat.parse(decimalFormat.format(currentPosition[1])).floatValue();
        currentPosition[2] =  decimalFormat.parse(decimalFormat.format(currentPosition[2])).floatValue();

        /*currentPosition[0] += rotationX * deltaTime;
        currentPosition[1] += rotationY * deltaTime;
        currentPosition[2] += rotationZ * deltaTime;*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}