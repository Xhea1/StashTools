package com.github.xhea1.service.image;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.opencv.core.Rect;

import java.util.ArrayList;

public class ImageProcessor {

    private final CascadeClassifier faceCascade;

    public ImageProcessor() {
        // Load pre-trained classifier for face detection from resources
        faceCascade = new CascadeClassifier("path/to/haarcascade_frontalface_default.xml");
    }

    public Mat detectFaces(String imagePath) {
        Mat image = opencv_imgcodecs.imread(imagePath);
        RectVector faces = new RectVector();
        faceCascade.detectMultiScale(image, faces);



        return image;
    }

    // TODO: add method for finding files with the same face



    // TODO: should we persist the results somewhere?
}
