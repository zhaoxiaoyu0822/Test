package com.hat.thread;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Process;
import android.os.UserManager;
import android.util.Log;

import com.hat.listen.GasCallback;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static com.hat.util.HexUtil.hexToByteArray;

public class GasThread implements Runnable{
    private static final String TAG = GasThread.class.getSimpleName();
    private GasCallback mGasCallback;
    private int mReadTimeout = 0;
    private static final int WRITE_WAIT_MILLIS = 2000;
    private UsbSerialPort mSerialPort;
    private State mState = State.STOPPED;
    private int index=0;
    byte[] outData=null;
    private final Object mReadBufferLock = new Object();
    private static final int BUFSIZ = 4096;
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFSIZ);
    public enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public GasThread(GasCallback gasCallback, UsbSerialDriver driver, UsbDeviceConnection usbConnection) {
        mGasCallback=gasCallback;
        mSerialPort=driver.getPorts().get(2);
        try {
            mSerialPort.open(usbConnection);
            mSerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            Log.d(TAG,"connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {


        synchronized (this) {
            if (getState() != State.STOPPED) {
                throw new IllegalStateException("Already running");
            }
            mState = State.RUNNING;
        }
        try {
            while (true) {
                if (getState() != State.RUNNING) {
                    Log.i(TAG, "Stopping mState=" + getState());
                    break;
                }
                step();
            }
        } catch (Exception e) {
            Log.w(TAG, "Run ending due to exception: " + e.getMessage(), e);
            if (mGasCallback != null) {
                mGasCallback.onGasError(e);
            }
        } finally {
            synchronized (this) {
                mState = State.STOPPED;
                Log.i(TAG, "Stopped");
            }
        }
    }

    public  synchronized State getState() {
        return mState;
    }

    private synchronized void step() throws IOException {
        byte[] buffer = null;
        synchronized (mReadBufferLock) {
            buffer = mReadBuffer.array();
        }
        int len = mSerialPort.read(buffer, mReadTimeout);
        if (len > 0) {
            if (index==0){
                outData=new byte[24];
            }
            //Log.d(TAG, "Read data len=" + len);
            if (mGasCallback != null) {
                try {
                    System.arraycopy(buffer, 0, outData, index, len);
                    index=index+len;
                }catch (ArrayIndexOutOfBoundsException e){
                    System.arraycopy(buffer, 0, outData, index-1, len);
                    index=index-1+len;
                }
                if (index==24){
                    mGasCallback.onGasRecv(outData);
                    outData=null;
                    index=0;
                }
            }
        }
    }

    public synchronized void stop() {
        if (getState() == State.RUNNING) {
            Log.i(TAG, "Stop requested");
            mState = State.STOPPING;
        }
    }

    public void send(String str){
        byte[] data=hexToByteArray(str);
        try {
            mSerialPort.write(data, WRITE_WAIT_MILLIS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
