package com.bnsantos.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by bruno on 07/01/15.
 */
public class DrawingView extends View {
    private Path mDrawPath;
    private Paint mDrawPaint, mCanvasPaint;
    private int mPaintColor = 0xFF660000;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        //TODO
    }
}
