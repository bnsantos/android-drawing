package com.bnsantos.drawing;

import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private DrawingView mView;
    private ImageButton mCurrentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mView = (DrawingView) findViewById(R.id.drawing);
        mCurrentColor = (ImageButton) findViewById(R.id.paint_black);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setDrawerBackground(View v){
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
    }
}
