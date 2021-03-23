package com.example.imageediting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ImageEditScreen extends AppCompatActivity {

    ImageView imageView;
    Button undo, crop, rotate, save;

    String imagePathString;
    Bitmap imageBitmap;
    Uri uri;
    Intent CropIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit_screen);
        imageView = findViewById(R.id.editImagePreview);
        undo = findViewById(R.id.undo);
        crop = findViewById(R.id.crop);
        rotate = findViewById(R.id.rotate);
        save = findViewById(R.id.save);
        getImageData();
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setRotation(imageView.getRotation() + 90);
            }
        });
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uri = getImageUri(ImageEditScreen.this, imageBitmap);
                imageCrop();
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageDrawable(null);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImage(imageBitmap);
                Intent intent = new Intent(ImageEditScreen.this, MainActivity.class);
                String imageptha = BitMapToString(imageBitmap);
                intent.putExtra("processed_image_path_string", imageptha);
                startActivity(intent);
            }
        });
    }

    public void imageCrop() {
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            CropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            CropIntent.setDataAndType(uri, "image/*");
            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);
            startActivityForResult(CropIntent, 1);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Error is :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getImageData() {
        imagePathString = getIntent().getStringExtra("image_path_string");
        imageBitmap = (Bitmap) getIntent().getParcelableExtra("compress_bitmap");
        Log.e("=========", "Image BitMap is :" + imageBitmap + "   Image Path in String is :" + imagePathString);
        imageBitmap = StringToBitMap(imagePathString);
        imageView.setImageBitmap(imageBitmap);
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_" + Calendar.getInstance().getTime(), null);
        return Uri.parse(path);
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("TAG",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("TAG", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("TAG", "Error accessing file: " + e.getMessage());
        }
        Log.e("========", "Finally Path is :" + pictureFile);
        Toast.makeText(this, "Path is :" + pictureFile, Toast.LENGTH_SHORT).show();
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}