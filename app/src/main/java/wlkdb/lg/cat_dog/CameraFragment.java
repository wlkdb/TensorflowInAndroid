package wlkdb.lg.cat_dog;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by LG on 2018/4/21.
 */

public class CameraFragment extends Fragment {

    private boolean active = false;
    private Bitmap bitmap = null;

    private SurfaceView surfaceView;
    private ImageView resultsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        surfaceView = view.findViewById(R.id.surfaceView);
        resultsView = view.findViewById(R.id.results);

        surfaceView.getHolder().addCallback(surfaceCallback);
        return view;
    }

    @Override
    public void onResume() {
         super.onResume();
         onHiddenChanged(false);
    }

    @Override
    public void onPause() {
        onHiddenChanged(true);
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            // 不在最前端界面显示
            releaseCamera();
        } else {
            // 重新显示到最前端中
            if (camera == null) {
                openCamera();
                surfaceView.getHolder().addCallback(surfaceCallback);
            }
            active = true;
        }
    }

    public void openCamera() {
        try {
            camera = Camera.open();
            WindowManager wm = (WindowManager) CameraFragment.this.getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Camera.Parameters parameters = camera.getParameters();
//                //设置预览照片的大小
//                parameters.setPreviewSize(display.getWidth(), display.getHeight());
//                //设置每秒3帧
//                parameters.setPreviewFrameRate(3);
            parameters.setPictureFormat(PixelFormat.JPEG);
            //设置照片的质量
            parameters.setJpegQuality(85);
//                parameters.setPictureSize(800, 600);
            camera.setParameters(parameters);
            //通过SurfaceView显示取景画面
            camera.setPreviewDisplay(surfaceView.getHolder());
            followScreenOrientation(camera);
            camera.startPreview();
            isPreview = true;
            camera.setPreviewCallback(previewCallback);
        } catch (IOException e) {

        }
        handler.post(renderResult);
    }

    public void releaseCamera() {
        active = false;
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        bitmap = null;
    }

    private Camera camera;
    private boolean isPreview;

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            openCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if(success){
                        focus();//实现相机的参数初始化
                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                    }
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if(camera != null) {
                if (isPreview) {//如果正在预览
                    camera.stopPreview();
                    camera.release();
                }
            }
        }
    };

    private void focus()
    {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight()); // 部分定制手机，无法正常识别该方法。
//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        followScreenOrientation(camera);
        camera.setParameters(parameters);
        camera.startPreview();
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    long lastPreviewTime = 0;

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (System.currentTimeMillis() - lastPreviewTime < 1000) {
                return;
            }
            lastPreviewTime = System.currentTimeMillis();

            camera.setPreviewCallback(null);

            if (camera == null || !active)
                return;

            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();
            final Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            int INPUT_SIZE = TensorFlowImageClassifier.INPUT_SIZE;
            bitmap = Utils.zoomImg(bm, INPUT_SIZE, INPUT_SIZE);

            camera.setPreviewCallback(this);
        }
    };

    public void followScreenOrientation(Camera camera){
        final int orientation = getActivity().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(180);
        }else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            camera.setDisplayOrientation(90);
        }
    }

    private Handler handler = new Handler();

    private Runnable renderResult = new Runnable() {
        @Override
        public void run() {
//            if (getActivity() == null) {
//                return;
//            }
            if (bitmap != null && getActivity() != null) {
                Classifier classifier = TensorFlowImageClassifier.create(getActivity());
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                Utils.setResult(getActivity(), resultsView, results);
            }
            if (active) {
                handler.post(this);
            }
        }
    };
}