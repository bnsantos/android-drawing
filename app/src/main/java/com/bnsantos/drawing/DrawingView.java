package com.bnsantos.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
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

  private boolean mDrawingEnabled = true;

  /*
    Drawing elements
   */
  private MyPath mCurrentPath;
  private Circle mCurrentCircle;
  private Rectangle mCurrentRectangle;

  private List<Action> mActions;
  private List<Action> mUndoActions;

  private WeakReference<DrawingViewListener> mListener;

  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mPaintColor = ContextCompat.getColor(context, R.color.palette_black);
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
      mCurrentCircle.drawAction(canvas);
    }

    if(mCurrentRectangle!=null){
      mCurrentRectangle.drawAction(canvas);
    }
  }

  private void recreateCanvasBitmap(){
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
        if(mDrawingEnabled) {
          onTouchDown(touchX, touchY);
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if(mDrawingEnabled) {
          onTouchMode(touchX, touchY);
        }
        break;
      case MotionEvent.ACTION_UP:
        if(mDrawingEnabled){
          onTouchUp();
        }else{
          if(mListener!=null&&mListener.get()!=null){
            mListener.get().onCanvasClick();
          }
          mDrawingEnabled = true;
        }
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
    clearRedoList();
    onAction();
  }

  private void clearRedoList() {
    if(mUndoActions==null){
      mUndoActions = new ArrayList<>();
    }else{
      mUndoActions.clear();
    }
  }

  private void onAction() {
    if(mListener!=null&&mListener.get()!=null){
      mListener.get().onAction();
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
      onAction();
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
      onAction();
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

  public void setMode(int option){
    if(option >= PENCIL_MODE && option <= ERASER_MODE){
      mMode = option;
    }
  }

  public boolean canRedo() {
    return mUndoActions!=null&&mUndoActions.size()>0;
  }

  public boolean canUndo(){
    return mActions!=null&&mActions.size()>0;
  }

  public void textTest() {
    String text = "Bacon ipsum dolor amet landjaeger pork belly leberkas sirloin, beef ribs bacon strip steak ribeye bresaola doner corned beef. Flank pork ball tip sausage jerky, pork loin alcatra landjaeger pig sirloin corned beef. Kevin jowl ground round, meatball chicken leberkas frankfurter jerky. Turducken fatback swine, tail sausage drumstick pork loin sirloin bacon doner ground round. Shank andouille bacon boudin leberkas ham hock t-bone. Meatloaf sirloin rump ham hock, tongue picanha sausage pancetta andouille jowl turkey strip steak biltong.";

    Rectangle rectangle = new Rectangle(50, 300, mDrawPaint);
    rectangle.setFinalPoint(350, 600);
    rectangle.drawAction(mDrawCanvas);
    mActions.add(rectangle);
    drawRectText(text, mDrawCanvas, new Rect(50, 300, 350, 600));
    drawText(text);
    invalidate();
  }

  private void drawRectText(String text, Canvas canvas, Rect r) {
    Paint textPaint = new Paint();

    textPaint.setTextSize(20);
    textPaint.setColor(mDrawPaint.getColor());
    textPaint.setTextAlign(Paint.Align.LEFT);
    int width = r.width();

    int numOfChars = textPaint.breakText(text,true,width,null);
    int start = (text.length()-numOfChars)/2;
    canvas.drawText(text,start,start+numOfChars,r.exactCenterX(),r.exactCenterY(),textPaint);
  }

  private void drawText(String text){
    TextPaint mTextPaint=new TextPaint();
    StaticLayout mTextLayout = new StaticLayout(text, mTextPaint, mDrawCanvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

    mDrawCanvas.save();
// calculate x and y position where your text will be placed

    float textX = 50.0f;
    float textY = 300.0f;

    mDrawCanvas.translate(textX, textY);
    mTextLayout.draw(mDrawCanvas);
    mDrawCanvas.restore();
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
    public float centerX, centerY;
    public float startX, startY;
    public float radius;
    public Paint actionPaint;

    public Circle(float x, float y, Paint actionPaint) {
      this.startX = x;
      this.startY = y;
      this.actionPaint = new Paint(actionPaint);
    }

    public void setRadius(float currentX, float currentY) {
      this.radius = (float) Math.sqrt(Math.pow(startX - currentX, 2) + Math.pow(startY- currentY, 2))/2.0f;
      this.centerX = (this.startX + currentX)/2.0f;
      this.centerY = (this.startY + currentY)/2.0f;
    }

    @Override
    public void drawAction(Canvas canvas) {
      canvas.drawCircle(centerX, centerY, radius, actionPaint);
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

  public void setListener(DrawingViewListener listener) {
    this.mListener = new WeakReference<>(listener);
  }

  public interface DrawingViewListener{
    void onCanvasClick();
    void onAction();
  }

  public boolean isDirty(){
    return (mActions!=null && mActions.size()>0);
  }

  public void setDrawingEnabled(boolean enabled) {
    this.mDrawingEnabled = enabled;
  }
}
