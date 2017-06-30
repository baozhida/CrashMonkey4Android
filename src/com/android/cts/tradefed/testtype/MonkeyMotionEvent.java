package com.android.cts.tradefed.testtype.monkey;

import java.awt.Point;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.tradefed.log.LogUtil.CLog;

//motion事件是由屏幕上某处一个down事件、一系列伪随机的移动事件和一个up事件组成
public class MonkeyMotionEvent extends MonkeyEvent {

	private List<Point> movePoints = new ArrayList<Point>();
	private Point downPoint = null;
	private Point upPoint = null;

	public MonkeyMotionEvent() {
		super(EVENT_TYPE_MOTION);
		// TODO Auto-generated constructor stub
	}

	public void addMovePoint(Point point) {
		movePoints.add(point);
	}

	public void setDownPoint(Point point) {
		downPoint = point;
	}

	public void setUpPoint(Point point) {
		upPoint = point;
	}

	public List<Point> getMovePoints() {
		return movePoints;
	}

	public void setMovePoints(List<Point> movePoints) {
		this.movePoints = movePoints;
	}

	public Point getDownPoint() {
		return downPoint;
	}

	public Point getUpPoint() {
		return upPoint;
	}

	@Override
	public int fireEvent(AdbChimpDevice acDevice) {
		acDevice.drag(downPoint.x, downPoint.y, upPoint.x, upPoint.y, 1, 1000);
		return 0;
	}

}
