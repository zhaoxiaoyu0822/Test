package com.hat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cppndkdemo.CameraDemo;
import com.hat.listen.GasCallback;
import com.hat.listen.HealthCallback;
import com.hat.listen.TDLASCallback;
import com.hat.thread.GasThread;
import com.hat.thread.HealthThread;
import com.hat.thread.TDLASThread;
import com.hat.util.HexUtil;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GasCallback, HealthCallback, TDLASCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

    private GasThread gasThread;
    private HealthThread healthThread;
    private TDLASThread tdlasThread;

    private CameraDemo jniUtil=new CameraDemo();

    private enum UsbPermission {Unknown, Requested, Granted, Denied}

    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private UsbPermission usbPermission = UsbPermission.Unknown;

    private TextView tv_jiawan,tv_keran,tv_yangqi,tv_yiyang,tv_h2s,tv_tvoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        tv_jiawan=findViewById(R.id.tv_jiawan);
        tv_keran=findViewById(R.id.tv_keran);
        tv_yangqi.findViewById(R.id.tv_yangqi);
        tv_yiyang.findViewById(R.id.tv_yiyang);
        tv_h2s.findViewById(R.id.tv_h2s);
        tv_tvoc.findViewById(R.id.tv_tvoc);
    }

    private void initGasDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = null;
        for (UsbDevice d : usbManager.getDeviceList().values()) {
            device = d;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                Log.i(TAG, "connection failed: permission denied");
            else
                Log.i(TAG, "connection failed: open failed");
            return;
        }
        gasThread = new GasThread(this, driver, usbConnection);
        fixedThreadPool.execute(gasThread);


    }

    private void initHealthDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = null;
        for (UsbDevice d : usbManager.getDeviceList().values()) {
            device = d;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                Log.i(TAG, "connection failed: permission denied");
            else
                Log.i(TAG, "connection failed: open failed");
            return;
        }
        healthThread = new HealthThread(this, driver, usbConnection);
        fixedThreadPool.execute(healthThread);


    }

    private void initTDLASDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = null;
        for (UsbDevice d : usbManager.getDeviceList().values()) {
            device = d;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                Log.i(TAG, "connection failed: permission denied");
            else
                Log.i(TAG, "connection failed: open failed");
            return;
        }
        tdlasThread = new TDLASThread(this, driver, usbConnection);
        fixedThreadPool.execute(tdlasThread);
    }

    @Override
    public void onGasRecv(byte[] data) {
        Log.d(TAG + "GAS", jniUtil.SLJCombustibleGas(data,data.length)+"");
    }

    @Override
    public void onGasError(Exception e) {

    }

    @Override
    public void onHealthRecv(byte[] data) {
        Log.d(TAG + "HEALTH", jniUtil.SLJheartInfo(data,data.length).length+"");
    }

    @Override
    public void onHealthError(Exception e) {

    }

    @Override
    public void onTDLASRecv(byte[] data) {
       // Log.d(TAG + "TDLAS", HexUtil.byte2Hex(data));
        Log.d(TAG + "HEALTH", jniUtil.SLJheartInfo(data,data.length).length+"");
    }

    @Override
    public void onTDLASError(Exception e) {

    }

    @Override
    public void onResume() {
        super.onResume();
        initHealthDevice();
        initGasDevice();
        initTDLASDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fixedThreadPool.shutdown();
    }
}