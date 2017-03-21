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
	private int mEventCount = 0;
	private Random mRandom;

	private long mThrottle = 0;
	private MonkeyEventQueue mQ;
	private int mVerbose = 0;
	private Rectangle mRectangle = null;

	public MonkeySourceRandom(Random random, long throttle,
			boolean randomizeThrottle, Rectangle rect) {
		this.mRandom = random;
		this.mThrottle = throttle;
		this.mRectangle = rect;
		// 默认的各个事件的比例
		mFactors[FACTOR_TOUCH] = 65.0f;
		mFactors[FACTOR_MOTION] = 30.0f;
		mFactors[FACTOR_SYSOPS] = 5.0f;

		mQ = new MonkeyEventQueue(random, throttle, randomizeThrottle);
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

	private void generateEvents() {
		//float cls = mRandom.nextFloat();
		int cls = mRandom.nextInt(FACTORZ_COUNT);
		String lastKey = "";

		if (cls == FACTOR_TOUCH) {// 0
			CLog.d("FACTOR_TOUCH");
			generatePointerEvent(mRandom, GESTURE_TAP);
			return;
		} else if (cls == FACTOR_MOTION) {// 1
			CLog.d("FACTOR_MOTION");
			generatePointerEvent(mRandom, GESTURE_DRAG);
			return;
		} else if (cls == FACTOR_SYSOPS) {// 2
			CLog.d("FACTOR_SYSOPS");
			lastKey = SYS_KEYS[0];
		}

		
		// 按键
		MonkeyKeyEvent e = new MonkeyKeyEvent(lastKey);
		mQ.addLast(e);

	}

	@Override
	public MonkeyEvent getNextEvent() {
		if (mQ.isEmpty()) {
			generateEvents();
		}
		mEventCount++;
		MonkeyEvent e = mQ.getFirst();
		mQ.removeFirst();
		return e;
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

	private void generatePointerEvent(Random random, int gesture) {

		Point p1 = randomPoint(random, mRectangle);
		Point v1 = randomVector(random);

		// sometimes we'll move during the touch
		// 拖拽动作
		if (gesture == GESTURE_TAP) {
			mQ.addLast(new MonkeyTapEvent(p1));
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
			mQ.addLast(motionEvent);
			// 点击动作
		}

	}

	private Point randomPoint(Random random, Rectangle display) {
		return new Point(random.nextInt(display.getWidth()),
				random.nextInt(display.getHeight()));
	}

	private Point randomVector(Random random) {
		return new Point((int) ((random.nextFloat() - 0.5f) * 200),
				(int) ((random.nextFloat() - 0.5f) * 200));
	}

	private Point getDragNextPoint(Random random, Rectangle display, Point point) {
		int[] arg = { -1, 1 };
		int y = 40 * (random.nextInt(20) - 10);
		int x = (int) Math.sqrt(Math.pow(2, 4) * Math.pow(10, 4)
				- Math.abs(y * y));
		x = arg[random.nextInt(arg.length)] * x;
		Point newPoint = new Point();

		CLog.d(String.format("(%s,%s)", x, y));
		newPoint.x = Math.max(Math.min(point.x + x, display.getWidth()), display.getX());
		newPoint.y = Math.max(Math.min(point.y + y, display.getHeight()), display.getY());
		return newPoint;
	}

	private Point randomWalk(Random random, Rectangle display, Point point,
			Point vector) {
		Point newPoint = new Point();
		newPoint.x = Math
				.max(Math.min(point.x + (int) (random.nextFloat() * vector.x),
						display.getWidth()), 0);
		newPoint.y = Math.max(Math.min(point.y
				+ (int) (random.nextFloat() * vector.y), display.getHeight()),
				0);

		return newPoint;
	}

	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return adjustEventFactors();
	}

}
