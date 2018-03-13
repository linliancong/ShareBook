package com.develop.zxing.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";

	private static final int MIN_PREVIEW_PIXELS = 480 * 320;
	private static final double MAX_ASPECT_DISTORTION = 0.15;

	private final Context context;

	// 屏幕分辨率
	private Point screenResolution;
	// 相机分辨率
	private Point cameraResolution;
	// 屏幕分辨率
	private Camera.Size mScreenResolution;
	// 相机分辨率
	private Camera.Size mCameraResolution;

	public CameraConfigurationManager(Context context) {
		this.context = context;
	}

	public void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();
		theScreenResolution = getDisplaySize(display);

		screenResolution = theScreenResolution;
		Log.i(TAG, "Screen resolution: " + screenResolution);

		// 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的
		Point screenResolutionForCamera = new Point();
		screenResolutionForCamera.x = screenResolution.x;
		screenResolutionForCamera.y = screenResolution.y;

		if (screenResolution.x < screenResolution.y) {
			screenResolutionForCamera.x = screenResolution.y;
			screenResolutionForCamera.y = screenResolution.x;
		}

		cameraResolution = findBestPreviewSizeValue(parameters, screenResolutionForCamera);
		Log.i(TAG, "Camera resolution x: " + cameraResolution.x);
		Log.i(TAG, "Camera resolution y: " + cameraResolution.y);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Point getDisplaySize(final Display display) {
		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (NoSuchMethodError ignore) {
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		return point;
	}

	public void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

		if (safeMode) {
			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
		}

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}

		/** 设置相机预览为竖屏 */
		camera.setDisplayOrientation(90);
	}

	public Point getCameraResolution() {
		return cameraResolution;
	}

	public Point getScreenResolution() {
		return screenResolution;
	}

	/**
	 * 从相机支持的分辨率中计算出最适合的预览界面尺寸
	 * 
	 * @param parameters
	 * @param screenResolution
	 * @return
	 */
	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {
		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Log.w(TAG, "Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});

		if (Log.isLoggable(TAG, Log.INFO)) {
			StringBuilder previewSizesString = new StringBuilder();
			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
				previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height).append(' ');
			}
			Log.i(TAG, "Supported preview sizes: " + previewSizesString);
		}

		double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;

		// Remove sizes that are unsuitable
		Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
		while (it.hasNext()) {
			Camera.Size supportedPreviewSize = it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;

			double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
			double distortion = Math.abs(aspectRatio - screenAspectRatio);
			if (distortion > MAX_ASPECT_DISTORTION) {
				it.remove();
				continue;
			}

			if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);
				Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
				return exactPoint;
			}
		}

		// If no exact match, use largest preview size. This was not a great
		// idea on older devices because
		// of the additional computation needed. We're likely to get here on
		// newer Android 4+ devices, where
		// the CPU is much more powerful.
		if (!supportedPreviewSizes.isEmpty()) {
			Camera.Size largestPreview = supportedPreviewSizes.get(0);
			Point largestSize = new Point(largestPreview.width, largestPreview.height);
			Log.i(TAG, "Using largest suitable preview size: " + largestSize);
			return largestSize;
		}

		// If there is nothing at all suitable, return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
		Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);

		return defaultSize;
	}

	/**
	 * 预览尺寸与给定的宽高尺寸比较器。首先比较宽高的比例，在宽高比相同的情况下，根据宽和高的最小差进行比较。
	 */
	private static class SizeComparator implements Comparator<Camera.Size> {
		private final int width;
		private final int height;
		private final float ratio;

		SizeComparator(int width, int height) {
			if (width < height) {
				this.width = height;
				this.height = width;
			} else {
				this.width = width;
				this.height = height;
			}
			this.ratio = (float) this.height / this.width;
		}

		@Override
		public int compare(Camera.Size size1, Camera.Size size2) {
			int width1 = size1.width;
			int height1 = size1.height;
			int width2 = size2.width;
			int height2 = size2.height;
			float ratio1 = Math.abs((float) height1 / width1 - ratio);
			float ratio2 = Math.abs((float) height2 / width2 - ratio);
			int result = Float.compare(ratio1, ratio2);
			if (result != 0) {
				return result;
			} else {
				int minGap1 = Math.abs(width - width1) + Math.abs(height - height1);
				int minGap2 = Math.abs(width - width2) + Math.abs(height - height2);
				return minGap1 - minGap2;
			}
		}

	}
	/**
	 * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
	 *
	 * @param surfaceWidth  需要被进行对比的原宽
	 * @param surfaceHeight 需要被进行对比的原高
	 * @param preSizeList   需要对比的预览尺寸列表
	 * @return 得到与原宽高比例最接近的尺寸
	 */
	protected Camera.Size findCloselySize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
		Collections.sort(preSizeList, new SizeComparator(surfaceWidth, surfaceHeight));
		return preSizeList.get(0);
	}
}
