package com.hat.listen;

public interface HealthCallback {

    public void onHealthRecv(byte[] data);

    public void onHealthError(Exception e);
}
