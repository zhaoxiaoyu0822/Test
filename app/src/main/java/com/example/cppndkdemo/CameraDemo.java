package com.example.cppndkdemo;

public class CameraDemo {
    static {
        System.loadLibrary("CameraJni");
    }

    /**
     * @param inputYuv 原始图像数据yuyv
     * @param width
     * @param height
     * @return rgb
     */
    public native byte[] yuyv2Rgb(byte[] inputYuv, int width, int height);

    /**
     * @param inputYuv 原始图像数据uyvy
     * @param width
     * @param height
     * @return rgb
     */
    public native byte[] uyvy2Rgb(byte[] inputYuv, int width, int height);

    /**
     * @param inputRgb 原始图像数据rgb
     * @param width
     * @param height
     * @return rgb
     */
    public native byte[] rgb2Rgb(byte[] inputRgb, int width, int height);

    /**
     *
     * @param heartData len=76 bytes ,first byte is 0xFF
     * @return int[1]=heart rate, int[2]=blood oxygen, int[3]=Microcirculation,
     *          int[4]=Systolic blood pressure, int[5]=Diastolic systolic pressure, int[6]=random num
     */
    public native int[] heartInfo(byte[] heartData);

    /*
    [in]    yuvSrc 输入数据
    [in]    yuvW
    [in]    yuvH
    [in]    rgb 输入数据
    [in]    rgbW
    [in]    rgbH
    [out]   rgbOut  输出数据 分配大小 width*height*3，该宽高与输入的rgb宽高相同
    */
    public native byte[] SLJInfRgbMerge(byte[] yuvSrc, int yuvW, int yuvH, byte[] rgb, int rgbW, int rgbH);

    /*
    [in]    rgbInf 输入数据
    [in]    infW
    [in]    infH
    [in]    rgb 输入数据
    [in]    rgbW
    [in]    rgbH
    [out]   rgbOut  输出数据 分配大小 width*height*3，该宽高与输入的rgb宽高相同
    */
    public native byte[] SLJInfRgbMergeV1(byte[] rgbInf, int infW, int infH, byte[] rgb, int rgbW, int rgbH);

    /*
    [in]    heratData 输入数据
    [in]    length 输入数据长度 76
    [out]   dataOut  输出数据 分配大小 6
    */
    public native int[] SLJheartInfo(byte[] heratData, int length);

    /*
    [in]    inputData 输入数据
    [in]    length 输入数据长度  固定长度128字节
    */
    public native float[] SLJfourInOneGas(byte[] inputData, int length);

    /*
    [in]    inputData 输入数据
    [in]    length 输入数据长度  固定长度24字节
    [out]   dataOut  输出数据 dataOut[0]=可燃气体数据
    */
    public native float[] SLJCombustibleGas(byte[] inputData, int length);

    /*
    [in]    inputData 输入数据
    [in]    length 输入数据长度 8字节固定长度
    [out]   dataOut  输出数据 dataOut[0]=甲烷
    */
    public native float[] SLJTDLASGas(byte[] inputData, int length);

}

