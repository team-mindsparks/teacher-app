package com.example.mindsparks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Scalar;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
    
    private Mat cameraImage;

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
    	saveImageToDisk(cameraImage, "image", "latest", -1);    	
    	new UploadLastImage().execute();
    }
    

    
    /**
	 * Saves a Mat to the SD card application folder as a jpg.  
	 * 
	 * @param source The image to save.
	 * @param filename The name of the file to be saved.
	 * @param directoryName The directory where the 
	 * @param ctx The activity context.
	 * @param colorConversion The openCV color conversion to apply to the image. -1 will use no color conversion.
	 */
	public void saveImageToDisk(Mat source, String filename, String directoryName, int colorConversion){
	
	    Mat mat = source.clone();
	    if(colorConversion != -1)
	        Imgproc.cvtColor(mat, mat, colorConversion, 4);
	
	    Bitmap bmpOut = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
	    Utils.matToBitmap(mat, bmpOut);
	    if (bmpOut != null){
	        mat.release();
	        OutputStream fout = null;
	        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
	        String dir = root + "/mindsparks/" + directoryName;
	        String fileName = filename + ".jpg";
	        File file = new File(dir);
	        file.mkdirs();
	        file = new File(dir, fileName);
	
	        try {
	            fout = new FileOutputStream(file);
	            BufferedOutputStream bos = new BufferedOutputStream(fout);
	            bmpOut.compress(Bitmap.CompressFormat.JPEG, 100, bos);
	            bos.flush();
	            bos.close();
	            bmpOut.recycle();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }

	    }
	    bmpOut.recycle();
	    

	}
    
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //first image
        cameraImage = inputFrame.rgba();
        
        detector.detect(cameraImage, keypoints1);        
        
        Scalar kpColor = new Scalar(255,159,10);//this will be color of keypoints
        //featuredImg will be the output of first image
        Imgproc.cvtColor(cameraImage, featuredImg, Imgproc.COLOR_RGBA2RGB);
        Features2d.drawKeypoints(featuredImg, keypoints1, featuredImg , kpColor, 0);
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


class UploadLastImage extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://188.226.156.181:8080/photo");
			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();        
			multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
	        String dir = root + "/mindsparks/" + "latest";
			multipartEntity.addPart("image", new FileBody(new File(dir + "/image.jpg")));

			post.setEntity(multipartEntity.build());
			HttpResponse response;
		
			response = client.execute(post);
			HttpEntity entity = response.getEntity();
			

			entity.consumeContent();
			client.getConnectionManager().shutdown(); 
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}