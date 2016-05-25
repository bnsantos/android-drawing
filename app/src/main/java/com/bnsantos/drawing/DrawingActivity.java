package com.bnsantos.drawing;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bnsantos.drawing.databinding.ActivityDrawingBinding;

@SuppressWarnings({"UnusedAssignment", "ResourceAsColor", "ResourceType"})
public class DrawingActivity extends AppCompatActivity implements View.OnClickListener {
  private static final String TAG_COLOR = "color";
  private static final String TAG_WIDTH = "width";
  public static final String SEPARATOR = ":";

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
      //TODO uri
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
        undo();
      }
    });
    mBinding.toolbar.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        confirm();
      }
    });
  }

  private void confirm() {
    Toast.makeText(DrawingActivity.this, "TODO confirm", Toast.LENGTH_SHORT).show();
  }

  private void undo() {
    Toast.makeText(DrawingActivity.this, "TODO undo", Toast.LENGTH_SHORT).show();
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
