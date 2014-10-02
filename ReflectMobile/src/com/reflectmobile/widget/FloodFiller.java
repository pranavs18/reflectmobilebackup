package com.reflectmobile.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

public class FloodFiller {

	protected Bitmap image = null;
	protected int[] tolerance = new int[] { 0, 0, 0 };
	protected int width = 0;
	protected int height = 0;
	protected int[] pixelBuffer = null;
	protected int fillColor = Color.WHITE;
	protected int borderColor = Color.RED;
	protected int[] startColor = new int[] { 0, 0, 0 };
	protected boolean[] isVisited;
	protected Queue<FloodFillRange> queue;
	protected ArrayList<Point> borderPointList;

	// Construct using an image and a copy will be made to fill into,
	// Construct with BufferedImage and flood fill will write directly to
	// provided BufferedImage
	public FloodFiller(Bitmap img) {
		image = img;
		width = img.getWidth();
		height = img.getHeight();
		pixelBuffer = new int[width * height];
		image.getPixels(pixelBuffer, 0, width, 0, 0, width, height);
	}

	public int[] getTolerance() {
		return tolerance;
	}

	public void setTolerance(int[] value) {
		tolerance = value;
	}

	public void setTolerance(int value) {
		tolerance = new int[] { value, value, value };
	}

	public Bitmap getImage() {
		return image;
	}

	public ArrayList<Point> getBorderPointList() {
		return borderPointList;
	}

	public void setBorderPointList(ArrayList<Point> borderPointList) {
		this.borderPointList = borderPointList;
	}

//	public void copyImage(Bitmap img) {
//		// Copy data from provided Image to a BufferedImage to write flood fill
//		// to, use getImage to retrieve
//		// cache data in member variables to decrease overhead of property calls
//		width = img.getWidth();
//		height = img.getHeight();
//
//		image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(image);
//		canvas.drawBitmap(img, 0, 0, null);
//
//		pixelBuffer = new int[width * height];
//
//		image.getPixels(pixelBuffer, 0, width, 0, 0, width, height);
//	}

	protected void prepare() {
		// Called before starting flood-fill
		isVisited = new boolean[pixelBuffer.length];
		queue = new LinkedList<FloodFillRange>();
		borderPointList = new ArrayList<Point>();
	}

	// Fills the specified point on the bitmap with the currently selected fill
	// color.
	// int x, int y: The starting coords for the fill
	public void floodFill(int x, int y) {
		// Setup
		prepare();

		if (startColor[0] == 0) {
			// ***Get starting color.
			int startPixel = pixelBuffer[(width * y) + x];
			startColor[0] = (startPixel >> 16) & 0xff;
			startColor[1] = (startPixel >> 8) & 0xff;
			startColor[2] = startPixel & 0xff;
		}

		// ***Do first call to floodfill.
		LinearFill(x, y);

		// ***Call floodfill routine while floodfill ranges still exist on the
		// queue
		FloodFillRange range;

		while (queue.size() > 0) {
			// **Get Next Range Off the Queue
			range = queue.remove();

			// **Check Above and Below Each Pixel in the Floodfill Range
			int downPxIdx = (width * (range.Y + 1)) + range.startX;
			int upPxIdx = (width * (range.Y - 1)) + range.startX;
			int upY = range.Y - 1;// so we can pass the y coord by ref
			int downY = range.Y + 1;

			for (int i = range.startX; i <= range.endX; i++) {
				// *Start Fill Upwards
				// if we're not above the top of the bitmap and the pixel above
				// this one is within the color tolerance
				if (range.Y > 0 && (!isVisited[upPxIdx]) && CheckPixel(upPxIdx))
					LinearFill(i, upY);

				// *Start Fill Downwards
				// if we're not below the bottom of the bitmap and the pixel
				// below this one is within the color tolerance
				if (range.Y < (height - 1) && (!isVisited[downPxIdx])
						&& CheckPixel(downPxIdx))
					LinearFill(i, downY);

				downPxIdx++;
				upPxIdx++;
			}
		}

//		image.setPixels(pixelBuffer, 0, width, 0, 0, width, height);
	}

	// Finds the furthermost left and right boundaries of the fill area
	// on a given y coordinate, starting from a given x coordinate, filling as
	// it goes.
	// Adds the resulting horizontal range to the queue of floodfill ranges,
	// to be processed in the main loop.

	// int x, int y: The starting coords
	protected void LinearFill(int x, int y) {
		// ***Find Left Edge of Color Area
		int lFillLoc = x; // the location to check/fill on the left
		int pxIdx = (width * y) + x;

		while (true) {
			// **fill with the color
			// pixels[pxIdx] = fillColor;
			borderPointList.add(new Point(lFillLoc, y));

			// **indicate that this pixel has already been checked and filled
			isVisited[pxIdx] = true;

			// **de-increment
			lFillLoc--; // de-increment counter
			pxIdx--; // de-increment pixel index

			// **exit loop if we're at edge of bitmap or color area
			if (lFillLoc < 0 || isVisited[pxIdx]) {
				break;
			} else if (!CheckPixel(pxIdx)) {
				// pixelBuffer[pxIdx] = borderColor;
				break;
			}
		}

		lFillLoc++;

		// ***Find Right Edge of Color Area
		int rFillLoc = x; // the location to check/fill on the left

		pxIdx = (width * y) + x;

		while (true) {
			// **fill with the color
			// pixels[pxIdx] = fillColor;
			borderPointList.add(new Point(rFillLoc, y));

			// **indicate that this pixel has already been checked and filled
			isVisited[pxIdx] = true;

			// **increment
			rFillLoc++; // increment counter
			pxIdx++; // increment pixel index

			// **exit loop if we're at edge of bitmap or color area
			if (rFillLoc >= width || isVisited[pxIdx]) {
				break;
			} else if (!CheckPixel(pxIdx)) {
				// pixelBuffer[pxIdx] = borderColor;
				break;
			}
		}

		rFillLoc--;

		// add range to queue
		FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);
		// System.out.println("OLD:" + lFillLoc + " " + rFillLoc + " " + y);

		queue.offer(r);
	}

	// Sees if a pixel is within the color tolerance range.
	protected boolean CheckPixel(int px) {
		int red = (pixelBuffer[px] >>> 16) & 0xff;
		int green = (pixelBuffer[px] >>> 8) & 0xff;
		int blue = pixelBuffer[px] & 0xff;

		return (red >= (startColor[0] - tolerance[0])
				&& red <= (startColor[0] + tolerance[0])
				&& green >= (startColor[1] - tolerance[1])
				&& green <= (startColor[1] + tolerance[1])
				&& blue >= (startColor[2] - tolerance[2]) && blue <= (startColor[2] + tolerance[2]));
	}

	// Represents a linear range to be filled and branched from.
	protected class FloodFillRange {
		public int startX;
		public int endX;
		public int Y;

		public FloodFillRange(int startX, int endX, int y) {
			this.startX = startX;
			this.endX = endX;
			this.Y = y;
		}
	}
}
