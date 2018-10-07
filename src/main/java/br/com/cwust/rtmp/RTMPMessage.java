package br.com.cwust.rtmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RTMPMessage implements Serializable {
    private int messageCode;
    private Serializable payload;
    private transient String senderId;

    public RTMPMessage(int messageCode, Serializable payload) {
        this.messageCode = messageCode;
        this.payload = payload;
    }
    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public Serializable getPayload() {
        return payload;
    }

    public <T extends Serializable> T getValue() {
        return (T) payload;
    }


    public void setPayload(Serializable payload) {
        this.payload = payload;
    }

    public byte[] toBytes() throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    public static RTMPMessage fromBytes(byte[] bytes) throws IOException, ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);
            return (RTMPMessage) in.readObject();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Override
    public String toString() {
        return "RTMPMessage{" +
                "messageCode=" + messageCode +
                ", payload=" + payload +
                ", senderId='" + senderId + '\'' +
                '}';
    }
}
