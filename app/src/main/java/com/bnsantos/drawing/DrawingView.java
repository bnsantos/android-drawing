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

public class DrawingView extends ImageView {
  public static final int PENCIL_MODE = 1;
  public static final int CIRCLE_MODE = 2;
  public static final int RECTANGLE_MODE = 3;
  public static final int ERASER_MODE = 4;

  private Paint mDrawPaint, mCanvasPaint;
  private int mPaintColor;
  private float mStrokeWidth;
  private Canvas mDrawCanvas;

  private Bitmap mBackgroundBitmap;
  private Bitmap mCanvasBitmap;

  private int mMode = PENCIL_MODE;

  /*
    Drawing elements
   */
  private MyPath mCurrentPath;
  private Circle mCurrentCircle;
  private Rectangle mCurrentRectangle;

  private List<Action> mActions;
  private List<Action> mUndoActions;

  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaintColor = ContextCompat.getColor(context, R.color.black);
    mStrokeWidth = (int) context.getResources().getDimension(R.dimen.m_brush);
    setupDrawing();
  }

  private void setupDrawing(){
    mDrawPaint = new Paint();
    mDrawPaint.setColor(mPaintColor);
    mDrawPaint.setAntiAlias(true);
    mDrawPaint.setStrokeWidth(mStrokeWidth);
    mDrawPaint.setStyle(Paint.Style.STROKE);
    mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
    mDrawPaint.setStrokeCap(Paint.Cap.ROUND);

    mActions = new ArrayList<>();
    mUndoActions = new ArrayList<>();
    mCurrentPath = new MyPath(false, mDrawPaint);

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

    if(mCurrentCircle!=null){
      canvas.drawCircle(mCurrentCircle.x, mCurrentCircle.y, mCurrentCircle.radius, mDrawPaint);
    }

    if(mCurrentRectangle!=null){
      canvas.drawRect(mCurrentRectangle.left(), mCurrentRectangle.top(), mCurrentRectangle.right(), mCurrentRectangle.bottom(), mDrawPaint);
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

    if(mActions!=null) {
      for (Action action : mActions) {
        action.drawAction(mDrawCanvas);
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
        onTouchDown(touchX, touchY);
        break;
      case MotionEvent.ACTION_MOVE:
        onTouchMode(touchX, touchY);
        break;
      case MotionEvent.ACTION_UP:
        onTouchUp();
        break;
      default:
        return super.onTouchEvent(event);
    }
    invalidate();
    return true;
  }

  private void onTouchDown(float touchX, float touchY) {
    switch (mMode){
      case CIRCLE_MODE:
        mCurrentCircle = new Circle(touchX, touchY, mDrawPaint);
        break;
      case RECTANGLE_MODE:
        mCurrentRectangle = new Rectangle(touchX, touchY, mDrawPaint);
        break;
      default: //PENCIL_MODE
        if(mMode==ERASER_MODE) {
          mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
          mDrawPaint.setXfermode(null);
        }
        mCurrentPath = new MyPath(mMode==ERASER_MODE, mDrawPaint);
        mCurrentPath.reset();
        mCurrentPath.moveTo(touchX, touchY);
    }
  }

  private void onTouchMode(float touchX, float touchY) {
    switch (mMode){
      case CIRCLE_MODE:
        mCurrentCircle.setRadius(touchX, touchY);
        break;
      case RECTANGLE_MODE:
        mCurrentRectangle.setFinalPoint(touchX, touchY);
        break;
      default: //PENCIL_MODE
        mCurrentPath.lineTo(touchX, touchY);
    }
  }

  private void onTouchUp() {
    switch (mMode){
      case CIRCLE_MODE:
        mActions.add(mCurrentCircle);
        mCurrentCircle.drawAction(mDrawCanvas);
        mCurrentCircle = null;
        break;
      case RECTANGLE_MODE:
        mActions.add(mCurrentRectangle);
        mCurrentRectangle.drawAction(mDrawCanvas);
        mCurrentRectangle = null;
        break;
      default: //PENCIL_MODE
        mActions.add(mCurrentPath);
        mCurrentPath.drawAction(mDrawCanvas);
        mCurrentPath = null;
    }
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
    if(mActions!=null&&mActions.size()>0){
      mUndoActions.add(mActions.remove(mActions.size()-1));
      recreateCanvasBitmap();
    }
  }

  public void clearAll(){
    if(mActions!=null&&mActions.size()>0){
      mActions.clear();
    }
    if(mUndoActions!=null&&mUndoActions.size()>0){
      mUndoActions.clear();
    }
    recreateCanvasBitmap();
  }

  public void redo(){
    if(mUndoActions!=null&&mUndoActions.size()>0){
      mActions.add(mUndoActions.remove(mUndoActions.size()-1));
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

    if(!(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)) {
      mBackgroundBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
      mBackgroundBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    }
  }

  public void setOption(int option){
    mMode = option;
    if(mMode<PENCIL_MODE||mMode>ERASER_MODE){
      mMode = PENCIL_MODE;
    }
  }

  private class MyPath extends Path implements  Action{
    public boolean erase;
    public Paint actionPaint;

    public MyPath(boolean erase, Paint paint) {
      super();
      this.erase = erase;
      this.actionPaint = new Paint(paint);
    }

    @Override
    public void drawAction(Canvas canvas) {
      if(erase) {
        mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
      } else {
        mDrawPaint.setXfermode(null);
      }
      canvas.drawPath(this, actionPaint);
    }
  }

  private class Circle implements Action{
    public float x,y;
    public float radius;
    public Paint actionPaint;

    public Circle(float x, float y, Paint actionPaint) {
      this.x = x;
      this.y = y;
      this.actionPaint = new Paint(actionPaint);
    }

    public void setRadius(float currentX, float currentY) {
      this.radius = (float) Math.sqrt(Math.pow(x - currentX, 2) + Math.pow(y- currentY, 2));
    }

    @Override
    public void drawAction(Canvas canvas) {
      canvas.drawCircle(x, y, radius, actionPaint);
    }
  }

  private class Rectangle implements Action{
    public float startX;
    public float startY;
    public float endX;
    public float endY;
    public Paint actionPaint;

    public Rectangle(float touchX, float touchY, Paint actionPaint) {
      startX = touchX;
      startY = touchY;
      endX = touchX;
      endY = touchY;
      this.actionPaint = new Paint(actionPaint);
    }

    public void setFinalPoint(float touchX, float touchY) {
      endX = touchX;
      endY = touchY;
    }

    public float left(){
      return (startX < endX) ? startX : endX;
    }

    public float right(){
      return (startX < endX) ? endX : startX;
    }

    public float top(){
      return (startY < endY) ? startY : endY;
    }

    public float bottom(){
      return (startY < endY) ? endY : startY;
    }

    @Override
    public void drawAction(Canvas canvas) {
      canvas.drawRect(left(), top(), right(), bottom(), actionPaint);
    }
  }

  private interface Action{
    void drawAction(Canvas canvas);
  }
}
