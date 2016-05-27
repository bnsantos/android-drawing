package com.bnsantos.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
  public static final int PEN_MODE = 1;
  public static final int CIRCLE_MODE = 2;
  public static final int SQUARE_MODE = 3;

  private Paint mDrawPaint, mCanvasPaint;
  private int mPaintColor;
  private float mStrokeWidth;
  private Canvas mDrawCanvas;
  private boolean mErase = false;

  private Bitmap mBackgroundBitmap;
  private Bitmap mCanvasBitmap;

  /*
    Drawing elements
   */
  private Path mCurrentPath;
  private List<Path> mPaths;
  private List<Path> mUndoPaths;

//  private List<>



  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaintColor = ContextCompat.getColor(context, R.color.black);
    mStrokeWidth = (int) context.getResources().getDimension(R.dimen.m_brush);
    setupDrawing();
  }

  private void setupDrawing(){
    mPaths = new ArrayList<>();
    mUndoPaths = new ArrayList<>();
    mCurrentPath = new Path();
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
    if(mCurrentPath!=null){
      canvas.drawPath(mCurrentPath, mDrawPaint);
    }
  }

  private void recreateCanvasBitmap(){
    //TODO case when the image was not loaded in background
    if(mBackgroundBitmap==null){
      mBackgroundBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
      mBackgroundBitmap.eraseColor(Color.WHITE);
    }
    mCanvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    mDrawCanvas = new Canvas(mCanvasBitmap);
    mDrawCanvas.drawBitmap(mBackgroundBitmap, 0, 0, mCanvasPaint);

    if(mPaths!=null) {
      for (Path path : mPaths) {
        mDrawCanvas.drawPath(path, mDrawPaint);
      }
    }
    invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float touchX = event.getX();
    float touchY = event.getY();
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mCurrentPath.reset();
        mCurrentPath.moveTo(touchX, touchY);
        break;
      case MotionEvent.ACTION_MOVE:
        mCurrentPath.lineTo(touchX, touchY);
        break;
      case MotionEvent.ACTION_UP:
        mPaths.add(mCurrentPath);
        mDrawCanvas.drawPath(mCurrentPath, mDrawPaint);
        mCurrentPath = new Path();
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

  public void setWidth(float width) {
    mStrokeWidth = width;
    mDrawPaint.setStrokeWidth(mStrokeWidth);
  }

  public void undo(){
    if(mPaths!=null&&mPaths.size()>0){
      mUndoPaths.add(mPaths.remove(mPaths.size()-1));
      recreateCanvasBitmap();
    }
  }

  public void clearAll(){
    if(mPaths!=null&&mPaths.size()>0){
      mPaths.clear();
      recreateCanvasBitmap();
    }
  }

  public void redo(){
    if(mUndoPaths!=null&&mUndoPaths.size()>0){
      mPaths.add(mUndoPaths.remove(mUndoPaths.size()-1));
      recreateCanvasBitmap();
    }
  }

  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if(bitmapDrawable.getBitmap() != null) {
        mBackgroundBitmap = bitmapDrawable.getBitmap();
      }
    }

    if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
    } else {
      mBackgroundBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
      mBackgroundBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }

    //TODO case when the image is not loaded..... deal with it
  }

  public void setErase(boolean erase){
    mErase = erase;

    if(erase) mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    else mDrawPaint.setXfermode(null);
  }
}
