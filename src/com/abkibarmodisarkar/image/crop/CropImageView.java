package com.abkibarmodisarkar.image.crop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

@SuppressLint("DrawAllocation")
public class CropImageView extends ImageView implements OnTouchListener {

	private Matrix mMatrix = new Matrix();
	private float mScaleFactor = .4f;
	private float mRotationDegrees = 0.f;
	private float mFocusX = 0.f;
	private float mFocusY = 0.f;
	private int mAlpha = 255;
	private int mImageHeight, mImageWidth;

	private ScaleGestureDetector mScaleDetector;
	private RotateGestureDetector mRotateDetector;
	private MoveGestureDetector mMoveDetector;
	private ShoveGestureDetector mShoveDetector;
	private FocusAreaType mFocusAreaType = FocusAreaType.CIRCLE; // default

	public enum FocusAreaType {
		CIRCLE, RECTANGLE;
	}

	public CropImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	public CropImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public CropImageView(Context context) {
		super(context);

		init();

	}

	public void setFocusAreaType(FocusAreaType focusAreaType) {
		this.mFocusAreaType = focusAreaType;
		invalidate();
	}

	public FocusAreaType getFocusAreaType() {
		return mFocusAreaType;
	}

	Paint mNoFocusPaint = new Paint();

	Path mPath = new Path();

	private int cropHeight = -1;
	private int cropWidth = -1;

	private int mViewHeight;
	private Path mFocusPath;
	private RectF mFocusRect;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		Drawable drawable = getDrawable();
		if (drawable == null || drawable.getIntrinsicWidth() == 0
				|| drawable.getIntrinsicHeight() == 0) {
			setMeasuredDimension(0, 0);
			return;
		}

		int drawableWidth = drawable.getIntrinsicWidth();
		int drawableHeight = drawable.getIntrinsicHeight();
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		mViewHeight = heightSize;
		//
		// Set view dimensions
		//
		setMeasuredDimension(widthSize, heightSize);
		mFocusX = widthSize / 2f;
		mFocusY = heightSize / 2f;

		// Determine dimensions of 'earth' image
		mImageHeight = drawableHeight;
		mImageWidth = drawableWidth;

		float scaleX = (float) widthSize / drawableWidth;
		float scaleY = (float) heightSize / drawableHeight;
		float scale = Math.min(scaleX, scaleY);

		mScaleFactor = scale;

		if (mScaleFactor < 1) {
			mScaleFactor = 1;
		}
		//
		// Center the image
		//
		float redundantYSpace = heightSize - (mScaleFactor * drawableHeight);
		float redundantXSpace = widthSize - (mScaleFactor * drawableWidth);

		mMatrix.setScale(mScaleFactor, mScaleFactor);
		mMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);

		setImageMatrix(mMatrix);

		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mNoFocusPaint.setARGB(125, 0, 0, 0);

		Rect mDrawRect2 = new Rect(0, 0, getWidth(), mViewHeight);

		// Rect mDrawRect = new Rect(0, (getHeight() / 2) - (mImageHeight / 2),
		// getWidth(), mImageHeight);

		Paint paint = new Paint();
		paint.setARGB(0, 255, 0, 0);

		// mPath.addRect(new RectF(mDrawRect2), Path.Direction.CW);
		//
		// canvas.clipPath(mPath, Region.Op.DIFFERENCE);

		canvas.drawRect(mDrawRect2, paint);
		//
		canvas.restore();

		canvas.save();

		mFocusPath = new Path();

		if (mFocusAreaType == FocusAreaType.CIRCLE) {
			cropHeight = (mViewHeight / 2);
			cropWidth = cropHeight;
			mFocusPath.addCircle(getWidth() / 2, mViewHeight / 2, cropHeight,
					Path.Direction.CW);
		} else {
			float totalMargin = ImageUtility
					.convertDpToPixel(104, getContext());
			cropHeight = (int) (mViewHeight - totalMargin);
			cropWidth = getWidth();
			mFocusRect = new RectF(0, totalMargin / 2, getWidth(), mViewHeight
					- totalMargin / 2);
			mFocusPath.addRect(mFocusRect, Direction.CW);
		}

		canvas.clipPath(mFocusPath, Region.Op.DIFFERENCE);

		canvas.drawRect(mDrawRect2, mNoFocusPaint);

		canvas.restore();

	}

	private void init() {

		setOnTouchListener(this);
		// Setup Gesture Detectors
		mScaleDetector = new ScaleGestureDetector(getContext(),
				new ScaleListener());
		mRotateDetector = new RotateGestureDetector(getContext(),
				new RotateListener());
		mMoveDetector = new MoveGestureDetector(getContext(),
				new MoveListener());
		mShoveDetector = new ShoveGestureDetector(getContext(),
				new ShoveListener());
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor(); // scale change since
														// previous event

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

			return true;
		}
	}

	private class RotateListener extends
			RotateGestureDetector.SimpleOnRotateGestureListener {
		@Override
		public boolean onRotate(RotateGestureDetector detector) {
			mRotationDegrees -= detector.getRotationDegreesDelta();
			return true;
		}
	}

	private class MoveListener extends
			MoveGestureDetector.SimpleOnMoveGestureListener {
		@Override
		public boolean onMove(MoveGestureDetector detector) {
			PointF d = detector.getFocusDelta();
			mFocusX += d.x;
			mFocusY += d.y;

			// mFocusX = detector.getFocusX();
			// mFocusY = detector.getFocusY();
			return true;
		}
	}

	private class ShoveListener extends
			ShoveGestureDetector.SimpleOnShoveGestureListener {
		@Override
		public boolean onShove(ShoveGestureDetector detector) {
			mAlpha += detector.getShovePixelsDelta();
			if (mAlpha > 255)
				mAlpha = 255;
			else if (mAlpha < 0)
				mAlpha = 0;

			return true;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		mRotateDetector.onTouchEvent(event);
		mMoveDetector.onTouchEvent(event);
		mShoveDetector.onTouchEvent(event);

		if (mScaleFactor < 1) {
			mScaleFactor = 1;
		}

		float scaledImageCenterX = (mImageWidth * mScaleFactor) / 2;
		float scaledImageCenterY = (mImageHeight * mScaleFactor) / 2;

		mMatrix.reset();
		mMatrix.postScale(mScaleFactor, mScaleFactor);
		mMatrix.postRotate(mRotationDegrees, scaledImageCenterX,
				scaledImageCenterY);
		mMatrix.postTranslate(mFocusX - scaledImageCenterX, mFocusY
				- scaledImageCenterY);

		setImageMatrix(mMatrix);

		return true;
	}

	// private Matrix finalBitmapMatrix() {
	// return mMatrix;
	// }

	public Bitmap getFinalBitmap(Bitmap orginal) {

		setDrawingCacheEnabled(true);
		buildDrawingCache(true);

		Bitmap crop = getDrawingCache(true);

		return getCroppedBitmap(crop);
	}

	public Bitmap getCroppedBitmap(Bitmap bitmap) {

		Bitmap croppedImage = Bitmap.createBitmap(cropWidth, cropHeight,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(croppedImage);

		Canvas c = new Canvas(bitmap);
		c.clipPath(mFocusPath, Region.Op.DIFFERENCE);
		c.drawColor(0xFFFFFFFF, PorterDuff.Mode.CLEAR);

		Rect srcRect = null;

		if (mFocusAreaType == FocusAreaType.CIRCLE) {
			srcRect = new Rect((bitmap.getWidth() / 2 - cropHeight),
					(bitmap.getHeight() / 2 - cropHeight), bitmap.getWidth()
							/ 2 + cropHeight,
					(bitmap.getHeight() / 2 + cropHeight));
		} else {
			srcRect = new Rect();
			mFocusRect.round(srcRect);
		}

		Rect dstRect = new Rect(0, 0, (int) cropWidth, (int) cropHeight);

		canvas.drawBitmap(bitmap, srcRect, dstRect, null);

		return croppedImage;
	}
}
