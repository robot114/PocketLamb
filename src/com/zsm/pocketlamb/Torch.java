package com.zsm.pocketlamb;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.Toast;

public class Torch {
	
	public static final int BASE_PERIOD_FACTOR = 200;
	
	private Context mContext;
	private CameraManager mCameraManager;
	private CameraDevice mCameraDevice;
	private Builder mBuilder;
	public CameraCaptureSession mSession;

	private MyCameraDeviceStateCallback mStateCallback;
	public boolean mRepeat;
	private boolean mCurrentLight;
	public int mIndexOfCharCode;
	private int[] mCode;
	private Timer mTimer;

	private ImageView mImageView;
	private Drawable mImageOn;
	private Drawable mImageOff;

	private Handler mImageChangeHandler;

	private boolean mIsTorchOn;

	private Runnable mChangeTorchRunner;
	
	public Torch( Context context ) throws CameraAccessException {
		mContext = context;
        mCameraManager
        	= (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //here to judge if flash is available
        CameraCharacteristics cameraCharacteristics
			= mCameraManager.getCameraCharacteristics("0");
		if( !cameraCharacteristics
				.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ) {
			
			throw new UnsupportedOperationException();
		}
		
		mStateCallback = new MyCameraDeviceStateCallback();
	}

	public Torch( Context context, ImageView imageView, int resIdOn, int resIdOff )
					throws CameraAccessException {
		
		this( context );
		mImageView = imageView;
		mImageOn = context.getResources().getDrawable( resIdOn, null );
		mImageOff = context.getResources().getDrawable( resIdOff, null );
		mImageView.setImageDrawable(mImageOff);
		mImageChangeHandler = new Handler();
	}

    private boolean openCamera() {
        try {
			mCameraManager.openCamera("0", mStateCallback, null);
			return true;
		} catch (CameraAccessException e) {
			Toast
				.makeText( mContext, R.string.alertFlashFailed, Toast.LENGTH_LONG )
				.show();
			return false;
		}
    }
    
	private class FlashTask extends TimerTask {
		@Override
		public void run() {
			if( mIndexOfCharCode >= mCode.length ) {
				if( mRepeat ) {
					innerStart();
				} else {
					stop();
				}
			} else {
				nextCodeItem();
			}
		}
	}
	
	private void nextCodeItem() {
		mTimer.schedule( new FlashTask(), ((long)mCode[mIndexOfCharCode])*BASE_PERIOD_FACTOR );
		mCurrentLight = !mCurrentLight;
		makeTorchOnOff(mCurrentLight);
		mIndexOfCharCode++;
	}
	
	private void innerStart() {
		mIndexOfCharCode = 0;
		mCurrentLight = false;
		nextCodeItem();
	}
	
	public void start( String word ) {
		start( word, false );
	}
	
	public void start( String word, boolean repeat ) {
		start( MorseCode.toMorseCode(word), repeat );
	}
	
	public void start( int[] code, boolean repeat ) {
		mCode = code;
		if( mCode == null || mCode.length == 0 ) {
			Toast
				.makeText( mContext, R.string.alertNothingToFlash, Toast.LENGTH_LONG )
				.show();
			
			return;
		}
		
		if( mTimer != null ) {
			stop();
		}
		
		mTimer = new Timer();
		mRepeat = repeat;
		
		if( mCameraDevice == null ) {
			openCamera();
		} else {
			innerStart();
		}
	}
	
    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {

        private SurfaceTexture mSurfaceTexture;
		private Surface mSurface;

		@Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            //get builder
            try {
                mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //flash on, default is on
                mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                List<Surface> list = new ArrayList<Surface>();
                mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(mCameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                mSurface = new Surface(mSurfaceTexture);
                list.add(mSurface);
                mBuilder.addTarget(mSurface);
                camera.createCaptureSession(list, new MyCameraCaptureSessionStateCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

	    private Size getSmallestSize(String cameraId) throws CameraAccessException {
	        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
	                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
	                .getOutputSizes(SurfaceTexture.class);
	        if (outputSizes == null || outputSizes.length == 0) {
	            throw new IllegalStateException(
	                    "Camera " + cameraId + "doesn't support any outputSize.");
	        }
	        Size chosen = outputSizes[0];
	        for (Size s : outputSizes) {
	            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
	                chosen = s;
	            }
	        }
	        return chosen;
	    }
	    
        @Override
        public void onDisconnected(CameraDevice camera) {
        }

        @Override
        public void onError(CameraDevice camera, int error) {
        }
    }
    
    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            innerStart();
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    }

    private void makeTorchOnOff(boolean isOn) {
        try {
            if (isOn) {
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } else {
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
		
		if( mImageView != null ) {
			mIsTorchOn = isOn;
			if( mChangeTorchRunner == null ) {
				mChangeTorchRunner = new Runnable() {
					@Override
					public void run() {
						mImageView.setImageDrawable( mIsTorchOn ? mImageOn : mImageOff );
					}
				};
			}
			mImageChangeHandler.post( mChangeTorchRunner );
		}
    }
    
  	public void stop() {
  		cancelTimer();
  		if( mSession != null ) {
  			makeTorchOnOff(false);
  		}
		mCurrentLight = false;
	}
	
    public void close() {
    	stop();
        if( mSession != null) {
	        mSession.close();
	        mSession = null;
        }
    	closeCamera();
    }

	private void cancelTimer() {
		if( mTimer != null ) {
    		mTimer.cancel();
    		mTimer = null;
    		mCurrentLight = false;
    	}
	}

	private void closeCamera() {
		if( mCameraDevice != null ) {
            mCameraDevice.close();
            mCameraDevice = null;
    	}
	}

}
