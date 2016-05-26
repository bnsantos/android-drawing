package com.bnsantos.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 07/01/15.
 */
public class DrawingView extends ImageView {
  private List<Path> mPaths;
  private List<Path> mUndoPaths;
  private Path mPath;
  private Paint mDrawPaint, mCanvasPaint;
  private int mPaintColor;
  private float mStrokeWidth;
  private Canvas mDrawCanvas;
  private Bitmap mCanvasBitmap;
  private boolean mErase = false;

  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaintColor = ContextCompat.getColor(context, R.color.black);
    mStrokeWidth = (int) context.getResources().getDimension(R.dimen.m_brush);
    setupDrawing();
  }

  private void setupDrawing(){
    mPaths = new ArrayList<>();
    mUndoPaths = new ArrayList<>();
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
    super.onDraw(canvas);
    canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
    if(mPaths!=null) {
      for (Path path : mPaths) {
        canvas.drawPath(path, mDrawPaint);
      }
    }
    if(mPath!=null){
      canvas.drawPath(mPath, mDrawPaint);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float touchX = event.getX();
    float touchY = event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mPath.reset();
        mPath.moveTo(touchX, touchY);
        break;
      case MotionEvent.ACTION_MOVE:
        mPath.lineTo(touchX, touchY);
        break;
      case MotionEvent.ACTION_UP:
        //mDrawCanvas.drawPath(mPath, mDrawPaint);
        mPaths.add(mPath);
        mPath = new Path();
        invalidate();
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

  public void setWidth(float width) {
    mStrokeWidth = width;
    mDrawPaint.setStrokeWidth(mStrokeWidth);
  }

  public void undo(){
    if(mPaths!=null&&mPaths.size()>0){
      mUndoPaths.add(mPaths.remove(mPaths.size()-1));
      invalidate();
    }
  }

  public void clearAll(){
    if(mPaths!=null&&mPaths.size()>0){
      mPaths.clear();
      invalidate();
    }
  }

  public void redo(){
    if(mUndoPaths!=null&&mUndoPaths.size()>0){
      mPaths.add(mUndoPaths.remove(mUndoPaths.size()-1));
      invalidate();
    }
  }
}
