package com.bnsantos.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by bruno on 07/01/15.
 */
public class DrawingView extends View {
    private Path mPath;
    private Paint mDrawPaint, mCanvasPaint;
    private int mPaintColor;
    private int mStrokeWidth;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private boolean mErase = false;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintColor = context.getResources().getColor(R.color.black);
        mStrokeWidth = (int) context.getResources().getDimension(R.dimen.small_brush);
        setupDrawing();
    }

    private void setupDrawing(){
        mPath = new Path();
        mDrawPaint = new Paint();

        mDrawPaint.setColor(mPaintColor);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(mStrokeWidth);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);

        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mPath, mDrawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                mDrawCanvas.drawPath(mPath, mDrawPaint);
                mPath.reset();
                break;
            default:
                return super.onTouchEvent(event);
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        invalidate();
        mPaintColor = Color.parseColor(newColor);
        mDrawPaint.setColor(mPaintColor);
    }

    public void setErase(boolean erase){
        mErase = erase;

        if(erase) mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mDrawPaint.setXfermode(null);
    }

    public void newDrawing(){
        mDrawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
}
