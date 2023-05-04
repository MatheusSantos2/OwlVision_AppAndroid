package Infraestructure.Senders;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSender {
    private final String BROKER_URI = "tcp://192.168.1.100:1883";
    private final String CLIENT_ID = "AndroidClient";
    private MqttClient mqttClient;

    public void connect() {
        try {
            mqttClient = new MqttClient(BROKER_URI, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttClient.connect(mqttConnectOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPosition(int x, int y) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }

        String topic = "myTopic";
        String payload = x + "," + y;
        try {
            mqttClient.publish(topic, payload.getBytes(), 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPosition(String[] stringPayload)
    {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }

        String topic = "myTopic";
        String payload = String.join(" ", stringPayload);

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