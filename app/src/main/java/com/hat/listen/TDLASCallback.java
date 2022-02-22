package com.hat.listen;

public interface TDLASCallback {

    public void onTDLASRecv(byte[] data);

    public void onTDLASError(Exception e);
}
