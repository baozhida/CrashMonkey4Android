package com.android.cts.tradefed.testtype.monkey;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.cts.tradefed.result.CtsXmlResultReporter;
import com.android.tradefed.log.LogUtil.CLog;

public class Monkey {

	private String testPackage;

	private MonkeySourceRandom mEventSource;

	/** Categories we are allowed to launch **/
	private ArrayList<String> mMainCategories = new ArrayList<String>();
	/** Applications we can switch to. */

	private long mThrottle = 100;
	private int mVerbose = 1;
	private boolean mRandomizeThrottle = false;
	private AdbChimpDevice mDevice;
	private Rectangle mRectangle = null;

	public Monkey(String testPackage, AdbChimpDevice device, float[] factors) throws InterruptedException {
		this.testPackage = testPackage;
		this.mDevice = device;
		init(factors);
	}
	
	/**
	 * Fire next random event
	 */
	public MonkeyEvent creatEvent() {
		MonkeyEvent ev = mEventSource.generateEvents();
		return ev;
	}

	/**
	 * Fire next event
	 */
	public void fireEvent(MonkeyEvent monkeyEvent, CtsXmlResultReporter ctsXmlResultReporter) {
		if (monkeyEvent != null) {
			ctsXmlResultReporter.addMonkeyEvent(monkeyEvent);
			monkeyEvent.fireEvent(mDevice);
		}

	}
	
	public void setY(int y){
		mRectangle.setY(y);
	}

	/**
	 * Initiate the monkey
	 * @throws InterruptedException 
	 */
	private void init(float[] factors) throws InterruptedException {
		Random mRandom = new SecureRandom();
		mRandom.setSeed(10);
		Thread.sleep(5000);
		CLog.i("屏幕宽度"+mDevice.getProperty("display.width"));
		CLog.i("屏幕高度"+mDevice.getProperty("display.height"));
		mRectangle = new Rectangle(Integer.parseInt(mDevice
				.getProperty("display.width")), Integer.parseInt(mDevice
				.getProperty("display.height")));
		mEventSource = new MonkeySourceRandom(mRandom, mThrottle,
				mRandomizeThrottle, mRectangle);
		mEventSource.setVerbose(mVerbose);
		for (int i = 0; i < factors.length; i++) {
			if (factors[i] > 0) {
				mEventSource.setFactors(i, factors[i]);
			}
		}

		//mEventSource.validate();

		// start a random activity
		// mEventSource.generateActivity();
	}

}
