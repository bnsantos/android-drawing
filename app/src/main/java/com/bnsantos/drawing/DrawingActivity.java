package com.bnsantos.drawing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bnsantos.drawing.databinding.ActivityDrawingBinding;
import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

@SuppressWarnings({"UnusedAssignment", "ResourceAsColor", "ResourceType"})
public class DrawingActivity extends AppCompatActivity implements View.OnClickListener, DrawingView.DrawingViewListener {
  private static final int INTENT_REQUEST_STORAGE_PERMISSION = 555;
  private final String TAG = DrawingActivity.class.getSimpleName();
  private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private static final String TAG_COLOR = "color";
  private static final String TAG_WIDTH = "width";
  public static final String SEPARATOR = ":";

  private ActivityDrawingBinding mBinding;

  private static final int COLORS[] = new int[]{
    R.color.palette_black,
    R.color.palette_white,
    R.color.palette_pink,
    R.color.palette_blue,
    R.color.palette_green,
    R.color.palette_orange,
    R.color.palette_yellow,
    R.color.palette_red
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

  private boolean mDrawOptionsVisible = false;

  private View mRedo;
  private View mUndo;

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

      ResizeOptions resizeOptions = new ResizeOptions(width, height);
      DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(ImageRequestBuilder.newBuilderWithSource(uri).setResizeOptions(resizeOptions).build(), this);
      dataSource.subscribe(new BaseBitmapDataSubscriber() {
        @Override
        protected void onNewResultImpl(Bitmap bitmap) {
          mBinding.drawing.setImageBitmap(bitmap);
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
          mBinding.drawing.setImageResource(R.color.palette_red);
        }
      }, UiThreadImmediateExecutorService.getInstance());
    }

    initActionBar();
    init();
    mBinding.strokeOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showStrokeOptions(!mStrokeOptionsVisible);
      }
    });

    mBinding.currentOption.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showDrawOptions(!mDrawOptionsVisible);
      }
    });

    mBinding.optionPencil.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        changeDrawOption(DrawingView.PENCIL_MODE, view);
      }
    });
    mBinding.optionCircle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        changeDrawOption(DrawingView.CIRCLE_MODE, view);
      }
    });
    mBinding.optionRectangle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        changeDrawOption(DrawingView.RECTANGLE_MODE, view);
      }
    });
    mBinding.optionEraser.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        changeDrawOption(DrawingView.ERASER_MODE, view);
        mBinding.drawing.setMode(DrawingView.ERASER_MODE);
      }
    });

    mBinding.drawing.setListener(this);
  }

  private void showAllDrawOptions(){
    mBinding.optionPencil.setVisibility(View.VISIBLE);
    mBinding.optionCircle.setVisibility(View.VISIBLE);
    mBinding.optionRectangle.setVisibility(View.VISIBLE);
    mBinding.optionEraser.setVisibility(View.VISIBLE);
  }

  private void changeDrawOption(int mode, View view){
    mBinding.drawing.setMode(mode);
    switch (mode){
      case DrawingView.CIRCLE_MODE:
        mBinding.currentOption.setImageResource(R.drawable.ic_circle);
        break;
      case DrawingView.RECTANGLE_MODE:
        mBinding.currentOption.setImageResource(R.drawable.ic_rectangle);
        break;
      case DrawingView.ERASER_MODE:
        mBinding.currentOption.setImageResource(R.drawable.ic_broom_white);
        break;
      default:
        mBinding.currentOption.setImageResource(R.drawable.ic_pencil);
        break;
    }
    showAllDrawOptions();
    view.setVisibility(View.GONE);
    showDrawOptions(false);
  }

  private void initActionBar() {
    setSupportActionBar(mBinding.toolbar);
    mBinding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    mBinding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DrawingActivity.this.onBackPressed();
      }
    });

    mUndo = mBinding.toolbar.findViewById(R.id.undo);
    mUndo.setOnClickListener(new View.OnClickListener() {
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
    mRedo = mBinding.toolbar.findViewById(R.id.redo);
    mRedo.setOnClickListener(new View.OnClickListener() {
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
    mBinding.drawing.setDrawingEnabled(!show);
  }

  private void showDrawOptions(boolean show){
    mDrawOptionsVisible = show;
    mBinding.optionsLayout.setVisibility(show?View.VISIBLE:View.GONE);
    mBinding.drawing.setDrawingEnabled(!show);
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

  @SuppressWarnings("ForLoopReplaceableByForEach")
  private void fillColorOptions(){
    for (int i = 0; i < COLORS.length; i++) {
      ImageView colorButton = roundImageView(COLORS[i], TAG_COLOR + SEPARATOR +COLORS[i]);
      colorButton.setOnClickListener(this);
      mBinding.strokeColors.addView(colorButton, generateParams(getResources().getDimensionPixelSize(R.dimen.stroke_opt_size), 0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.stroke_color_margin)));
    }
  }

  @SuppressWarnings("ForLoopReplaceableByForEach")
  private void fillWidthOptions(){
    for (int i = 0; i < WIDTH.length; i++) {
      ImageView widthButton = roundImageView(R.color.palette_black, null);

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

    SaveImageTask task = new SaveImageTask(mBinding.drawing.getDrawingCache(), this);
    task.execute();
  }

  public void imageSaved(Uri uri) {
    if(uri!=null) {
      finish();

      Intent shareIntent = new Intent();
      shareIntent.setAction(Intent.ACTION_VIEW);
      shareIntent.setDataAndType(uri, "image/jpg");
      shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivity(Intent.createChooser(shareIntent, "View image"));
    }else{
      Toast.makeText(DrawingActivity.this, "Error while saving image", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onCanvasClick() {
    showDrawOptions(false);
    showStrokeOptions(false);
  }

  private void checkUndoRedoStatus(){
    if(mBinding.drawing.canRedo()){
      mRedo.setVisibility(View.VISIBLE);
    }else{
      mRedo.setVisibility(View.INVISIBLE);
    }

    if(mBinding.drawing.canUndo()){
      mUndo.setVisibility(View.VISIBLE);
    }else{
      mUndo.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onAction() {
    checkUndoRedoStatus();
  }

  @Override
  protected void onResume() {
    super.onResume();
    checkUndoRedoStatus();
  }
}
