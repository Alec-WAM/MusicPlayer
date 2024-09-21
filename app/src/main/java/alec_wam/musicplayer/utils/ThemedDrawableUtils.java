package alec_wam.musicplayer.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

import alec_wam.musicplayer.R;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StyleableRes;

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
}
