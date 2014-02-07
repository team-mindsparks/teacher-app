package com.example.mindsparks;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Scalar;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String  TAG = "TREASURE_HUNTER";
	private CameraBridgeViewBase mOpenCvCameraView;
	
    private Mat featuredImg;
    private MatOfKeyPoint keypoints1;
    private FeatureDetector detector;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    
                    featuredImg = new Mat();
                    keypoints1 = new MatOfKeyPoint();
                    detector = FeatureDetector.create(FeatureDetector.ORB);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        
        
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    public void saveAndUpload(View view) {
    	
    }
    
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //first image
        Mat img1 = inputFrame.rgba();
        
        detector.detect(img1, keypoints1);
//        descriptor.compute(img1, keypoints1, descriptors1);
        
        
        Scalar kpColor = new Scalar(255,159,10);//this will be color of keypoints
        //featuredImg will be the output of first image
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2RGB);
        Features2d.drawKeypoints(img1, keypoints1, featuredImg , kpColor, 0);
        Imgproc.cvtColor(featuredImg, featuredImg, Imgproc.COLOR_RGB2RGBA);

        return featuredImg;
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}
	
}
