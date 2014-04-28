package com.abkibarmodisarkar.photobadge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.abkibarmodisarkar.image.crop.CropImageView;
import com.abkibarmodisarkar.image.crop.ImageUtility;

public class ImageCropActivity extends FragmentActivity {

	private CropImageView mCropImageView;
	private Bitmap mBitmap;
	private Bitmap finalBitmap;
	private Dialog mEmmaDialog;
	private View mLayoutView;

	// request code
	public static final int REQUEST_CODE_GALLERY = 111;
	public static final int REQUEST_CODE_TAKE_PICTURE = 222;
	public static final int REQUEST_CODE_CROP_IMAGE = 333;

	boolean aapCheckBox = false;
	boolean bjpCheckBox = false;
	boolean congressCheckBox = false;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= 18) {
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
		// remove title
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.cropimage);

		// initialize a view.
		initializeView();
		// show choose image view
		showChooseImagesDialog();
	}

	/**
	 * initialize a layout view..
	 */
	private void initializeView() {

		// initialize view
		mLayoutView = (View) findViewById(R.id.crop_image_layout);
		mCropImageView = (CropImageView) findViewById(R.id.crop_image_view);

		// cancel button
		Button cancelButton = (Button) findViewById(R.id.discard_button);
		// set text value
		cancelButton.setText("CANCEL");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// save button
		Button saveButton = (Button) findViewById(R.id.save_button);
		// set text value
		saveButton.setText("CREATE");
		saveButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (mBitmap != null) {
					finalBitmap = mCropImageView.getFinalBitmap(mBitmap);
					Bitmap mBitmap2 = null;
					if (aapCheckBox) {
						// mBitmap2 =
						// BitmapFactory.decodeResource(getResources(),
						// R.drawable.a);
						mBitmap2 = BitmapFactory.decodeResource(getResources(),
								R.drawable.aap_c);
					} else if (bjpCheckBox) {
						// mBitmap2 =
						// BitmapFactory.decodeResource(getResources(),
						// R.drawable.b);
						mBitmap2 = BitmapFactory.decodeResource(getResources(),
								R.drawable.bjp_c);
					} else if (congressCheckBox) {
						mBitmap2 = BitmapFactory.decodeResource(getResources(),
								R.drawable.congress_c);
						// mBitmap2 =
						// BitmapFactory.decodeResource(getResources(),
						// R.drawable.congress);
					}

					// Init our overlay bitmap
					Bitmap bmp = finalBitmap.copy(Bitmap.Config.RGB_565, true);
					// Init the canvas
					Canvas canvas = new Canvas(bmp);

					System.out.println("canvas width :" + canvas.getWidth()
							+ "canvas height :" + canvas.getHeight());
					// Draw the text on top of the canvas
					canvas.drawBitmap(mBitmap2,
							(canvas.getWidth() - mBitmap2.getWidth()) / 2,
							canvas.getHeight() - mBitmap2.getHeight() - 5, null);

					// now bmp have the two overlayed:
					mCropImageView.setImageBitmap(bmp);

					if (bmp != null) {
						saveImageToStorage(bmp);
					}
				} else {
					finish();
				}
			}
		});
	}

	private void createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("image/*");

		// For a file in shared storage. For data in private storage, use a
		// ContentProvider.
		Uri uri = Uri.fromFile(new File(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/PhotoBadge/Photo_badge.png"));
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(shareIntent, "Send to . . "));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_GALLERY:
			if (resultCode == RESULT_OK) {
				mLayoutView.setVisibility(View.VISIBLE);
				Uri imageUri = data.getData();
				if (imageUri != null) {
					startCropImage(imageUri);
				}
			} else {
				finish();
			}
			break;
		case REQUEST_CODE_TAKE_PICTURE:
			if (resultCode == RESULT_OK) {
				mLayoutView.setVisibility(View.VISIBLE);
				Uri pictureUri = data.getData();
				if (pictureUri != null) {
					startCropImage(pictureUri);
				} else {
					Bitmap bitmap = data.getParcelableExtra("data");
					if (bitmap != null) {
						Uri tempUri = getImageUri(this, bitmap);
						startCropImage(tempUri);
					}
				}
			} else {
				finish();
			}
			break;
		case REQUEST_CODE_CROP_IMAGE:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
	 * 
	 * @param inContext
	 * @param inImage
	 * @return
	 */
	private Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.PNG, 50, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(),
				inImage, "", null);
		return Uri.parse(path);
	}

	/**
	 * cropping a image..
	 * 
	 * @param pictureUri
	 */
	private void startCropImage(Uri pictureUri) {
		mBitmap = ImageUtility.getBitmap(this, pictureUri);
		mCropImageView.setImageBitmap(mBitmap);
		if (mBitmap == null) {
			finish();
			return;
		}

	}

	/**
	 * save image to external storage..
	 * 
	 * @param bmOverlay
	 */
	private void saveImageToStorage(Bitmap bmOverlay) {
		try {
			boolean b = Utility.saveImageToExternalStorage(bmOverlay,
					"Photo_badge.png");
			if (b) {
				createShareIntent();
			}
		} catch (IOException e) {
		}
	}

	/**
	 * show emma dialog for choose image from camera/Gallery
	 */
	protected void showChooseImagesDialog() {
		// get text from localized file
		String title = "";
		String galleryButtonText = "Gallery";
		String cameraButtonText = "Camera";

		mEmmaDialog = showEmmaDialog(title, "", 0, galleryButtonClickListener,
				galleryButtonText, -1, cameraButtonClickListener,
				cameraButtonText);
		// handle emma dialog back button, when back button pressed finish the
		// activity..
		mEmmaDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (mEmmaDialog.isShowing()) {
						mEmmaDialog.dismiss();
					}
					finish();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * handling the gallery button click events
	 */
	private OnClickListener galleryButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			openGallery();
			mEmmaDialog.dismiss();
		}
	};

	/**
	 * handling the camera button click events
	 */
	private OnClickListener cameraButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			openCamera();
			mEmmaDialog.dismiss();
		}
	};

	/**
	 * open a gallery when click on gallery button
	 */
	private void openGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
	}

	/**
	 * open a camera when click on camera button
	 */
	private void openCamera() {
		try {
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
		} catch (ActivityNotFoundException e) {

		}
	}

	private final Dialog showEmmaDialog(String title, String desc,
			int descTextColor, OnClickListener onPtBtnListener,
			String ptBtnText, int ptBtnResID, OnClickListener onNgBtnListener,
			String negBtnText) {

		final Dialog dialog = new Dialog(ImageCropActivity.this,
				R.style.DialogAnimationOutUpInUp);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setLayout(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

		dialog.setTitle("Create your badge . .");

		final View content = dialog.findViewById(R.id.dialog_content);

		RadioGroup radioGroup = (RadioGroup) dialog
				.findViewById(R.id.radioGroup1);

		RadioButton aapRadioButton = (RadioButton) radioGroup
				.findViewById(R.id.aap_radio0);
		aapRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				aapCheckBox = true;
				bjpCheckBox = false;
				congressCheckBox = false;
			}
		});
		RadioButton bjpRadioButton = (RadioButton) radioGroup
				.findViewById(R.id.bjp_radio1);
		bjpRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				aapCheckBox = false;
				bjpCheckBox = true;
				congressCheckBox = false;
			}
		});
		RadioButton congressRadioButton = (RadioButton) radioGroup
				.findViewById(R.id.congress_radio2);
		congressRadioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				aapCheckBox = false;
				bjpCheckBox = false;
				congressCheckBox = true;
			}
		});

		if (aapRadioButton.isChecked()) {
			aapCheckBox = true;
			bjpCheckBox = false;
			congressCheckBox = false;
		} else if (bjpRadioButton.isChecked()) {
			aapCheckBox = false;
			bjpCheckBox = true;
			congressCheckBox = false;
		} else if (congressRadioButton.isChecked()) {
			aapCheckBox = false;
			bjpCheckBox = false;
			congressCheckBox = true;
		}

		if (title != null)
			((TextView) dialog.findViewById(R.id.title)).setText(title);
		else
			dialog.findViewById(R.id.title).setVisibility(View.GONE);

		if (desc != null) {
			TextView descTextView = (TextView) dialog.findViewById(R.id.desc);
			descTextView.setText(Html.fromHtml(desc));
			if (descTextColor != -1)
				descTextView.setTextColor(descTextColor);
		}

		Button negButton = (Button) dialog.findViewById(R.id.btn_negative);
		Button posiButton = (Button) dialog.findViewById(R.id.btn_positive);

		posiButton.setOnClickListener(onPtBtnListener);

		if (onNgBtnListener == null) {
			onNgBtnListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			};
		}
		negButton.setOnClickListener(onNgBtnListener);

		if (ptBtnText != null) {
			posiButton.setText(ptBtnText);
		}

		if (negBtnText != null) {
			negButton.setText(negBtnText);
		}

		dialog.show();
		return dialog;

	}
}
