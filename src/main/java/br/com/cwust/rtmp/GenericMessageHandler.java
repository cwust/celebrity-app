package br.com.cwust.rtmp;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import br.com.cwust.rtmp.RTMPClient;
import br.com.cwust.rtmp.RTMPMessage;
import br.com.cwust.rtmp.RTMPMessageHandler;

public class GenericMessageHandler implements RTMPMessageHandler {
    public static final int MSG_HOST_ID = 20000;

    private RTMPClient client;
    private Map<Integer, Consumer<RTMPMessage>> mapHandlers;
    private String hostId;


    public GenericMessageHandler(RTMPClient client) {
        this.client = client;
        this.client.setMessageHandler(this);
        this.mapHandlers = new HashMap<>();
        setHandler(MSG_HOST_ID, this::handleMsgHostId);
    }

    protected void setHandler(int messageCode, Consumer<RTMPMessage> handler) {
        this.mapHandlers.put(messageCode, handler);
    }

    @Override
    public void handleMessage(@NonNull RTMPMessage message) {
        this.mapHandlers.get(message.getMessageCode()).accept(message);
    }

    public void setMeAsHost() {
        this.client.broadcastMessage(MSG_HOST_ID, Boolean.TRUE);
    }

    public RTMPClient getClient() {
        return this.client;
    }

    public <T extends Serializable> void broadcastMessage(int messageCode, T payload) {
        this.client.broadcastMessage(messageCode, payload);
    }

    private void handleMsgHostId(@NonNull RTMPMessage message) {
        this.hostId = message.getSenderId();
    }

    public <T extends Serializable> void sendMsgToHost(int messageCode, T payload) {
        this.client.sendMessage(messageCode, payload, this.hostId);
    }

    public boolean isHost() {
        return this.client.getMyParticipantId().equals(this.hostId);
    }
}
