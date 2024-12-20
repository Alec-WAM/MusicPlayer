package alec_wam.musicplayer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.DrawableRes;

public class ThemedDrawableUtils {

    private static final Map<String, Drawable> THEMED_DRAWABLES = new HashMap<>();

    public static Drawable getThemedIcon(Context context, @DrawableRes int drawableResId, int colorAttrId, int fallbackColor){
        String cacheKey = drawableResId + "_" + colorAttrId;

        if(THEMED_DRAWABLES.containsKey(cacheKey)){
            return THEMED_DRAWABLES.get(cacheKey);
        }

        Drawable drawable = context.getResources().getDrawable(drawableResId, context.getTheme());
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{colorAttrId});
        drawable.setTint(typedArray.getColor(0, fallbackColor));

        THEMED_DRAWABLES.put(cacheKey, drawable);

        return drawable;
    }

    public static int getThemeColor(Context context, int colorAttrId, int fallbackColor){
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{colorAttrId});
        return typedArray.getColor(0, fallbackColor);
    }

    public static void clearCache(){
        THEMED_DRAWABLES.clear();
    }


    // Convert Drawable to ByteBuffer
    public static byte[] getByteArrayFromDrawable(Drawable drawable) {
        if (drawable == null) return null;

        Bitmap bitmap = drawableToBitmap(drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Use the desired format and quality
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // Method to convert Drawable to Bitmap
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }

    // Method to convert Bitmap to ByteBuffer
    private static ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();  // Reset the buffer’s position to the beginning

        return byteBuffer;
    }
}
