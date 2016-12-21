package com.zsm.pocketlamb;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest.Builder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int[] LIGHT_CODE = { (3600*1000*240)/Torch.BASE_PERIOD_FACTOR, 0 };
	private static final int[] FLASH_CODE = { 8, 8 };
	private static final int ACCESS_CAMERA_REQUEST_CODE = 1001;
	
	public CameraDevice mCameraDevice;
	public Builder mBuilder;
	public CameraCaptureSession mSession;
	private Torch mTorch;
	private TextView mTextView;
	private CheckBox mRepeatView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
		        != PackageManager.PERMISSION_GRANTED) {
		    //…Í«ÎWRITE_EXTERNAL_STORAGE»®œﬁ
		    ActivityCompat
		    	.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
		    						ACCESS_CAMERA_REQUEST_CODE);
		}
		
		try {
			mTorch
				= new Torch( this, (ImageView)findViewById( R.id.imageViewTorch ),
							 R.drawable.flashlight_on, R.drawable.flashlight_off );
		} catch (Exception e) {
			Toast
				.makeText( this, R.string.alertFlashFailed, Toast.LENGTH_LONG )
				.show();
		
			finish();
		}
	}
	
    
	@Override
	public void onRequestPermissionsResult( int requestCode, String[] permissions,
											int[] grantResults ) {
		
		if( requestCode == ACCESS_CAMERA_REQUEST_CODE ) {
			// Only one permission should be requested
			if( grantResults == null
				|| grantResults[0] != PackageManager.PERMISSION_GRANTED ) {
				
				new AlertDialog.Builder( this )
					.setMessage( R.string.accessCameraNotGranted )
					.setIcon( android.R.drawable.ic_dialog_info )
					.setPositiveButton( null, null )
					.setNegativeButton( android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					} )
					.show();
			} else {
				
			}
		}
	}


	public void onLight( View v ) {
		mTorch.start( LIGHT_CODE, true );
	}

	public void onOff( View v ) {
		mTorch.close();
	}
	
	public void onFlash( View v ) {
		mTorch.start( FLASH_CODE, true );
	}

	public void onSos( View v ) {
		mTorch.start( "SOS", true );
	}

	public void onSendMessage( View v ) {
		if( mTextView == null ) {
			mTextView = (EditText)findViewById( R.id.editText );
			mRepeatView = (CheckBox)findViewById( R.id.checkBoxRepeat );
		}
		mTorch.start( mTextView.getText().toString(), mRepeatView.isChecked() );
	}

    @Override
    protected void onDestroy() {
        mTorch.close();
        super.onDestroy();
    }
}
