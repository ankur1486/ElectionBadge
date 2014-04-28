package com.abkibarmodisarkar.photobadge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;

public class Utility {

	public static synchronized boolean saveImageToExternalStorage(
			Bitmap bitmap, String fileName) throws IOException {
		boolean isSuccess = false;
		if (checkSDcardAvailable()) {
			FileOutputStream fileOutputStream = null;
			try {
				File externalFileStorage = Environment
						.getExternalStorageDirectory();
				File directory = new File(externalFileStorage.getAbsolutePath()
						+ "/PhotoBadge");
				directory.mkdirs();
				File imageFile = new File(directory, fileName);
				if (imageFile.exists()) {
					imageFile.delete();
					imageFile.createNewFile();
				}

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100,
						byteArrayOutputStream);
				byte[] byteArray = byteArrayOutputStream.toByteArray();
				fileOutputStream = new FileOutputStream(imageFile);
				if (fileOutputStream != null) {
					fileOutputStream.write(byteArray);
					fileOutputStream.flush();
					fileOutputStream.close();
				}
				isSuccess = true;
			} catch (FileNotFoundException e) {

				isSuccess = false;
			}
		}
		return isSuccess;
	}

	private static boolean checkSDcardAvailable() {
		boolean isSdcardAvailable = false;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			isSdcardAvailable = true;
		}
		return isSdcardAvailable;
	}
}
