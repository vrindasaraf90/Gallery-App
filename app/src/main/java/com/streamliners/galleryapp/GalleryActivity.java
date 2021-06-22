package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.streamliners.galleryapp.databinding.ActivityDialogAddImageBinding;
import com.streamliners.galleryapp.databinding.ActivityDemoBinding;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;
import com.streamliners.galleryapp.ItemHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {
    ActivityDemoBinding b;
    private static final int PICK_IMAGE = 0;
    SharedPreferences sharedPrefs;
    List<Item> itemList = new ArrayList<>();
    ItemCardBinding binding;
    Context context = this;
    static final Integer WRITE_EXIST = 0x3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDemoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>")));

        sharedPrefs = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreference();


        //floatingActionButton();

//        new ItemHelper()
//                .fetchData(1090, 1080, this, new ItemHelper.OnCompleteListener() {
//                    @Override
//                    public void onFetched(Bitmap image, Set<Integer> colors, List<String> labels) {
//                        b.imageView.setImageBitmap(image);
//                        inflateColorChips(colors);
//                        inflateLabelChips(labels);
//                    }
//
//                    @Override
//                    public void onError(String error) {
//                        new MaterialAlertDialogBuilder(GalleryActivity.this)
//                                .setTitle("Error")
//                                .setMessage(error)
//                                .show();
//                    }
//                });

    }

    //result for storage permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 3) {
                shareImage();
            }
        }
        else Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
    }

    //ask for storage permission
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(GalleryActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{permission}, requestCode);
        } else {
            shareImage();

        }
    }

    // intent to open gallery
    private void showAddImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    //share image
    private void shareImage() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, "Share image via"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery, menu);
        return true;
    }



    //Action Menu Methods

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImage = data.getData();
                String uri = selectedImage.toString();
                new AddImageDialog().fetchDataForGallery(uri, this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        itemList.add(item);

                    }

                    @Override
                    public void onError(String error) {

                    }
                });
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_image) {
            showAddImageDialog();
            return true;
        } else if (item.getItemId() == R.id.addImageFromGallery) {
            showAddImageFromGallery();
            return true;
        }

        return false;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.shareImage) {
            askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXIST);
            return true;
        } else if (item.getItemId() == R.id.edit) {
            return true;
        }

        return true;
    }


    //on pause to save data in shared preference
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int numOfImg = itemList.size();
        editor.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : itemList) {
            editor.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, item.url)
                    .apply();
            counter++;
        }
        editor.commit();

    }
    
    //get data from shared preference
    void getDataFromSharedPreference() {
        int itemCount = sharedPrefs.getInt(Constants.NUMOFIMG, 0);
        if (itemCount > 0) {
        }
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(sharedPrefs.getString(Constants.IMAGE + i, "")
                    , sharedPrefs.getInt(Constants.COLOR + i, 0)
                    , sharedPrefs.getString(Constants.LABEL + i, ""));

            itemList.add(item);
        }
    }

    private void showAddImageDialog() {

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        inflateViewForItem(item);
                        b.noItems.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();

                    }
                });

    }

    private void inflateViewForItem(Item item) {

        //inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());


        //bind data
        Glide.with(this)
                .asBitmap()
                .load(item.url)
                .into(binding.imageView);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //adding to the list
        b.list.addView(binding.getRoot());
    }


    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

//  private void loadImage() {
//       Glide.with(this)
//               .asBitmap()
//               .load("https://picsum.photos/1080")
//               .diskCacheStrategy(DiskCacheStrategy.NONE)
//               .skipMemoryCache(true)
//               .listener(new RequestListener<Bitmap>() {
//                   @Override
//                   public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                       b.loader.setVisibility(View.GONE);
//                       b.subtitle.setText(getString(R.string.image_load_failed, e.toString()));
//                       return true;
//                  }
//
//                  @Override
//                  public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                     b.loader.setVisibility(View.GONE);
//                     b.subtitle.setText(R.string.image_loaded);
//                     b.imageView.setImageBitmap(resource);
//                     createPaletteAsync(resource);
//                     labelImage(resource);
//                     return true;
//                    }
//                })
//                .into(b.imageView);
// }

    private void labelImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Labels Fetched")
                                .setMessage(labels.toString())
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(e.toString())
                                .show();
                    }
                });


    }

    public void createPaletteAsync(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                new MaterialAlertDialogBuilder(GalleryActivity.this)
                        .setTitle("Palette Fetched")
                        .setMessage(p.getSwatches().toString())
                        .show();
            }
        });
    }

    private void showCustomThemeDialog(){
        new MaterialAlertDialogBuilder(GalleryActivity.this, R.style.CustomDialogTheme)
                .setTitle("Congratulations")
                .setMessage("You won lottery of 10000000000!")
                .show();

    }

    private void showSimpleDialog() {
        new MaterialAlertDialogBuilder(GalleryActivity.this)
            .setTitle("Congratulations")
                .setMessage("You won lottery of 10000000000!")
                .setPositiveButton("REDEEM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GalleryActivity.this, "redeemed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("DISAGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GalleryActivity.this, "disagreed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("PROVE IT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GalleryActivity.this, "You asked for a proof!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    private void showCustomViewDialog(){
        ActivityDialogAddImageBinding binding = ActivityDialogAddImageBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(GalleryActivity.this)
                .setView(binding.getRoot())
                .show();
    }

    private void inflateLabelChips(List<String> labels) {
        for (String label : labels){
            ChipColorBinding binding = ChipColorBinding.inflate(getLayoutInflater());
            binding.getRoot().setText(label);
            b.Labels.addView(binding.getRoot());
        }

    }

    private void inflateColorChips(Set<Integer> colors){
        for (int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(getLayoutInflater());
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colourChips.addView(binding.getRoot());

        }
    }

    private void testDialog() {
        ActivityDialogAddImageBinding binding = ActivityDialogAddImageBinding.inflate(getLayoutInflater());

        AlertDialog dialog =new MaterialAlertDialogBuilder(this)
                .setView(binding.getRoot())
                .show();

        binding.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.imageDimensionRoot.setVisibility(View.GONE);
                binding.progressIndicatorRoot.setVisibility(View.VISIBLE);


                new Handler()
                        .postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressIndicatorRoot.setVisibility(View.GONE);
                                binding.mainRoot.setVisibility(View.VISIBLE);

                            }
                        },2000);


            }
        });

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}