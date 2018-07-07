package wlkdb.lg.cat_dog;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by LG on 2018/4/21.
 */

public class PhotoFragment extends Fragment {
    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    //所需权限

    private ImageView resultsView;
    private ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_photo, container, false);

        resultsView = view.findViewById(R.id.results);
        imageView = view.findViewById(R.id.image);
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoFragment.this.openPhotos(view);
            }
        });


        return view;
    }

    public void openPhotos(View v) {
        //调用相册
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            showImage(imagePath);
            c.close();
        }
    }

    //加载图片
    private void showImage(String imagePath){
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bm);
        int INPUT_SIZE = TensorFlowImageClassifier.INPUT_SIZE;
        Bitmap bitmap = Utils.zoomImg(bm, INPUT_SIZE, INPUT_SIZE);
        renderResult(bitmap);
    }

    protected void renderResult(Bitmap bitmap) {
//        final long startTime = SystemClock.uptimeMillis();
        Classifier classifier = TensorFlowImageClassifier.create(getActivity());
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        Utils.setResult(getActivity(), resultsView, results);
    }
}
