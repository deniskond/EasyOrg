package nosfie.easyorg.Helpers;

import android.content.Context;

public class ViewHelper {

    public static int convertDpToPixels(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
