package com.hat.thread;

import android.hardware.usb.UsbDeviceConnection;
import android.os.Process;
import android.util.Log;

import com.hat.listen.GasCallback;
import com.hat.listen.HealthCallback;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import static com.hat.util.HexUtil.hexToByteArray;

public class HealthThread implements Runnable{
    private static final String TAG = HealthThread.class.getSimpleName();
    private HealthCallback mHealthCallback;
    private int mReadTimeout = 0;
    private static final int WRITE_WAIT_MILLIS = 2000;
    private UsbSerialPort mSerialPort;
    private State mState = State.STOPPED;
    byte[] outData=null;
    byte[] buffer = null;
    private static final int BUFSIZ = 4096;
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFSIZ);
    public enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public HealthThread(HealthCallback healthCallback, UsbSerialDriver driver, UsbDeviceConnection usbConnection) {
        mHealthCallback=healthCallback;
        mSerialPort=driver.getPorts().get(3);
        try {
            mSerialPort.open(usbConnection);
            mSerialPort.setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            Log.d(TAG,"connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        send("8A");
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
            if (mHealthCallback != null) {
                mHealthCallback.onHealthError(e);
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

    private  int index1=0;
    private void  step() throws IOException {
        buffer = mReadBuffer.array();
        int len = mSerialPort.read(buffer, mReadTimeout);
        if (len > 0) {
            if (index1==0){
                outData=new byte[76];
            }
           // Log.d(TAG, "Read data len=" + len);
            if (mHealthCallback != null) {
                try {
                    System.arraycopy(buffer, 0, outData, index1, len);
                    index1=index1+len;
                }catch (ArrayIndexOutOfBoundsException e){
                    System.arraycopy(buffer, 0, outData, index1-1, len);
                    index1=index1-1+len;
                }
                if (index1==76){
                    mHealthCallback.onHealthRecv(outData);
                    outData=null;
                    index1=0;
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
