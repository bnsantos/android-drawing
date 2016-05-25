package com.bnsantos.drawing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.bnsantos.drawing.databinding.ActivityMainBinding;


public class             MainActivity extends AppCompatActivity {
  private static final int PICK_PHOTO_REQ = 123;
  private ActivityMainBinding binding;
  private BottomSheetBehavior mBottomSheetBehavior;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

    binding.start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      }
    });
    
    binding.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(MainActivity.this, "TODO View activity", Toast.LENGTH_SHORT).show();
      }
    });

    binding.bottomSheet.findViewById(R.id.phoneGallery).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        hideBottomSheet();
        choosePictureFromGallery();
      }
    });

    binding.bottomSheet.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        hideBottomSheet();
        Toast.makeText(MainActivity.this, "TODO phone camera", Toast.LENGTH_SHORT).show();
      }
    });

    binding.contentLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        hideBottomSheet();
      }
    });
  }

  @Override
  public void onBackPressed() {
    if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
      hideBottomSheet();
    }else{
      super.onBackPressed();
    }
  }

  private void choosePictureFromGallery() {
    Intent pickPhoto;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    }else {
      pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
    }

    pickPhoto.setType("image/*");
    pickPhoto.addCategory(Intent.CATEGORY_OPENABLE);
    pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
    }

    startActivityForResult(pickPhoto, PICK_PHOTO_REQ);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(resultCode == RESULT_OK){
      switch (requestCode){
        case PICK_PHOTO_REQ:
          final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
          }

          startDrawingActivity(data.getData());
          break;
      }
    }
  }

  private void startDrawingActivity(Uri data) {
    Intent intent = new Intent(this, DrawingActivity.class);
    intent.setData(data);
    startActivity(intent);
  }

  private void hideBottomSheet(){
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
  }
}
