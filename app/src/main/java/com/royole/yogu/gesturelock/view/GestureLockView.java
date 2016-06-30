package com.royole.yogu.gesturelock.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.royole.yogu.gesturelock.model.Cycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Copyright (C) 2015, Royole Corporation all rights reserved.
 * Author  yogu
 * Since  2016/6/29
 */


// 1 All of the view classes defined in the Android framework extend View.
//   Your custom view can also extend View directly, or you can save time by extending one of the existing view subclasses, such as Button.
public class GestureLockView extends View {
    private Paint normalCyclePaint;
    private Paint outCyclePaint = new Paint();
    private Paint innerCyclePaint = new Paint();
    private Paint linePaint = new Paint();
    private Paint errorPaint = new Paint();
    private int OUT_CYCLE_NORMAL = Color.rgb(108, 119, 138);
    private int OUT_CYCLE_ONTOUCH = Color.rgb(025, 066, 103);
    private int INNER_CYCLE_ONTOUCH = Color.rgb(002, 210, 255);
    private int LINE_COLOR = Color.argb(127, 002, 210, 255);
    private int ERROR_COLOR = Color.argb(127, 255, 000, 000);

    private Cycle[] cycles;
    private boolean isFinished = false;
    private boolean result;
    private Path linePath = new Path();
    private List<Integer> linedCycles = new ArrayList<Integer>();
    public OnGestureFinishListener onGestureFinishListener;
    private String keyCode;
    private int eventX, eventY;
    private Timer timer;

    public GestureLockView(Context context) {
        super(context);
        init();
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * 2 To allow Android Studio to interact with your view,
     * at a minimum you must provide a constructor that takes a Context and an AttributeSet object as parameters.
     */
    public GestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Private Method

    /**
     * Canvas defines shapes that you can draw on the screen,
     * while Paint defines the color, style, font, and so forth of each shape you draw.
     */
    private void init() {
        normalCyclePaint = setAntiAliasPaint(3, Paint.Style.STROKE);
        outCyclePaint = setAntiAliasPaint(3, Paint.Style.STROKE);
        innerCyclePaint = setAntiAliasPaint(3, Paint.Style.FILL);
        linePaint = setAntiAliasPaint(6, Paint.Style.STROKE);
        errorPaint = setAntiAliasPaint(3, Paint.Style.STROKE);
    }

    private Paint setAntiAliasPaint(float strokeWidth, Paint.Style paintStyle) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(strokeWidth);
        p.setStyle(paintStyle);
        return p;
    }

    private void drawCycle(Canvas canvas) {
        for (int i = 0; i < cycles.length; i++) {
            setCycleColor(cycles[i]);
            if (cycles[i].isOnTouch()) {
                canvas.drawCircle(cycles[i].getOx(), cycles[i].getOy(),
                        cycles[i].getR(), outCyclePaint);
                canvas.drawCircle(cycles[i].getOx(), cycles[i].getOy(), cycles[i].getR() / 3,
                        innerCyclePaint);
            } else {
                canvas.drawCircle(cycles[i].getOx(), cycles[i].getOy(),
                        cycles[i].getR(), normalCyclePaint);
            }
        }
    }

    private void setCycleColor(Cycle c) {
        if (isFinished && !result) {
            outCyclePaint.setColor(ERROR_COLOR);
            innerCyclePaint.setColor(ERROR_COLOR);
            linePaint.setColor(ERROR_COLOR);
        } else if (c.isOnTouch()) {
            outCyclePaint.setColor(OUT_CYCLE_ONTOUCH);
            innerCyclePaint.setColor(INNER_CYCLE_ONTOUCH);
            linePaint.setColor(LINE_COLOR);
        } else {
            normalCyclePaint.setColor(OUT_CYCLE_NORMAL);
            innerCyclePaint.setColor(INNER_CYCLE_ONTOUCH);
            linePaint.setColor(LINE_COLOR);
        }
    }

    private void drawLine(Canvas canvas) {
        linePath.reset();
        if (linedCycles.size() > 0) {
            for (int i = 0; i < linedCycles.size(); i++) {
                int index = linedCycles.get(i);
                if (i == 0) {
                    linePath.moveTo(cycles[index].getOx(), cycles[index].getOy());// start
                } else {
                    linePath.lineTo(cycles[index].getOx(), cycles[index].getOy());
                }
            }
            if (!isFinished) {
                linePath.lineTo(eventX, eventY);
            }
            canvas.drawPath(linePath, linePaint);
        }
    }
    // End Private - M

    /**
     * 3 Handle Layout Events
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int perSize = 0;
        // divide into 6 parts
        if (cycles == null && (perSize = getWidth() / 6) > 0) {
            cycles = new Cycle[9];
            for (int i = 0; i < 3; i++) {//y
                for (int j = 0; j < 3; j++) {//x
                    Cycle cycle = new Cycle();
                    cycle.setNum(i * 3 + j);
                    cycle.setOx(perSize * (j * 2 + 1));
                    cycle.setOy(perSize * (i * 2 + 1));
                    cycle.setR(perSize * 0.5f);
                    cycles[i * 3 + j] = cycle;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 4 Once you have your object creation and measuring code defined, you can implement onDraw()
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("", "onDraw...");
        drawCycle(canvas);
        drawLine(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isFinished) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    eventX = (int) event.getX();
                    eventY = (int) event.getY();
                    for (int i = 0; i < cycles.length; i++) {
                        if (cycles[i].isPointIn(eventX, eventY)) {
                            cycles[i].setOnTouch(true);
                            if (!linedCycles.contains(cycles[i].getNum())) {
                                linedCycles.add(cycles[i].getNum());
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isFinished = true;
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < linedCycles.size(); i++) {
                        sb.append(linedCycles.get(i));
                    }
                    result = keyCode.equals(sb.toString());
                    if (onGestureFinishListener != null) {
                        onGestureFinishListener.OnGestureFinish(result);
                    }
                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            eventX = eventY = 0;
                            for (int i = 0; i < cycles.length; i++) {
                                cycles[i].setOnTouch(false);
                            }
                            linedCycles.clear();
                            linePath.reset();
                            isFinished = false;
                            postInvalidate();
                        }
                    }, 1000);
                    break;
            }
        }
        invalidate();
        return true;
    }

    // OnGestureFinishListener
    public interface OnGestureFinishListener {
        void OnGestureFinish(Boolean result);
    }

    public void setOnGestureFinishListener(
            OnGestureFinishListener onGestureFinishListener) {
        this.onGestureFinishListener = onGestureFinishListener;
    }
    // End OnGestureFinishListener
}
