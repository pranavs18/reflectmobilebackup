package com.reflectmobile.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.View;


public class ColorPickerDialog extends Dialog {

public interface OnColorChangedListener {
    void colorChanged(int color);
}

private OnColorChangedListener mListener;
private int mInitialColor;

private static class ColorPickerView extends View {
    private Paint mPaint;
    private Paint mCenterPaint;
    private final int[] mColors;
   
    ColorPickerView(Context c, OnColorChangedListener l, int color) {
        super(c);
        mColors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        Shader s = new SweepGradient(0, 0, mColors, null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32);

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(color);
        mCenterPaint.setStrokeWidth(5);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCenterPaint.setColor(Color.rgb(232, 232, 230));
        canvas.drawCircle(100, 100, 60, mCenterPaint);
        mCenterPaint.setStrokeWidth(2);
        
        mCenterPaint.setColor(Color.rgb(222, 63, 98));
        canvas.drawCircle(250, 100, 60, mCenterPaint);
       
        
        mCenterPaint.setColor(Color.rgb(220,219, 135));
        canvas.drawCircle(400, 100, 60, mCenterPaint);
        mCenterPaint.setStrokeWidth(0);
        
        mCenterPaint.setColor(Color.rgb(42,106,96));
        canvas.drawCircle(550, 100, 60, mCenterPaint);
        
        mCenterPaint.setColor(Color.rgb(239, 248, 249));
        canvas.drawCircle(100, 250, 60, mCenterPaint);
        
        mCenterPaint.setColor(Color.rgb(233, 110, 13));
        canvas.drawCircle(250, 250, 60, mCenterPaint);
        
        mCenterPaint.setColor(Color.rgb(247,150, 69));
        canvas.drawCircle(400, 250, 60, mCenterPaint);
        
        mCenterPaint.setColor(Color.rgb(65,89,80));
        canvas.drawCircle(550, 250, 60, mCenterPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(CENTER_X*8, CENTER_Y*4);
    }

    private static final int CENTER_X = 100;
    private static final int CENTER_Y = 100;
   
}

public ColorPickerDialog(Context context,
                         OnColorChangedListener listener,
                         int initialColor) {
    super(context);

    mListener = listener;
    mInitialColor = initialColor;
}

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    OnColorChangedListener l = new OnColorChangedListener() {
        public void colorChanged(int color) {
            mListener.colorChanged(color);
            dismiss();
        }
    };

    setContentView(new ColorPickerView(getContext(), l, mInitialColor));
    setTitle("Pick Your Color Scheme");
    
}
}