package com.github.xhea1.service.image.facerecognition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.*;
import org.opencv.dnn.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceMatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FaceFeatureExtractor extractor;

    static {
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Constructs a new face matcher with the specified model path.
     *
     * @param modelPath The path to the face feature extraction model file.
     * @throws IOException If there's an issue loading the model.
     */
    public FaceMatcher(String modelPath) throws IOException {
        extractor = new FaceFeatureExtractor(modelPath);
    }

    /**
     * Method to load a pre-trained face detection model.
     *
     * This method loads the OpenCV DNN (Deep Neural Network) module, which is used for
     * facial recognition and face detection tasks. It uses a prototxt file that contains
     * information about the neural network architecture, along with a caffemodel file
     * that contains the pre-trained weights of the network.
     *
     * @return A loaded OpenCV Net object that can be used for face detection tasks
     */
    private Net loadFaceDetector() {
        // Define the paths to the prototxt and caffemodel files
        String protoPath = "path/to/deploy.prototxt";
        String modelPath = "path/to/res10_300x300_ssd_iter_140000.caffemodel";

        // Load the face detection model using OpenCV DNN module
        return Dnn.readNetFromCaffe(protoPath, modelPath);
    }

    // TODO: persist the output somewhere
    /**
     * Method to detect faces and extract feature vectors using a pre-trained feature extraction model.
     *
     * @param imagePaths list of image file paths
     * @return map where each key is an image path and the value is a list of extracted features
     */
    private Map<Path, List<float[]>> detectAndExtractFeatures(List<Path> imagePaths) {
        // Initialize an empty map to store the detected faces' features
        Map<Path, List<float[]>> imageFeatures = new HashMap<>();

        // Load the pre-trained face detection model
        Net faceNet = loadFaceDetector();

        for (Path imagePath : imagePaths) {
            // Read the image from file
            Mat image = Imgcodecs.imread(imagePath.toString());
            if (image.empty()) {
                LOGGER.error("Could not load image: {}", imagePath);
                continue;
            }

            // Preprocess the input image for face detection
            Mat inputBlob = Dnn.blobFromImage(image, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0), false, false);

            // Set the preprocessed image as the model's input
            faceNet.setInput(inputBlob);

            // Run the face detection model to detect faces in the image
            Mat detections = faceNet.forward();

            // Reshape the output of the face detection model for easier processing
            Mat detectionMat = detections.reshape(1, detections.size(2));

            // Initialize an empty list to store the detected faces' features
            List<float[]> featuresList = new ArrayList<>();

            for (int i = 0; i < detectionMat.rows(); i++) {
                double confidence = detectionMat.get(i, 2)[0];
                if (confidence > 0.5) {
                    // Extract the bounding box coordinates of the detected face
                    int x1 = (int) (detectionMat.get(i, 3)[0] * image.cols());
                    int y1 = (int) (detectionMat.get(i, 4)[0] * image.rows());
                    int x2 = (int) (detectionMat.get(i, 5)[0] * image.cols());
                    int y2 = (int) (detectionMat.get(i, 6)[0] * image.rows());

                    // Create a rectangle representing the detected face
                    Rect faceRect = new Rect(x1, y1, x2 - x1, y2 - y1);

                    // Extract the ROI of the detected face from the original image
                    Mat face = new Mat(image, faceRect);

                    // Extract features using FaceNet or similar model
                    float[] features = extractor.extractFeatures(Utils.convertToBytedecoMat(face));
                    featuresList.add(features);
                }
            }

            // Store the detected faces' features in the map
            imageFeatures.put(imagePath, featuresList);
        }

        // Return the map of detected faces' features
        return imageFeatures;
    }

    // Method to calculate Euclidean distance between two feature vectors
    private double calculateDistance(float[] feature1, float[] feature2) {
        // Initialize the sum variable for calculating the Euclidean distance
        double sum = 0.0;

        // Iterate through each dimension of the feature vectors
        for (int i = 0; i < feature1.length; i++) {
            // Calculate the squared difference between corresponding elements in the two feature vectors
            double diff = Math.pow(feature1[i] - feature2[i], 2);
            sum += diff;
        }

        // Return the square root of the sum, which is the Euclidean distance
        return Math.sqrt(sum);
    }

    // TODO: this method should load the persisted outputs instead of recalculating them everytime
    /**
     * Finds images that match a target image based on their facial features.
     *
     * @param targetImagePath the path to the target image
     * @param otherImagePaths the paths to the other images
     * @param threshold the maximum allowed distance between feature vectors for two images to be considered matching
     * @return a list of paths to the matching images
     */
    public List<Path> findMatchingImages(Path targetImagePath, List<Path> otherImagePaths, double threshold) {
        List<Path> matchingImages = new ArrayList<>();

        List<Path> allImagePaths = new ArrayList<>(otherImagePaths);
        allImagePaths.add(targetImagePath);

        Map<Path, List<float[]>> imageFeatures = detectAndExtractFeatures(allImagePaths);

        List<float[]> targetFeatures = imageFeatures.get(targetImagePath);

        for (Path imagePath : otherImagePaths) {
            List<float[]> otherFeatures = imageFeatures.computeIfAbsent(imagePath, imageFeatures::get);
            for (float[] targetFeature : targetFeatures) {
                for (float[] otherFeature : otherFeatures) {
                    double distance = calculateDistance(targetFeature, otherFeature);
                    if (distance < threshold) {
                        matchingImages.add(imagePath);
                        break;
                    }
                }
            }
        }

        return matchingImages;
    }
}

