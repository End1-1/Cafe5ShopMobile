package com.breezedevs.shopmobile;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class MessageMaker {

    public static final String BROADCAST_DATA = "BROADCAST_DATA";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";

    public byte[] mBuffer;
    public static int mMessageNumber = 1;
    private static int mMessageIdCounter = 1;
    public static int mMessageId = 0;
    private static short mMessageType = 0;

    public MessageMaker(short c) {
        mMessageType = c;
        mMessageId = mMessageIdCounter++;
        mBuffer = new byte[]{
                0x03, 0x04, 0x15,               //pattern 0
                0x00, 0x00, 0x00, 0x00,         //packet number 3
                0x00, 0x00, 0x00, 0x00,         //message id 7
                0x00, 0x00,                     //command 11
                0x00, 0x00, 0x00, 0x00          //data size 13
        };
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buf = bb.putInt(mMessageId).array();
        System.arraycopy(buf, 0, mBuffer, 7, 4);
        bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        buf = bb.putShort(c).array();
        System.arraycopy(buf, 0, mBuffer, 11, 2);
    }

    public static double bytesToDouble(byte[] bytes, int offcet) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, offcet, 8);
        return bb.getDouble(0);
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes);
        return bb.getInt(0);
    }

    public static int bytesToInt(byte[] bytes, int offcet) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, offcet, 4);
        return bb.getInt(0);
    }

    public static short bytesToShort(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes[0]);
        bb.put(bytes[1]);
        return bb.getShort(0);
    }

    private void correctSize(int delta) {
        byte[] buf = new byte[4];
        System.arraycopy(mBuffer, 13, buf, 0, 4);
        int newSize = bytesToInt(buf) + delta;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        buf = bb.putInt(newSize).array();
        System.arraycopy(buf, 0, mBuffer, 13, 4);
    }

    public int getMessageId() {
        byte[] bb = new byte[4];
        System.arraycopy(mBuffer, 7, bb, 0, 4);
        return bytesToInt(bb);
    }

    public short getType() {
        byte[] bb = new byte[2];
        System.arraycopy(mBuffer, 11, bb, 0, 2);
        return bytesToShort(bb);
    }

    public void putByte(byte i) {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.put(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(1);
    }

    public void putShort(short i) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putShort(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(2);
    }

    public void putInteger(int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putInt(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(4);
    }

    public void putDouble(double i) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putDouble(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(8);
    }

    public void putString(String s) {
        int strSize = s.getBytes(StandardCharsets.UTF_8).length;
        ByteBuffer bb = ByteBuffer.allocate(4 + strSize);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(s.getBytes(StandardCharsets.UTF_8).length);
        bb.put(s.getBytes(StandardCharsets.UTF_8));
        ByteBuffer out = ByteBuffer.allocate(mBuffer.length + bb.array().length);
        out.put(mBuffer);
        out.put(bb.array());
        mBuffer = out.array();
        correctSize(strSize + 4);
    }

    public static String getString(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data);
        bb.position(0);
        int sz;
        sz = bb.getInt();
        byte[] strbuf = new byte[sz];
        bb.get(strbuf, 0, sz);
        return new String(strbuf);
    }

    public static String getString(byte[] data, int offcet) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data, offcet, 4);
        bb.position(0);
        int sz;
        sz = bb.getInt();
        return new String(data, offcet + 4, sz);
    }

    public void setPacketNumber() {
        ByteBuffer msgNumberBytes = ByteBuffer.allocate(4);
        msgNumberBytes.order(ByteOrder.LITTLE_ENDIAN);
        msgNumberBytes.putInt(MessageMaker.getMessageNumber()).array();
        for (int i = 0; i < msgNumberBytes.array().length; i++) {
            mBuffer[i + 3] = msgNumberBytes.array()[i];
        }
    }

    public static int getMessageNumber() {
        return mMessageNumber++;
    }

    public int send() {
        Intent intent = new Intent(BROADCAST_DATA);
        intent.putExtra("socket", true);
        intent.putExtra("data", mBuffer);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        System.out.println(String.format("Message send: %d", mMessageType));
        return mMessageId;
    }
}
