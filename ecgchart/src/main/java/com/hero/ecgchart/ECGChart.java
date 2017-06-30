package com.hero.ecgchart;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by admin on 2017/6/22.
 */

public class ECGChart extends View {
    private float mGraphMax = 2500.f;
    private int mRedrawInterval = 50;
    private int mRedrawPoints;

    static final int SWEEP_MODE = 0;
    static final int FLOW_MODE = 1;
    private int mLineColor;
    private int mGridColor;
    private int mArrowColor;

    private int mWindowSize;
    private int mWindowCount = 2;
    private int ONEWINDOW = 240;
    private LinkedBlockingDeque<PointData> mInputBuf;
    private Vector<PointData> mDrawingBuf;

    private Paint mPaint;
    private Paint mAFPaint;
    private Paint mNormalPaint;


    private Paint mPaintGrid;
    private Paint mPaintRuler;
    private Paint mPaintArrow;


    private Paint mPaintSmallGrid;

    private Paint mMaskBarPaint;
    private int mDrawPosition;

    private Activity mActivity;

    private int mGraphMode = 1;

    private boolean mGrid = true;
    private boolean mArrow = true;

    private boolean mFullscreen = false;
    private boolean isConnected = false;

    public ECGChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity) context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ECGChart,
                0, 0);

        try {
            mLineColor = a.getColor(R.styleable.ECGChart_lineColor, Color.WHITE);
            mGridColor = a.getColor(R.styleable.ECGChart_gridColor, Color.argb(0x33, 0x00, 0xFF, 0x00));
            mArrowColor = a.getColor(R.styleable.ECGChart_arrowColor, Color.rgb(255, 255, 255));


            mGraphMode = a.getInt(R.styleable.ECGChart_graphMode, SWEEP_MODE);
            mGrid = a.getBoolean(R.styleable.ECGChart_grid, true);
            mArrow = a.getBoolean(R.styleable.ECGChart_arrow, false);

            mWindowSize = a.getColor(R.styleable.ECGChart_windowSize, ONEWINDOW * mWindowCount);

            mInputBuf = new LinkedBlockingDeque<>();
            mDrawingBuf = new Vector<>();

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(3f);
            mPaint.setColor(mLineColor);

            mAFPaint = new Paint();
            mAFPaint.setAntiAlias(true);
            mAFPaint.setStrokeWidth(5f);
            mAFPaint.setColor(Color.rgb(255, 255, 255));

            mNormalPaint = new Paint();
            mNormalPaint.setAntiAlias(true);
            mNormalPaint.setStrokeWidth(5f);
            mNormalPaint.setColor(Color.rgb(255, 255, 255));

            mPaintGrid = new Paint();
            mPaintGrid.setColor(mGridColor);
            mPaintGrid.setStrokeWidth(2f);
            mPaintSmallGrid = new Paint();
            mPaintSmallGrid.setColor(mGridColor);
            mPaintSmallGrid.setStrokeWidth(1f);

            mPaintArrow = new Paint();
            mPaintArrow.setColor(mArrowColor);
            mPaintArrow.setStrokeWidth(2);
            mPaintArrow.setStyle(Paint.Style.FILL);

            mPaintRuler = new Paint();
            mPaintRuler.setColor(Color.argb(0xAA, 0xFF, 0xFF, 0xFF));

            mMaskBarPaint = new Paint();
            mMaskBarPaint.setColor(Color.rgb(0x33, 0x33, 0x33));
            mMaskBarPaint.setStyle(Paint.Style.FILL);
        } finally {
            a.recycle();
        }
        init();
    }

    public void setWindowSize(int size) {
        this.mWindowSize = size;
    }

    private void init() {
        mRedrawPoints = ONEWINDOW / (1000 / mRedrawInterval);
        for (int i = 0; i < mWindowSize; i++)
            mDrawingBuf.add(new PointData(1250));

        TimerTask drawEmitter = new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnected)
                            return;
                        if (mInputBuf.size() < ONEWINDOW)
                            return;
                        if (mInputBuf.size() > 250) {
                            mRedrawPoints++;
                        }
                        if (mInputBuf.size() <= 250) {
                            mRedrawPoints = ONEWINDOW / (1000 / mRedrawInterval);
                        }
                        if (mGraphMode == SWEEP_MODE) {
                            for (int i = 0; i < mRedrawPoints; i++) {
                                PointData val = mInputBuf.pollFirst();
                                mDrawingBuf.remove(mDrawPosition);
                                mDrawingBuf.add(mDrawPosition++, val);
                                if (mDrawPosition >= mWindowSize) mDrawPosition = 0;
                            }
                        } else {
                            for (int i = 0; i < mRedrawPoints; i++) {
                                PointData val = mInputBuf.pollFirst();
                                mDrawingBuf.remove(0);
                                mDrawingBuf.add(val);
                            }
                        }

                        invalidate();
                        Log.d("inputBufSizeinput", mInputBuf.size() + "");
                        Log.d("inputBufSizedraw", mDrawingBuf.size() + "");
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(drawEmitter, 0, mRedrawInterval);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = this.getWidth();
        float height = this.getHeight();

        if (mDrawingBuf.size() < mWindowSize)
            return;

        float mapRatio = width / mWindowSize;
        PointData start = mDrawingBuf.get(0);

        for (int i = 1; i < mWindowSize; i++) {
            PointData end = mDrawingBuf.get(i);
            canvas.drawLine(i * mapRatio, height - start.value / mGraphMax * height, (i + 1) * mapRatio, height - end.value / mGraphMax * height, mPaint);
            if (start.info == 1) {// if af then
                canvas.drawText("AF", i * mapRatio - 10, height - start.value / mGraphMax * height - 10, mAFPaint);
                canvas.drawPoint(i * mapRatio, height - start.value / mGraphMax * height, mAFPaint);
            }
            if (start.info == 0) {// if normal then
                canvas.drawText("N", i * mapRatio - 5, height - start.value / mGraphMax * height - 10, mNormalPaint);
                canvas.drawPoint(i * mapRatio, height - start.value / mGraphMax * height, mNormalPaint);
            }

            start = end;
        }

        if (mGraphMode == SWEEP_MODE)
            canvas.drawRect((mDrawPosition - 10) * mapRatio, 0, (mDrawPosition + 10) * mapRatio, height, mMaskBarPaint);
        if (mGrid)
            drawGridLine(canvas);
    }

    public void setGraphMode(int type) {
        mInputBuf.clear();
        mGraphMode = type;
    }

    public void setConnection(boolean isConnect) {
        if (!isConnect)
            mInputBuf.clear();
        isConnected = isConnect;
    }

    public void drawGridLine(Canvas canvas) {
        float width = this.getWidth();
        float height = this.getHeight();
        int gridXNumber = 8;
        int gridYNumber = 5;
        for (int i = 0; i < gridXNumber; i++) {
            canvas.drawLine(i * width / gridXNumber, 0, i * width / gridXNumber, height, mPaintGrid);
//            String text = i + "";
//            float txtWidth = mPaintRuler.measureText(text);
//            canvas.drawText(text, i * width / gridXNumber - txtWidth / 2f, height - 4, mPaintRuler);
        }

        for (int i = 0; i < gridXNumber; i++) {
            for (int j = 0; j < 10; j++)
                canvas.drawLine(i * width / gridXNumber + j * width / gridXNumber / 10f, 0,
                        i * width / gridXNumber + j * width / gridXNumber / 10f, height, mPaintSmallGrid);
        }

        for (int i = 0; i < gridYNumber; i++) {
            canvas.drawLine(0, i * height / gridYNumber, width, i * height / gridYNumber, mPaintGrid);
//            canvas.drawT
        }

        for (int i = 0; i < gridYNumber; i++) {
            for (int j = 0; j < 10; j++)
                canvas.drawLine(0, i * height / gridYNumber + j * height / gridYNumber / 10f,
                        width, i * height / gridYNumber + j * height / gridYNumber / 10f, mPaintSmallGrid);
        }

        if (mArrow) {
            float pX = 10;
            float pY = height - 10;
            float dx = width / gridXNumber;
            float dy = height / gridYNumber;
            canvas.drawLine(pX, pY, pX, pY - dy + 5, mPaintArrow);
            canvas.drawLine(pX, pY, pX + dx - 5, pY, mPaintArrow);

            Path path = new Path();

            path.moveTo(0, -10);
            path.lineTo(5, 0);
            path.lineTo(-5, 0);
            path.close();

            Path path2 = new Path();

            path2.moveTo(10, 0);
            path2.lineTo(0, 5);
            path2.lineTo(0, -5);
            path2.close();

            path.offset(pX, pY - dy + 5);
            path2.offset(pX + dx - 5, pY);

            canvas.drawPath(path, mPaintArrow);
            canvas.drawPath(path2, mPaintArrow);
            mPaintArrow.setStrokeWidth(1);
            mPaintArrow.setAntiAlias(true);
            canvas.drawText("250 ms", pX + 28, pY - 10, mPaintArrow);
            canvas.drawText("500 mV", pX + 8, pY - dy + 15, mPaintArrow);
        }
    }

    public void addEcgData(int data) {
        mInputBuf.addLast(new PointData(data));
        Log.i("addEcgData", "" + mInputBuf.size());
    }

    public void addEcgData(PointData data) {
        mInputBuf.addLast(data);
    }

    public void addEcgDataArr(int[] data) {
        Log.i("addEcgDataArr", "" + data.length + " : " + mInputBuf.size());
        checkBufOverflow();
        for (int aData : data) {
            mInputBuf.addLast(new PointData(aData));
        }
    }

    public void addEcgDataArrBl(LinkedBlockingDeque<Integer> data) {

        checkBufOverflow();
        for (int i = 0; i < data.size(); i++) {
            try {
                mInputBuf.addLast(new PointData(data.takeFirst()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (data.size() > 0) {
            ONEWINDOW = data.size();
            mWindowSize = ONEWINDOW * 3;
            mRedrawPoints = ONEWINDOW / (1000 / mRedrawInterval);
        }
    }

    private void checkBufOverflow() {
        Log.i("size", mInputBuf.size() + "");
        if (mInputBuf.size() > 2000)
            mInputBuf.clear();
    }

    public void toggleFullscreen() {
        mFullscreen = !mFullscreen;
        if (mFullscreen) {
            setRotation(90);
            this.setLayoutParams(new LinearLayout.LayoutParams(300, 600));
        } else {
            setRotation(0);
        }
        invalidate();
    }
}
