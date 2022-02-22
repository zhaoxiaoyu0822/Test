package com.hat.listen;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

public interface GasCallback {

    public void onGasRecv(byte[] data);

    public void onGasError(Exception e);
}
