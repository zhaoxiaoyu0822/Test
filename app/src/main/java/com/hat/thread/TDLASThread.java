package com.hat.thread;

import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import com.hat.listen.GasCallback;
import com.hat.listen.TDLASCallback;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.hat.util.HexUtil.hexToByteArray;

public class TDLASThread implements Runnable{
    private static final String TAG = TDLASThread.class.getSimpleName();
    private TDLASCallback mTDLASCallback;
    private int mReadTimeout = 0;
    private static final int WRITE_WAIT_MILLIS = 2000;
    private UsbSerialPort mSerialPort;
    private State mState = State.STOPPED;
    byte[] outData=null;
    private final Object mReadBufferLock = new Object();
    private static final int BUFSIZ = 4096;
    private ByteBuffer mReadBuffer = ByteBuffer.allocate(BUFSIZ);
    public enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public TDLASThread(TDLASCallback tdlasCallback, UsbSerialDriver driver, UsbDeviceConnection usbConnection) {
        mTDLASCallback=tdlasCallback;
        mSerialPort=driver.getPorts().get(0);
        try {
            mSerialPort.open(usbConnection);
            mSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (Exception e) {
            Log.d(TAG,"connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        send("AA13010055");
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
            if (mTDLASCallback != null) {
                mTDLASCallback.onTDLASError(e);
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
        if (len > 0&&len==8) {
            outData=new byte[8];
            //Log.d(TAG, "Read data len=" + len);
            if (mTDLASCallback != null) {
                System.arraycopy(buffer, 0, outData, 0, 8);
                mTDLASCallback.onTDLASRecv(outData);
                outData=null;
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
