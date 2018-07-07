package wlkdb.lg.cat_dog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by LG on 2018/4/21.
 */

public class Utils {

    public static Bitmap zoomImg(Bitmap bm, int newWidth , int newHeight){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public static void setResult(Context context, ImageView view, List<Classifier.Recognition> results) {
        if (results.size() == 0) {
            return;
        }
        Classifier.Recognition answer = null;
        for (final Classifier.Recognition recognition : results) {
            if (answer == null || recognition.getConfidence() > answer.getConfidence()) {
                answer = recognition;
            }
        }
        int resId = answer.getTitle().equals("cat") ? R.mipmap.cat : R.mipmap.dog;
        view.setImageResource(resId);
//        Drawable leftDrawable = context.getResources().getDrawable(resId);
//        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
//        textView.setCompoundDrawables(leftDrawable, null, null, null);
//        String text = "";
//        for (final Classifier.Recognition recognition : results) {
//            float confidence = (float)(Math.round(answer.getConfidence() * 100)) / 100;
//            text = text + results.size() + " " + recognition.getTitle() + " : " + confidence + "  ";
//        }
//        textView.setText(text);
    }
}
