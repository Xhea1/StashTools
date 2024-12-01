package com.github.xhea1.service.image.facerecognition;


import java.nio.ByteBuffer;

public class Utils {

    public static org.bytedeco.opencv.opencv_core.Mat convertToBytedecoMat(org.opencv.core.Mat openCvMat) {
        // Create a ByteBuffer with the same size as the original Mat's total byte count
        int totalBytes = (int) (openCvMat.total() * openCvMat.elemSize());
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalBytes);

        // Copy Mat data to ByteBuffer
        openCvMat.get(0, 0, byteBuffer.array());

        // Create a new Bytedeco Mat with the same dimensions and type as the original Mat
        org.bytedeco.opencv.opencv_core.Mat bytedecoMat = new org.bytedeco.opencv.opencv_core.Mat(openCvMat.rows(), openCvMat.cols(), openCvMat.type());

        // Copy the data from ByteBuffer into the Bytedeco Mat
        bytedecoMat.data().put(byteBuffer.get());

        return bytedecoMat;
    }
}
