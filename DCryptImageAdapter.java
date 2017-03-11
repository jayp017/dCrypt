package com.example.jay_pc.dcrypt;

/**
 * Created by Jay-pc on 3/10/2017.
 */

import java.io.File;
import java.io.FileFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class DCryptImageAdapter extends BaseAdapter {

    private Context dbContext;
    private static File[] imageFiles = null;
    private static long[] imageFilesId = null;
    private static File parentDir = new File(
            Environment.getExternalStorageDirectory() + "/external_sd/DCIM/Camera/");
    public static int LAST_INDEX;

    public DCryptImageAdapter(Context context) {
        dbContext = context;
        fetchFiles();
    }

    public int getCount() {
        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            return 0;
        }
        return imageFiles.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        if (imageFilesId == null) {
            Log.i("DCrypt", "imageFilesId Is NULL!");
            return -1;
        }
        if (position > LAST_INDEX) {
            return -1;
        }
        return imageFilesId[position];
    }

    public static File getCurrItemId(int position) {
        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            return null;
        }

        if (position > LAST_INDEX) {
            return imageFiles[LAST_INDEX];
        } else {
            return imageFiles[position];
        }
    }

    public static File getPrevItemId(int position) {
        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            return null;
        }
        if(--position < 0) {
            position = LAST_INDEX;
        }
        return imageFiles[position];
    }

    public static File getNextItemId(int position) {
        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            return null;
        }
        if(++position > LAST_INDEX) {
            position = 0;
        }
        return imageFiles[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("DCrypt", "(1)" + position + ";" + convertView + ";" + parent);
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(dbContext);
            imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(5, 5, 5, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        Log.i("DCrypt", "(2)" + position + ";" + imageView + ";" + parent);

        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            fetchFiles();
        }

        try {
            Log.i("DCrypt", imageFiles[position].getAbsolutePath());
            String filePath = imageFiles[position].getAbsolutePath();
            //Drawable tempDrawable = Drawable.createFromPath(filePath);
            //imageView.setImageDrawable(tempDrawable);
            imageView.setImageBitmap(getScaledBitmap(filePath, 100, 100));
        } catch(Exception exp) {
            Log.i("DCrypt", exp.getMessage());
        }
        return imageView;
    }

    /**
     *
     */
    public static void fetchFiles() {
        if (!parentDir.exists()) {
            Log.i("DCrypt", "- Directory does not exist!");
            return;
        }
        if (!parentDir.isDirectory()) {
            Log.i("DCrypt", parentDir + " Is NOT A Directory!");
            return;
        }

        imageFiles = parentDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith(".jpg") ||
                        file.getName().endsWith(".jpeg") ||
                        file.getName().endsWith(".png")) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (imageFiles == null) {
            Log.i("DCrypt", "imageFiles Is NULL!");
            return;
        }

        imageFilesId = new long[imageFiles.length];

        int i = 0;
        for (File file : imageFiles) {
            imageFilesId[i] = file.lastModified();
            i++;
        }

        LAST_INDEX = imageFiles.length - 1;

        Log.i("DCrypt", "Fetching Done!" + "\nLENGTH=" + imageFiles.length);
    }


    private Bitmap getScaledBitmap(String picturePath, int width, int height) {
        BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, sizeOptions);

        int inSampleSize = calculateInSampleSize(sizeOptions, width, height);

        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(picturePath, sizeOptions);
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}

