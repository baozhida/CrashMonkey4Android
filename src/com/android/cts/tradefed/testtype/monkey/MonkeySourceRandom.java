package com.android.cts.tradefed.testtype.monkey;

import java.awt.Point;
import java.util.Random;

import com.android.tradefed.log.LogUtil.CLog;

public class MonkeySourceRandom implements MonkeyEventSource {
	// 系统按键
	private static final String[] SYS_KEYS = { "KEYCODE_BACK" };
	// Monkey测试中的九大参数，只用到3个
	public static final int FACTOR_TOUCH = 0;
	public static final int FACTOR_MOTION = 1;
	public static final int FACTOR_SYSOPS = 2;

	public static final int FACTORZ_COUNT = 3;
	// 点击事件的2种不同方式
	private static final int GESTURE_TAP = 0;
	private static final int GESTURE_DRAG = 1;

	private float[] mFactors = new float[FACTORZ_COUNT];
	private Random mRandom;

	private long mThrottle = 0;
	private int mVerbose = 0;
	private Rectangle mRectangle = null;

	public MonkeySourceRandom(Random random, long throttle, boolean randomizeThrottle, Rectangle rect) {
		this.mRandom = random;
		this.mThrottle = throttle;
		this.mRectangle = rect;
		// 默认的各个事件的比例
		mFactors[FACTOR_TOUCH] = 45.0f;
		mFactors[FACTOR_MOTION] = 45.0f;
		mFactors[FACTOR_SYSOPS] = 10.0f;

	}

	// 设置各个事件的百分比
	public void setFactors(float factors[]) {
		int c = FACTORZ_COUNT;
		if (factors.length < c) {
			c = factors.length;
		}
		for (int i = 0; i < c; i++)
			mFactors[i] = factors[i];
	}

	// 设置某一个事件的百分比
	public void setFactors(int index, float v) {
		mFactors[index] = v;
	}

	public MonkeyEvent generateEvents() {

		int randomNumber;
		String lastKey = "";
		randomNumber = mRandom.nextInt(100);
		//CLog.i("随机数是："+randomNumber+"、"+mFactors[FACTOR_TOUCH]+"、"+mFactors[FACTOR_MOTION]+"、"+mFactors[FACTOR_SYSOPS]);
		if (randomNumber >= 0 && randomNumber <= mFactors[FACTOR_TOUCH]) {
			CLog.i("FACTOR_TOUCH");
			return generatePointerEvent(mRandom, GESTURE_TAP);
		} else if (randomNumber > mFactors[FACTOR_TOUCH]
				&& randomNumber <= mFactors[FACTOR_TOUCH] + mFactors[FACTOR_MOTION]) {
			CLog.i("FACTOR_MOTION");
			return generatePointerEvent(mRandom, GESTURE_DRAG);
		} else if (randomNumber > mFactors[FACTOR_TOUCH] + mFactors[FACTOR_MOTION]
				&& randomNumber <= mFactors[FACTOR_TOUCH] + mFactors[FACTOR_MOTION] + mFactors[FACTOR_SYSOPS]) {
			CLog.i("FACTOR_SYSOPS");
			lastKey = SYS_KEYS[0];
		}

		// 按键
		return new MonkeyKeyEvent(lastKey);

	}

	@Override
	public MonkeyEvent getNextEvent() {
		return	generateEvents();

	}

	@Override
	public void setVerbose(int verbose) {
		// TODO Auto-generated method stub
		mVerbose = verbose;

	}

	/**
	 * Adjust the percentages (after applying user values) and then normalize to
	 * a 0..1 scale.
	 */
	private boolean adjustEventFactors() {
		// go through all values and compute totals for user & default values
		float userSum = 0.0f;
		float defaultSum = 0.0f;
		int defaultCount = 0;
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			if (mFactors[i] <= 0.0f) { // user values are zero or negative
				userSum -= mFactors[i];
			} else {
				defaultSum += mFactors[i];
				++defaultCount;
			}
		}

		// if the user request was > 100%, reject it
		if (userSum > 100.0f) {
			System.err.println("** Event weights > 100%");
			return false;
		}

		// if the user specified all of the weights, then they need to be 100%
		if (defaultCount == 0 && (userSum < 99.9f || userSum > 100.1f)) {
			System.err.println("** Event weights != 100%");
			return false;
		}

		// compute the adjustment necessary
		float defaultsTarget = (100.0f - userSum);
		float defaultsAdjustment = defaultsTarget / defaultSum;

		// fix all values, by adjusting defaults, or flipping user values back
		// to >0
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			if (mFactors[i] <= 0.0f) { // user values are zero or negative
				mFactors[i] = -mFactors[i];
			} else {
				mFactors[i] *= defaultsAdjustment;
			}
		}

		// if verbose, show factors
		if (mVerbose > 0) {
			CLog.d("// Event percentages:");
			for (int i = 0; i < FACTORZ_COUNT; ++i) {
				CLog.d("//   " + i + ": " + mFactors[i] + "%");
			}
		}

		// if (!validateKeys()) {
		// return false;
		// }

		// finally, normalize and convert to running sum
		float sum = 0.0f;
		for (int i = 0; i < FACTORZ_COUNT; ++i) {
			sum += mFactors[i] / 100.0f;
			mFactors[i] = sum;
		}
		return true;
	}

	private MonkeyEvent generatePointerEvent(Random random, int gesture) {

		Point p1 = randomPoint(random, mRectangle);
		Point v1 = randomVector(random);

		// sometimes we'll move during the touch
		// tap
		if (gesture == GESTURE_TAP) {
			return new MonkeyTapEvent(p1);
			// 滑动
		} else if (gesture == GESTURE_DRAG) {
			MonkeyMotionEvent motionEvent = new MonkeyMotionEvent();
			motionEvent.setDownPoint(p1);
			int count = random.nextInt(3);
			Point newPoint = randomWalk(random, mRectangle, p1, v1);
			for (int i = 0; i < count; i++) {
				newPoint = randomWalk(random, mRectangle, newPoint, v1);
				motionEvent.addMovePoint(newPoint);
			}
			newPoint = randomWalk(random, mRectangle, newPoint, v1);
			motionEvent.addMovePoint(newPoint);
			motionEvent.setUpPoint(newPoint);
			return motionEvent;
		}
		return null;

	}

	private Point randomPoint(Random random, Rectangle display) {
		return new Point(Math.max(random.nextInt(display.getWidth()),10), Math.max(random.nextInt(display.getHeight()),20));
	}

	private Point randomVector(Random random) {
		return new Point((int) ((random.nextFloat() - 0.5f) * 200), (int) ((random.nextFloat() - 0.5f) * 200));
	}

	private Point getDragNextPoint(Random random, Rectangle display, Point point) {
		int[] arg = { -1, 1 };
		int y = 40 * (random.nextInt(20) - 10);
		int x = (int) Math.sqrt(Math.pow(2, 4) * Math.pow(10, 4) - Math.abs(y * y));
		x = arg[random.nextInt(arg.length)] * x;
		Point newPoint = new Point();

		CLog.d(String.format("(%s,%s)", x, y));
		newPoint.x = Math.max(Math.min(point.x + x, display.getWidth()-10), display.getX()-10);
		newPoint.y = Math.max(Math.min(point.y + y, display.getHeight()-20), display.getY()-20);
		return newPoint;
	}

	private Point randomWalk(Random random, Rectangle display, Point point, Point vector) {
		Point newPoint = new Point();
		newPoint.x = Math.max(Math.min(point.x + (int) (random.nextFloat() * vector.x), display.getWidth()-10), 10);
		newPoint.y = Math.max(Math.min(point.y + (int) (random.nextFloat() * vector.y), display.getHeight()-20), 20);

		return newPoint;
	}

	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return adjustEventFactors();
	}

}
