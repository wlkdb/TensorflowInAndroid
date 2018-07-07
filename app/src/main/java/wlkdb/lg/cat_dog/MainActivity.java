package wlkdb.lg.cat_dog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Fragment lastFragment = null;
    private Fragment mPhotoFragment;
    private CameraFragment mCameraFragment;

    private FragmentManager fragmentManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            choseFragment(item);
            return true;
        }
    };

    private void choseFragment(MenuItem menuItem) {
        //开启事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        Fragment fragment = retrieveFromCache(menuItem);
        switch (menuItem.getItemId()) {
            case R.id.navigation_photo:
                if (mPhotoFragment == null) {
                    transaction.add(R.id.fragment, mPhotoFragment = new PhotoFragment());
                }
                if (mCameraFragment != null) {
//                    mCameraFragment.releaseCamera();
                    transaction.hide(mCameraFragment);
                }
                transaction.show(mPhotoFragment);
                break;
            case R.id.navigation_camera:
                if (mCameraFragment == null) {
                    transaction.add(R.id.fragment, mCameraFragment = new CameraFragment());
                }
                if (mPhotoFragment != null) {
                    transaction.hide(mPhotoFragment);
                }
                transaction.show(mCameraFragment);
//                transaction.addToBackStack(null);
//                transaction.replace(R.id.fragment, mCameraFragment);
                break;
        }
//        transaction.addToBackStack(null);
//        transaction.replace(R.id.fragment, fragment);
        transaction.commit();// 事务提交
    }

    private Fragment retrieveFromCache(MenuItem menuItem) {
//        从fragmentManager中获取已有的fragment对象
        for (Fragment backFragment : fragmentManager.getFragments()) {
            if (null != backFragment && menuItem.getClass().equals(backFragment.getClass())) {
                return backFragment;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment, mPhotoFragment = new PhotoFragment());
        transaction.add(R.id.fragment, mCameraFragment = new CameraFragment());
        transaction.commit();
//        choseFragment(R.id.navigation_photo);
    }
}
