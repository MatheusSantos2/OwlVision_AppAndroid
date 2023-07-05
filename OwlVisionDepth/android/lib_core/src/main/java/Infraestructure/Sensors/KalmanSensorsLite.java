package Infraestructure.Sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.video.KalmanFilter;

import java.text.DecimalFormat;
import java.text.ParseException;

public class KalmanSensorsLite implements SensorEventListener {
    private SensorManager sensorManager;

    private KalmanFilter kalmanFilter;
    private Mat state;
    private Mat stateCovariance;

    private boolean isFirstMeasurement = true;
    private long previousTimestamp;
    private static final int SENSOR_BUFFER_SIZE = 30;

    private float[] estimatedPosition = new float[3];
    private float[][] accelerometerBuffer = new float[3][SENSOR_BUFFER_SIZE];
    private int bufferIndex = 0;


    DecimalFormat decimalFormat = new DecimalFormat("#.####");

    public KalmanSensorsLite(SensorManager sensorManager) {
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
        int stateSize = 3;
        int measurementSize = 3;
        kalmanFilter = new KalmanFilter(stateSize, measurementSize, CvType.CV_32F);

        state = new Mat(stateSize, 1, CvType.CV_32F);
        stateCovariance = new Mat(stateSize, stateSize, CvType.CV_32F);

        state.setTo(new Scalar(0));
        stateCovariance.setTo(new Scalar(1));

        Mat transitionMatrix = Mat.eye(stateSize, stateSize, CvType.CV_32F);
        Mat measurementMatrix = Mat.eye(measurementSize, stateSize, CvType.CV_32F);
        kalmanFilter.set_transitionMatrix(transitionMatrix);
        kalmanFilter.set_measurementMatrix(measurementMatrix);
    }

    public void register() {
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerometerBuffer[0][bufferIndex] = event.values[0];
            accelerometerBuffer[1][bufferIndex] = event.values[1];
            accelerometerBuffer[2][bufferIndex] = event.values[2];
            processSensorData(accelerometerBuffer, event.timestamp);
        }
    }

    private void processSensorData(float[][] sensorData, long timestamp) {

        bufferIndex++;

        if (bufferIndex >= SENSOR_BUFFER_SIZE) {
            bufferIndex = 0;

            float[] avgAcceleration = calculateMovingAverage(sensorData, 5);

            Mat measurement = new Mat(avgAcceleration.length, 1, CvType.CV_32F);
            measurement.put(0, 0, avgAcceleration);

            if (isFirstMeasurement) {
                kalmanFilter.set_statePre(measurement);
                isFirstMeasurement = false;
                previousTimestamp = timestamp;
                return;
            }
            float timeInterval = (timestamp - previousTimestamp) / 1e9f;

            kalmanFilter.get_transitionMatrix().put(0, 3, timeInterval);
            kalmanFilter.get_transitionMatrix().put(1, 4, timeInterval);
            kalmanFilter.get_transitionMatrix().put(2, 5, timeInterval);

            Mat predictedState = kalmanFilter.predict();
            Mat correctedState = kalmanFilter.correct(measurement);

            estimatedPosition[0] = (float) correctedState.get(0, 0)[0];
            estimatedPosition[1] = (float) correctedState.get(1, 0)[0];
            estimatedPosition[2] = (float) correctedState.get(2, 0)[0];

            previousTimestamp = timestamp;

            formatCurrentPosition();

            if (sensorUpdateCallback != null) {
                sensorUpdateCallback.onSensorUpdate(estimatedPosition);
            }
        }


    }

    private void formatCurrentPosition()
    {
        try {
            estimatedPosition[0] =  decimalFormat.parse(decimalFormat.format(estimatedPosition[0])).floatValue();
            estimatedPosition[1] =  decimalFormat.parse(decimalFormat.format(estimatedPosition[1])).floatValue();
            estimatedPosition[2] =  decimalFormat.parse(decimalFormat.format(estimatedPosition[2])).floatValue();
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

    private float[] calculateMovingAverage(float[][] buffer, int windowSize) {
        float[] movingAverage = new float[3];
        int startIndex = bufferIndex - windowSize;
        if (startIndex < 0) {
            startIndex += SENSOR_BUFFER_SIZE;
        }

        for (int i = 0; i < 3; i++) {
            float sum = 0;
            int count = 0;
            int index = startIndex;
            while (count < windowSize) {
                sum += buffer[i][index];
                index = (index + 1) % SENSOR_BUFFER_SIZE;
                count++;
            }
            movingAverage[i] = sum / windowSize;
        }

        return movingAverage;
    }
}


