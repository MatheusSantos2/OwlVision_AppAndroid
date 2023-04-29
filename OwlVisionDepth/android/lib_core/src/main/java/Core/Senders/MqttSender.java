package Core.Senders;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSender {
    private final String BROKER_URI = "tcp://192.168.1.100:1883"; // URI do broker MQTT no ESP32
    private final String CLIENT_ID = "AndroidClient"; // Identificador do cliente MQTT
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

        String topic = "myTopic"; // Tópico MQTT a ser usado para enviar as coordenadas
        String payload = x + "," + y; // Coordenadas a serem enviadas
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