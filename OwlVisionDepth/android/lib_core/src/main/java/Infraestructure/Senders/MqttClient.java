package Infraestructure.Senders;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttClient {
    private final String BROKER_URI = "tcp://192.168.1.100:1883";
    private final String CLIENT_ID = "AndroidClient";
    private org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

    public void connect() {
        try {
            mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(BROKER_URI, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttClient.connect(mqttConnectOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPosition(String payload, String topic)
    {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }

        try {
            mqttClient.publish(topic, payload.getBytes(), 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}