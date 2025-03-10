package com.example.project;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.example.project.Emotion;
import com.example.project.R;
import androidx.core.content.ContextCompat;
/**
 * Utility class for retrieving emotion-related data.
 * This class provides methods to get the color and icon associated with various emotions.
 */
public class EmotionData {

    /**
     * Retrieves the color associated with a given emotion.
     *
     * @param context The application context, used to access resources.
     * @param emotion The emotion for which the color is required.
     * @return The color associated with the given emotion.
     * @throws IllegalArgumentException if the emotion is unknown.
     */
    public static int getEmotionColor(Context context, Emotion emotion) {
        switch (emotion) {
            case ANGER:
                return ContextCompat.getColor(context, R.color.anger);
            case CONFUSION:
                return ContextCompat.getColor(context, R.color.confusion);
            case DISGUST:
                return ContextCompat.getColor(context, R.color.disgust);
            case FEAR:
                return ContextCompat.getColor(context, R.color.fear);
            case HAPPINESS:
                return ContextCompat.getColor(context, R.color.happiness);
            case SADNESS:
                return ContextCompat.getColor(context, R.color.sadness);
            case SHAME:
                return ContextCompat.getColor(context, R.color.shame);
            case SURPRISE:
                return ContextCompat.getColor(context, R.color.surprise);
            default:
                throw new IllegalArgumentException("Unknown emotion: " + emotion);
        }
    }

    /**
     * Retrieves the icon (Drawable) associated with a given emotion.
     *
     * @param context The application context, used to access resources.
     * @param emotion The emotion for which the icon is required.
     * @return The icon associated with the given emotion.
     * @throws IllegalArgumentException if the emotion is unknown.
     */
    public static Drawable getEmotionIcon(Context context, Emotion emotion) {
        switch (emotion) {
            case ANGER:
                return ContextCompat.getDrawable(context, R.drawable.ic_angry);
            case CONFUSION:
                return ContextCompat.getDrawable(context, R.drawable.ic_confused);
            case DISGUST:
                return ContextCompat.getDrawable(context, R.drawable.ic_disgust);
            case FEAR:
                return ContextCompat.getDrawable(context, R.drawable.ic_fear);
            case HAPPINESS:
                return ContextCompat.getDrawable(context, R.drawable.ic_happy);
            case SADNESS:
                return ContextCompat.getDrawable(context, R.drawable.ic_sad);
            case SHAME:
                return ContextCompat.getDrawable(context, R.drawable.ic_shame);
            case SURPRISE:
                return ContextCompat.getDrawable(context, R.drawable.ic_surprise);
            default:
                throw new IllegalArgumentException("Unknown emotion: " + emotion);
        }
    }
}

