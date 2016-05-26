package com.bnsantos.drawing;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bnsantos.drawing.databinding.ActivityDrawingBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings({"UnusedAssignment", "ResourceAsColor", "ResourceType"})
public class DrawingActivity extends AppCompatActivity implements View.OnClickListener {
  private static final int INTENT_REQUEST_STORAGE_PERMISSION = 555;
  private final String TAG = DrawingActivity.class.getSimpleName();
  private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private static final String TAG_COLOR = "color";
  private static final String TAG_WIDTH = "width";
  public static final String SEPARATOR = ":";
  public static final String FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";

  private ActivityDrawingBinding mBinding;

  private static final int COLORS[] = new int[]{
    R.color.black,
    R.color.white,
    R.color.pink,
    R.color.blue,
    R.color.green,
    R.color.orange,
    R.color.yellow,
    R.color.red
  };

  private static final int WIDTH[] = new int[]{
      R.dimen.ss_brush,
      R.dimen.s_brush,
      R.dimen.m_brush,
      R.dimen.l_brush,
      R.dimen.ll_brush
  };

  private boolean mStrokeOptionsVisible = false;
  private int mCurrentStrokeColor = COLORS[0];
  private int mCurrentStrokeWidth = WIDTH[2];

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_drawing);

    if(getIntent()!=null&&getIntent().getData()!=null){
      Uri uri = getIntent().getData();

      DisplayMetrics displaymetrics = new DisplayMetrics();
      getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
      int height = displaymetrics.heightPixels;
      int width = displaymetrics.widthPixels;

      Picasso.with(this).setLoggingEnabled(true);

      Picasso.with(this)
          .load(uri)
          .resize(width, height)
          .centerInside()
          .placeholder(R.color.blue)
          .error(R.color.red)
          .into(mBinding.drawing);
    }

    initActionBar();
    init();
    mBinding.strokeOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showStrokeOptions(!mStrokeOptionsVisible);
      }
    });
  }

  private void initActionBar() {
    setSupportActionBar(mBinding.toolbar);
    mBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });

    mBinding.toolbar.findViewById(R.id.undo).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mBinding.drawing.undo();
      }
    });
    mBinding.toolbar.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mBinding.drawing.clearAll();
      }
    });
    mBinding.toolbar.findViewById(R.id.redo).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mBinding.drawing.redo();
      }
    });
    mBinding.toolbar.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(DrawingActivity.this, PERMISSION) == PackageManager.PERMISSION_GRANTED) {
          saveImage();
        }else{
          ActivityCompat.requestPermissions(DrawingActivity.this, new String[]{PERMISSION}, INTENT_REQUEST_STORAGE_PERMISSION);
        }
      }
    });
  }

  private void init(){
    mBinding.drawing.setColor(getString(mCurrentStrokeColor));
    mBinding.drawing.setWidth(getResources().getDimensionPixelOffset(mCurrentStrokeWidth));
    fillColorOptions();
    fillWidthOptions();
    updateStrokeOptionsUi();
  }

  private void showStrokeOptions(boolean show) {
    mStrokeOptionsVisible = show;
    mBinding.strokeColors.setVisibility(show?View.VISIBLE: View.GONE);
    mBinding.strokeWidth.setVisibility(show?View.VISIBLE: View.GONE);
  }

  @Override
  public void onClick(View view) {
    showStrokeOptions(false);
    String tag = (String) view.getTag();
    if(tag!=null){
      String[] split = tag.split(SEPARATOR);
      if(split.length==2){
        if(split[0].equals(TAG_COLOR)){
          mCurrentStrokeColor = Integer.parseInt(split[1]);
          mBinding.drawing.setColor(getString(mCurrentStrokeColor));
        }else if(split[0].equals(TAG_WIDTH)){
          mCurrentStrokeWidth = Integer.parseInt(split[1]);
          mBinding.drawing.setWidth(getResources().getDimensionPixelOffset(mCurrentStrokeWidth));
        }
      }
    }
    updateStrokeOptionsUi();
  }

  private void fillColorOptions(){
    for (int i = 0; i < COLORS.length; i++) {
      ImageView colorButton = roundImageView(COLORS[i], TAG_COLOR + SEPARATOR +COLORS[i]);
      colorButton.setOnClickListener(this);
      mBinding.strokeColors.addView(colorButton, generateParams(getResources().getDimensionPixelSize(R.dimen.stroke_opt_size), 0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.stroke_color_margin)));
    }
  }

  private void fillWidthOptions(){
    for (int i = 0; i < WIDTH.length; i++) {
      ImageView widthButton = roundImageView(R.color.black, null);

      LinearLayout container = new LinearLayout(this);
      container.setBackgroundResource(R.drawable.stroke_options_bg);
      container.setGravity(Gravity.CENTER);
      container.setTag(TAG_WIDTH + SEPARATOR + WIDTH[i]);
      container.setOnClickListener(this);
      container.addView(widthButton, generateParams(getResources().getDimensionPixelSize(WIDTH[i]), 0, 0, 0, 0));
      mBinding.strokeWidth.addView(container, generateParams(getResources().getDimensionPixelSize(R.dimen.stroke_opt_size), getResources().getDimensionPixelOffset(R.dimen.stroke_color_margin), 0, 0, 0));
    }
  }

  private ImageView roundImageView(int colorResource, String tag){
    ImageView imageView = new ImageView(this);
    imageView.setTag(tag);

    GradientDrawable shape =  new GradientDrawable();
    shape.setCornerRadius( 800 );
    shape.setColor(Color.parseColor(getString(colorResource)));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      imageView.setBackground(shape);
    }else {
      //noinspection deprecation
      imageView.setBackgroundDrawable(shape);
    }
    return imageView;
  }

  private LinearLayout.LayoutParams generateParams(int size, int marginLeft, int marginTop, int marginRight, int marginBottom) {
    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(size, size);
    containerParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
    return containerParams;
  }

  private void updateStrokeOptionsUi() {
    mBinding.strokeOptions.removeAllViews();
    mBinding.strokeOptions.addView(roundImageView(mCurrentStrokeColor, null), generateParams(getResources().getDimensionPixelSize(mCurrentStrokeWidth), 0, 0, 0, 0));
  }

  private void saveImage(){
    mBinding.drawing.setDrawingCacheEnabled(true);
    mBinding.drawing.invalidate();

    File drawingFolder = createDrawingFolder();
    File file = new File(drawingFolder, "IMG-" + new SimpleDateFormat(FILE_DATE_FORMAT, Locale.getDefault()).format(new Date()) + ".jpg");
    OutputStream fOut = null;

    try {
      fOut = new FileOutputStream(file);
    } catch (Exception e) {
      Log.e(TAG, e.getCause() + e.getMessage());
      Toast.makeText(DrawingActivity.this, "Error", Toast.LENGTH_SHORT).show();
      return;
    }


    mBinding.drawing.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 75, fOut);

    try {
      fOut.flush();
      fOut.close();
    } catch (IOException e) {
      Log.e(TAG, e.getCause() + e.getMessage());
      Toast.makeText(DrawingActivity.this, "Error 2", Toast.LENGTH_SHORT).show();
    }


    ContentValues values = new ContentValues();

    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.MediaColumns.DATA, file.getPath());

    getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    finish();

    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_VIEW);
    shareIntent.setDataAndType(Uri.fromFile(file), "image/jpg");
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivity(Intent.createChooser(shareIntent, "View image"));
  }

  private File createDrawingFolder() {
    File mediaFolder = Environment.getExternalStoragePublicDirectory("SimpleDrawing");
    if (!mediaFolder.exists()) {
      Log.i(TAG, "Creating [" + mediaFolder.getAbsolutePath() + "] folders " + mediaFolder.mkdirs());
    }
    return mediaFolder;
  }

  /*public void setDrawerBackground(View v){
    //TODO option to select any picture from gallery, also use picasso to avoid out of memmory
    mView.setBackgroundResource(R.drawable.monalisa);
  }

  public void paintClicked(View v){
    if(!v.getTag().equals(mCurrentColor.getTag())){
      mCurrentColor.setImageResource(R.drawable.paint);
      mCurrentColor = (ImageButton) v;
      mCurrentColor.setImageResource(R.drawable.paint_pressed);
      mView.setColor(mCurrentColor.getTag().toString());
    }
  }

  public void erase(View v){
    mView.setErase(true);
  }

  public void draw(View v){
    mView.setErase(false);
  }

  public void newDrawing(View v){
    //TODO confirmation dialog
    mView.newDrawing();
  }

  public void saveImage(View v){
    mView.setDrawingCacheEnabled(true);
    String imgSaved = MediaStore.Images.Media.insertImage(
        getContentResolver(), mView.getDrawingCache(),
        UUID.randomUUID().toString()+".png", "drawing");

    if(imgSaved!=null){
      Toast.makeText(getApplicationContext(), "Drawing saved to Gallery!", Toast.LENGTH_SHORT).show();
    }
    else{
      Toast.makeText(getApplicationContext(), "Oops! Image could not be saved.", Toast.LENGTH_SHORT).show();
    }
    mView.destroyDrawingCache();
    mView.newDrawing();
  }*/
}
