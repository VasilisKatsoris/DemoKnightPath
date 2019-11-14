package demo.knight.utils;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils {

    public static int dpToPx(int dp) {
        return Math.round((float)dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void doOnceOnGlobalLayoutOfView(final View v, final Runnable r){
        if(r!=null && v!=null) {
            v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    r.run();
                }
            });
        }
    }
}
