package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.video.KalmanFilter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class KalmanSensors implements SensorEventListener {
    private SensorManager sensorManager;
    private static final int SENSOR_BUFFER_SIZE = 20;

    private float[] currentPosition = {0f, 0f, 0f};
    private float[] currentVelocity = {0f, 0f, 0f};
    private long timeInterval;

    private float[] accelerometerData = new float[3];
    private float[] gyroscopeData = new float[3];
    private float[] magneticFieldData = new float[3];

    private float[][] accelerometerBuffer = new float[3][SENSOR_BUFFER_SIZE];
    private float[][] gyroscopeBuffer = new float[3][SENSOR_BUFFER_SIZE];
    private float[][] magneticFieldBuffer = new float[3][SENSOR_BUFFER_SIZE];
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private int bufferIndex = 0;

    private KalmanFilter kalmanFilter;

    public KalmanSensors(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        initKalmanFilter();
    }

    private SensorUpdateCallback sensorUpdateCallback;

    public interface SensorUpdateCallback {
        void onSensorUpdate(float[] positions);
    }

    public void setSensorUpdateCallback(SensorUpdateCallback callback) {
        sensorUpdateCallback = callback;
    }

    private void initKalmanFilter() {
        int stateSize = 6;
        int measurementSize = 3;
        int controlSize = 0;

        kalmanFilter = new KalmanFilter(stateSize, measurementSize, controlSize, CvType.CV_32F);

        // Transition matrix A
        Mat transitionMatrix = Mat.eye(stateSize, stateSize, CvType.CV_32F);
        kalmanFilter.set_transitionMatrix(transitionMatrix);

        // Measurement matrix H
        Mat measurementMatrix = Mat.zeros(measurementSize, stateSize, CvType.CV_32F);
        measurementMatrix.put(0, 0, 1, 0);  // x
        measurementMatrix.put(1, 1, 1, 0);  // y
        measurementMatrix.put(2, 2, 1, 0);  // z
        kalmanFilter.set_measurementMatrix(measurementMatrix);

        // Process noise covariance matrix Q
        Mat processNoiseCov = Mat.eye(stateSize, stateSize, CvType.CV_32F);
        processNoiseCov = processNoiseCov.mul(processNoiseCov, 0.01);
        kalmanFilter.set_processNoiseCov(processNoiseCov);

        // Measurement noise covariance matrix R
        Mat measurementNoiseCov = Mat.eye(measurementSize, measurementSize, CvType.CV_32F);
        measurementNoiseCov = measurementNoiseCov.mul(measurementNoiseCov, 0.1);
        kalmanFilter.set_measurementNoiseCov(measurementNoiseCov);

        // Initial state estimate
        Mat state = Mat.zeros(stateSize, 1, CvType.CV_32F);
        kalmanFilter.set_statePre(state);

        // Initial error covariance matrix P
        Mat errorCov = Mat.eye(stateSize, stateSize, CvType.CV_32F);
        kalmanFilter.set_errorCovPre(errorCov);
    }

    public void register() {
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerBuffer[0][bufferIndex] = event.values[0];
            accelerometerBuffer[1][bufferIndex] = event.values[1];
            accelerometerBuffer[2][bufferIndex] = event.values[2];

            timeInterval = event.timestamp;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeBuffer[0][bufferIndex] = event.values[0];
            gyroscopeBuffer[1][bufferIndex] = event.values[1];
            gyroscopeBuffer[2][bufferIndex] = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldBuffer[0][bufferIndex] = event.values[0];
            magneticFieldBuffer[1][bufferIndex] = event.values[1];
            magneticFieldBuffer[2][bufferIndex] = event.values[2];

            bufferIndex++;

            if (bufferIndex >= SENSOR_BUFFER_SIZE) {
                bufferIndex = 0;

                float deltaTime = Math.abs(event.timestamp - timeInterval) / 1_000_000_000.0f;
                timeInterval = event.timestamp;

                float[] avgAcceleration = calculateAverage(accelerometerBuffer);
                float[] avgGyroscope = calculateAverage(gyroscopeBuffer);
                float[] avgMagneticField = calculateAverage(magneticFieldBuffer);

                // Apply Kalman filter to estimate position
                Mat measurement = new Mat(3, 1, CvType.CV_32F);
                measurement.put(0, 0, avgAcceleration[0]);
                measurement.put(1, 0, avgAcceleration[1]);
                measurement.put(2, 0, avgAcceleration[2]);

                kalmanFilter.predict();

                // Incorporate gyroscope and magnetic field data
                Mat control = new Mat(3, 1, CvType.CV_32F);
                control.put(0, 0, avgGyroscope[1] * deltaTime);  // Y-axis rotation rate
                control.put(1, 0, avgGyroscope[2] * deltaTime);  // Z-axis rotation rate
                control.put(2, 0, avgMagneticField[0]);  // Magnetic field along X-axis
                kalmanFilter.set_controlMatrix(control);

                Mat estimated = kalmanFilter.correct(measurement);

                currentPosition[0] = (float) estimated.get(0, 0)[0];
                currentPosition[1] = (float) estimated.get(1, 0)[0];
                currentPosition[2] = (float) estimated.get(2, 0)[0];

                formatCurrentPosition();

                // Notify callback with updated position
                if (sensorUpdateCallback != null) {
                    sensorUpdateCallback.onSensorUpdate(currentPosition);
                }
            }
        }
    }

    private void formatCurrentPosition()
    {
        try {
            currentPosition[0] =  decimalFormat.parse(decimalFormat.format(currentPosition[0])).floatValue();
            currentPosition[1] =  decimalFormat.parse(decimalFormat.format(currentPosition[1])).floatValue();
            currentPosition[2] =  decimalFormat.parse(decimalFormat.format(currentPosition[2])).floatValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
}

