package com.abkibarmodisarkar.image.crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

public class ImageUtility {

	public static float convertPixelsToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / 160f);
		return dp;
	}

	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	public static float convertSpToPixel(float sp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = sp * (metrics.scaledDensity);
		return px;
	}

	/**
	 * To get a bitmap after compress if the image is too big.
	 * 
	 * Note : For taking image from SD card Use this method, If the image is
	 * Cropped by application use Utility.getImageBitmap(context, filename);
	 * 
	 * @param context
	 * @param path
	 * @return
	 */
	public static Bitmap getBitmap(Context context, Uri uri) {

		final int IMAGE_MAX_SIZE = 1024;

		// Uri uri = getImageUri(path);
		InputStream in = null;
		try {
			in = context.getContentResolver().openInputStream(uri);

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(
						2,
						(int) Math.round(Math.log(IMAGE_MAX_SIZE
								/ (double) Math.max(o.outHeight, o.outWidth))
								/ Math.log(0.5)));
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			in = context.getContentResolver().openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();

			return b;
		} catch (FileNotFoundException e) {
			// Log.e(TAG, "file " + path + " not found");
		} catch (IOException e) {
			// Log.e(TAG, "file " + path + " not found");
		}
		return null;
	}

	/**
	 * To get the Uri using file path.
	 * 
	 * @param path
	 * @return
	 */
	public static Uri getImageUri(String path) {

		return Uri.fromFile(new File(path));
	}

	/**
	 * To get Bitmap from a file.
	 * 
	 * @param context
	 * @param name
	 *            -> file absolute path.
	 * @return
	 */
	public Bitmap getImageBitmap(Context context, String fileName) {
		try {
			FileInputStream fis = context.openFileInput(fileName);
			Bitmap b = BitmapFactory.decodeStream(fis);
			fis.close();
			return b;
		} catch (Exception e) {
		}
		return null;
	}

}
