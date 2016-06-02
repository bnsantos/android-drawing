package com.bnsantos.drawing;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bruno on 02/06/16.
 */
public class SaveImageTask extends AsyncTask<Void, Void, Uri> {
  private static final String TAG = SaveImageTask.class.getSimpleName();
  public static final String FILE_DATE_FORMAT = "yyyyMMdd-HHmmss";
  private final Bitmap drawingCache;
  private final WeakReference<DrawingActivity> activity;

  public SaveImageTask(Bitmap drawingCache, DrawingActivity drawingActivity) {
    this.drawingCache = drawingCache;
    this.activity = new WeakReference<>(drawingActivity);
  }


  @Override
  protected Uri doInBackground(Void... voids) {
    File drawingFolder = createDrawingFolder();
    File file = new File(drawingFolder, "IMG-" + new SimpleDateFormat(FILE_DATE_FORMAT, Locale.getDefault()).format(new Date()) + ".jpg");
    OutputStream fOut = null;

    try {
      fOut = new FileOutputStream(file);
    } catch (Exception e) {
      Log.e(TAG, e.getCause() + e.getMessage());
      return null;
    }

    drawingCache.compress(Bitmap.CompressFormat.JPEG, 75, fOut);

    try {
      fOut.flush();
      fOut.close();

      ContentValues values = new ContentValues();

      values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
      values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
      values.put(MediaStore.MediaColumns.DATA, file.getPath());

      activity.get().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

      return Uri.fromFile(file);
    } catch (IOException e) {
      Log.e(TAG, e.getCause() + e.getMessage());
      return null;
    }

  }

  @Override
  protected void onPostExecute(Uri uri) {
    super.onPostExecute(uri);
    if(activity.get()!=null){
      activity.get().imageSaved(uri);
    }
  }

  private File createDrawingFolder() {
    File mediaFolder = Environment.getExternalStoragePublicDirectory("SimpleDrawing");
    if (!mediaFolder.exists()) {
      Log.i(TAG, "Creating [" + mediaFolder.getAbsolutePath() + "] folders " + mediaFolder.mkdirs());
    }
    return mediaFolder;
  }
}
