package org.tensorflow.lite.examples.detection;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class CallingClassifier {

    private static final String MODEL_NAME = "callingNew.tflite";

    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 224;
    public static final int IMG_WIDTH = 224;
    private static final int NUM_CHANNEL = 3;
    private static final int NUM_CLASSES = 1;

    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter tfLiteInterpreter;
    private final ByteBuffer imageData;
    private final float[][] result;

    public CallingClassifier(Activity activity) throws IOException {
        tfLiteInterpreter = new Interpreter(loadModelFile(activity), options);
        imageData = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        imageData.order(ByteOrder.nativeOrder());
        result = new float[1][NUM_CLASSES];
    }

    public float[] classify(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
        tfLiteInterpreter.run(imageData, result);
        Log.i(" Result Calling : " , Arrays.toString(result[0]));
        return result[0];
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap image) {
        Bitmap bitmap = Bitmap.createScaledBitmap(image, 224, 224, false);

        int[] intValues = new int[IMG_WIDTH * IMG_HEIGHT];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < IMG_WIDTH; ++i) {
            for (int j = 0; j < IMG_HEIGHT; ++j) {
                final int val = intValues[pixel++];

                imageData.putFloat((float) (((val >> 16) & 0xFF)/255.0));
                imageData.putFloat((float) (((val >> 8) & 0xFF)/255.0));
                imageData.putFloat((float) (((val) & 0xFF)/255.0));
            }

        }
    }
}
