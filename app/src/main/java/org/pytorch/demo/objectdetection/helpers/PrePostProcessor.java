package org.pytorch.demo.objectdetection.helpers;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.pytorch.demo.objectdetection.MainActivity;
import org.pytorch.demo.objectdetection.detection.ObjectDetectionActivity;
import org.pytorch.demo.objectdetection.models.GarbagePoint;
import org.pytorch.demo.objectdetection.models.Result;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class PrePostProcessor {
    // for yolov5 model, no need to apply MEAN and STD
    public static float[] NO_MEAN_RGB = new float[]{0.0f, 0.0f, 0.0f};
    public static float[] NO_STD_RGB = new float[]{1.0f, 1.0f, 1.0f};

    // model input image size
    public static int mInputWidth = 640;
    public static int mInputHeight = 640;

    // model output is of size 25200*(num_of_class+5)
    public static int mOutputRow = 25200; // as decided by the YOLOv5 model for input image of size 640*640
    public static int mOutputColumn = 6; // left, top, right, bottom, score and 80 class probability
    public static float mThreshold = 0.60f; // score above which a detection is generated
    public static int mNmsLimit = 15;

    public static String[] mClasses;

    // The two methods nonMaxSuppression and IOU below are ported from https://github.com/hollance/YOLO-CoreML-MPSNNGraph/blob/master/Common/Helpers.swift

    /**
     Removes bounding boxes that overlap too much with other boxes that have
     a higher score.
     - Parameters:
     - boxes: an array of bounding boxes and their scores
     - limit: the maximum number of boxes that will be selected
     - threshold: used to decide whether boxes overlap too much
     */
    public static ArrayList<Result> nonMaxSuppression(ArrayList<Result> boxes, int limit, float threshold) {

        // Do an argsort on the confidence scores, from high to low.
        boxes.sort(Comparator.comparing(o -> o.score));

        ArrayList<Result> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        boolean done = false;
        for (int i = 0; i < boxes.size() && !done; i++) {
            if (active[i]) {
                Result boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit) break;

                for (int j = i + 1; j < boxes.size(); j++) {
                    if (active[j]) {
                        Result boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }

    /**
     Computes intersection-over-union overlap between two bounding boxes.
     */
    static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }

    @SuppressLint("MissingPermission")
    public static ArrayList<Result> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY, float ivScaleX, float ivScaleY, float startX, float startY, Bitmap image, Location location) {
        ArrayList<Result> results = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://garbagemap-5a1e8.appspot.com");
        StorageReference storageRef = storage.getReference().child("garbage_images/" + new Date() + ".jpg");

        for (int i = 0; i < mOutputRow; i++) {
            if (outputs[i * mOutputColumn + 4] > mThreshold) {
                float x = outputs[i * mOutputColumn];
                float y = outputs[i * mOutputColumn + 1];
                float w = outputs[i * mOutputColumn + 2];
                float h = outputs[i * mOutputColumn + 3];

                float left = imgScaleX * (x - w / 2);
                float top = imgScaleY * (y - h / 2);
                float right = imgScaleX * (x + w / 2);
                float bottom = imgScaleY * (y + h / 2);

                float max = outputs[i * mOutputColumn + 5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn - 5; j++) {
                    if (outputs[i * mOutputColumn + 5 + j] > max) {
                        max = outputs[i * mOutputColumn + 5 + j];
                        cls = j;
                    }
                }

                Rect rect = new Rect((int) (startX + ivScaleX * left), (int) (startY + top * ivScaleY), (int) (startX + ivScaleX * right), (int) (startY + ivScaleY * bottom));
                Result result = new Result(cls, outputs[i * mOutputColumn + 4], rect);
                results.add(result);
            }
        }

        if (results.size() > 0) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.continueWithTask(task -> storageRef.getDownloadUrl()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        GarbagePoint garbagePoint = new GarbagePoint(
                                "",
                                MainActivity.drone.getIdDrone(),
                                downloadUri.toString(),
                                new GeoPoint(ObjectDetectionActivity.latitude, ObjectDetectionActivity.longitude),
                                new Date()
                                );
                        db.collection("/GarbagePoint").add(garbagePoint.toMap());
                    }
                });



        }

        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }
}
