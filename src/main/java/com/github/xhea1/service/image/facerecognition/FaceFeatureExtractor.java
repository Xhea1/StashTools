package com.github.xhea1.service.image.facerecognition;

import org.bytedeco.opencv.opencv_core.Mat;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Size;


import java.io.File;
import java.io.IOException;

public class FaceFeatureExtractor {

    private final ComputationGraph faceNetModel;

    public FaceFeatureExtractor(String modelPath) throws IOException {
        // Load the FaceNet model from a file
        faceNetModel = ModelSerializer.restoreComputationGraph(new File(modelPath));
    }

    public float[] extractFeatures(Mat face) {
        // Preprocess the face image
        INDArray faceImage = preprocessImage(face);

        // Pass the image through the FaceNet model to extract features
        INDArray output = faceNetModel.outputSingle(faceImage);

        // Convert INDArray to a float array
        return output.toFloatVector();
    }

    private INDArray preprocessImage(Mat face) {
        // Resize image to 160x160, which is the input size for FaceNet
        Mat resizedFace = new Mat();
        opencv_imgproc.resize(face, resizedFace, new Size(160, 160));

        // Convert the Mat image to INDArray format
        int height = resizedFace.rows();
        int width = resizedFace.cols();
        int channels = resizedFace.channels();

        INDArray inputImage = Nd4j.create(1, channels, height, width);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Access pixel values for all channels
                double[] pixel = new double[channels];
                for (int c = 0; c < channels; c++) {
                    // Extract pixel value for the specific channel c
                    pixel[c] = resizedFace.ptr(y, x).get(c) & 0xFF; // Ensures pixel values are in [0, 255] range
                    inputImage.putScalar(new int[]{0, c, y, x}, pixel[c]);
                }
            }
        }

        // Normalize the pixel values to the range [0, 1]
        ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
        scaler.transform(inputImage);

        return inputImage;
    }

}

