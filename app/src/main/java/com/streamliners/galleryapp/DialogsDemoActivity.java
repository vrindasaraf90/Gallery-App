package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.streamliners.galleryapp.databinding.ActivityDialogAddImageBinding;
import com.streamliners.galleryapp.databinding.ActivityDialogsDemoBinding;

import java.util.List;

public class DialogsDemoActivity extends AppCompatActivity {
    ActivityDialogsDemoBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDialogsDemoBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_dialogs_demo);

      //  showSimpleDialog();
      //  showCustomViewDialog();
      //  showCustomThemeDialog();
        loadImage();
    }

    //Action Menu Methods


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh){
            refresh();
            return true;
        }
        return false;
    }

    private void refresh() {
        finish();
        startActivity(new Intent(this, DialogsDemoActivity.class));

    }

    private void loadImage(){
        Glide.with(this)
                .asBitmap()
                .load("https://picsum.photos/1080")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
//                .listener(new RequestListener<Bitmap>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                        b.loader.setVisibility(View.GONE);
//                        b.subtitle.setText(getString(R.string.image_load_failed, e.toString()));
//                        return true;
//
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                        b.loader.setVisibility(View.GONE);
//                        b.subtitle.setText(R.string.image_loaded);
//
//
//                        b.imageView.setImageBitmap(resource);
//
//                        labelImage(resource);
//                        return true;
//                    }
//                })

                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable  Transition<? super Bitmap> transition) {
                        b.loader.setVisibility(View.GONE);
                        //b.subtitle.setText(R.string.image_loaded);
                        b.subtitle.setVisibility(View.GONE);
                        b.imageView.setVisibility(View.VISIBLE);
                        b.imageView.setImageBitmap(resource);

                        labelImage(resource);


                    }

                    @Override
                    public void onLoadCleared(@Nullable  Drawable placeholder) {

                    }
                });
    }

    private void labelImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        new MaterialAlertDialogBuilder(DialogsDemoActivity.this)
                                .setTitle("Labels Fetched")
                                .setMessage(labels.toString())
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new MaterialAlertDialogBuilder(DialogsDemoActivity.this)
                                .setTitle("Error")
                                .setMessage(e.toString())
                                .show();
                    }
                });


    }

    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                // Use generated instance
            }
        });
    }

    private void showCustomThemeDialog(){
        new MaterialAlertDialogBuilder(DialogsDemoActivity.this, R.style.CustomDialogTheme)
                .setTitle("Congratulations")
                .setMessage("You won lottery of 10000000000!")
                .show();

    }

    private void showSimpleDialog() {
        new MaterialAlertDialogBuilder(DialogsDemoActivity.this)
            .setTitle("Congratulations")
                .setMessage("You won lottery of 10000000000!")
                .setPositiveButton("REDEEM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DialogsDemoActivity.this, "redeemed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DialogsDemoActivity.this, "disagreed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("PROVE IT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(DialogsDemoActivity.this, "You asked for a proof!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    private void showCustomViewDialog(){
        ActivityDialogAddImageBinding binding = ActivityDialogAddImageBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(DialogsDemoActivity.this)
                .setView(binding.getRoot())
                .show();
    }
}