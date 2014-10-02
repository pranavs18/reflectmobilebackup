package com.reflectmobile.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

public class Segmentation {
	private Bitmap originalBitmap;
	private Bitmap resultBitmap;
	private int lineColor = Color.WHITE;
	private int width;
	private int height;
	private ArrayList<Point> convevHullPointList;
	private int touchPointX;
	private int touchPointY;
	private FloodFiller floodFiller;

	public Segmentation(Bitmap bitmap) {
		this.originalBitmap = bitmap;
		this.width = originalBitmap.getWidth();
		this.height = originalBitmap.getHeight();
		this.floodFiller = new FloodFiller(originalBitmap);
	}

	public void setTouchLocation(int touchPointX, int touchPointY) {
		this.touchPointX = touchPointX;
		this.touchPointY = touchPointY;
	}

	/**
	 * Main function of this class, using flood fill algorithm to
	 * get the border point list and find convex hull list and 
	 * draw the boundary of the convex hull list;
	 * @param threshold - threshold of the flood fill algorithm
	 */
	public void segmentation(int threshold) {
		floodFiller.setTolerance(threshold);
		floodFiller.floodFill(touchPointX, touchPointY);

		floodFiller.getImage();
		ArrayList<Point> borderPointList = floodFiller.getBorderPointList();

		// Convex hull
		convevHullPointList = findConvexHull(borderPointList);

		// Copy
		resultBitmap = copyImage(originalBitmap);

		// Normolize point list
		normilizePointList();

		// Draw lines
		drawLines();

		ArrayList<Point> touchPointList = new ArrayList<Point>();
		touchPointList.add(new Point(touchPointX, touchPointY));
		drawDots(touchPointList);

		return;
	}

	public ArrayList<Point> findConvexHull(ArrayList<Point> points) {
		@SuppressWarnings("unchecked")
		ArrayList<Point> xSorted = (ArrayList<Point>) points.clone();
		Collections.sort(xSorted, new Comparator<Point>() {

			@Override
			public int compare(Point lhs, Point rhs) {
				return lhs.x - rhs.x;
			}
		});

		int n = xSorted.size();

		Point[] lUpper = new Point[n];

		lUpper[0] = xSorted.get(0);
		lUpper[1] = xSorted.get(1);

		int lUpperSize = 2;

		for (int i = 2; i < n; i++) {
			lUpper[lUpperSize] = xSorted.get(i);
			lUpperSize++;

			while (lUpperSize > 2
					&& !rightTurn(lUpper[lUpperSize - 3],
							lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
				// Remove the middle point of the three last
				lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
				lUpperSize--;
			}
		}

		Point[] lLower = new Point[n];

		lLower[0] = xSorted.get(n - 1);
		lLower[1] = xSorted.get(n - 2);

		int lLowerSize = 2;

		for (int i = n - 3; i >= 0; i--) {
			lLower[lLowerSize] = xSorted.get(i);
			lLowerSize++;

			while (lLowerSize > 2
					&& !rightTurn(lLower[lLowerSize - 3],
							lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
				// Remove the middle point of the three last
				lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
				lLowerSize--;
			}
		}

		ArrayList<Point> result = new ArrayList<Point>();

		for (int i = 0; i < lUpperSize; i++) {
			result.add(lUpper[i]);
		}

		for (int i = 1; i < lLowerSize - 1; i++) {
			result.add(lLower[i]);
		}

		return result;
	}

	private boolean rightTurn(Point a, Point b, Point c) {
		return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
	}

	/*
	 * Make sure the point is in the border of the image
	 */
	private void normilizePointList() {
		int offset = 10;
		for (Point point : convevHullPointList) {
			point.x = Math.min(resultBitmap.getWidth() - offset,
					Math.max(offset, point.x));
			point.y = Math.min(resultBitmap.getHeight() - offset,
					Math.max(offset, point.y));
		}

	}

	/**
	 * Draw the boundary of based for the convex hull
	 */
	private void drawLines() {
		if (convevHullPointList.size() <= 3) {
			return;
		}
		Paint paint = new Paint();
		paint.setColor(lineColor);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(6);
		Canvas canvas = new Canvas(resultBitmap);

		Point prevPoint = convevHullPointList.get(0);
		for (int i = 1; i <= convevHullPointList.size() - 1; i++) {
			Point currentPoint = convevHullPointList.get(i);
			canvas.drawLine(prevPoint.x, prevPoint.y, currentPoint.x,
					currentPoint.y, paint);
			prevPoint = currentPoint;
		}
		canvas.drawLine(prevPoint.x, prevPoint.y, convevHullPointList.get(0).x,
				convevHullPointList.get(0).y, paint);
		return;
	}

	private void drawDots(ArrayList<Point> pointList) {
		int littleSquareRadius = 6;
		Canvas canvas = new Canvas(resultBitmap);
		for (Point point : pointList) {
			int x = Math.min(resultBitmap.getWidth() - littleSquareRadius,
					Math.max(littleSquareRadius, point.x));
			int y = Math.min(resultBitmap.getHeight() - littleSquareRadius,
					Math.max(littleSquareRadius, point.y));
			// Generate brush for draw little square border
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.FILL);
			// Draw the little square border
			RectF rect = new RectF(x - littleSquareRadius - 2, y
					- littleSquareRadius - 2, x + littleSquareRadius + 2, y
					+ littleSquareRadius + 2);
			canvas.drawRoundRect(rect, 2, 2, paint);

			// Generate brush for draw little square
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			// Draw the little square
			rect = new RectF(x - littleSquareRadius, y - littleSquareRadius, x
					+ littleSquareRadius, y + littleSquareRadius);
			canvas.drawRoundRect(rect, 2, 2, paint);
		}
		return;
	}

	/**
	 * Get square coordination location of the convex hull list
	 * @return
	 */
	public RectF getSquareLocation() {
		// Calculate border
		int left = width;
		int right = 0;
		int top = height;
		int bottom = 0;
		for (Point point : convevHullPointList) {
			left = Math.min(left, point.x);
			right = Math.max(right, point.x);
			top = Math.min(top, point.y);
			bottom = Math.max(bottom, point.y);
		}
		return new RectF(left, top, right, bottom);
	}

	public ArrayList<Point> getConvevHullPointList() {
		return convevHullPointList;
	}

	public Bitmap getResultBitmap() {
		return resultBitmap;
	}

	public Bitmap copyImage(Bitmap img) {
		Bitmap copyedImage = Bitmap
				.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(copyedImage);
		canvas.drawBitmap(originalBitmap, 0, 0, null);
		return copyedImage;
	}
}
