package com.jegarn.mqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initConnect();
        initSslConnect();
    }

    protected void initConnect() {
        System.out.println("reach initConnect");
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://jegarn.com:1883", "my_client_id");
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                } else {
                    System.out.println("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("IMqttDeliveryToken " + token.toString());
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to connect to server ");
                    exception.printStackTrace();
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    protected void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe("hello", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscribed!");
                    publishMessage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe");
                }
            });
        } catch (MqttException ex) {
            System.out.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    protected void publishMessage() {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload("world!".getBytes());
            mqttAndroidClient.publish("hello", message);
            System.out.println("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                System.out.println(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.out.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void initSslConnect() {
        System.out.println("reach initSslConnect");
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "ssl://jegarn.com:8883", "my_client_id_ssl");
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                } else {
                    System.out.println("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("IMqttDeliveryToken " + token.toString());
            }
        });

        InputStream certificates = getResources().openRawResource(R.raw.server);
        InputStream pkcs12File = getResources().openRawResource(R.raw.client);
        String password = "111111";
        InputStream bksFile = HttpsUtils.pkcs12ToBks(pkcs12File, password);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(new InputStream[]{certificates}, bksFile, password);
        if (sslParams.sSLSocketFactory == null) {
            System.out.println("SSLSocketFactory is null");
        }

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setSocketFactory(sslParams.sSLSocketFactory);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to connect to server ");
                    exception.printStackTrace();
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }
}
