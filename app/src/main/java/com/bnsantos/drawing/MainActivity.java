package com.bnsantos.drawing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.bnsantos.drawing.databinding.ActivityMainBinding;


public class             MainActivity extends AppCompatActivity {
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
        Toast.makeText(MainActivity.this, "TODO", Toast.LENGTH_SHORT).show();
      }
    });

    binding.bottomSheet.findViewById(R.id.phoneGallery).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(MainActivity.this, "TODO phone gallery", Toast.LENGTH_SHORT).show();
      }
    });

    binding.bottomSheet.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(MainActivity.this, "TODO phone camera", Toast.LENGTH_SHORT).show();
      }
    });

    binding.contentLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
      }
    });
  }

  @Override
  public void onBackPressed() {
    if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }else{
      super.onBackPressed();
    }
  }
}
