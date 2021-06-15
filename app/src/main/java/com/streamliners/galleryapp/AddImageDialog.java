package com.streamliners.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ActivityDialogAddImageBinding;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private ActivityDialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private AlertDialog dialog;
    String redirectUrl;


    //inflate dialog layout
    void show(Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;
        if (context instanceof GalleryActivity) {
            inflater =  ((GalleryActivity) context).getLayoutInflater();
            b = ActivityDialogAddImageBinding.inflate(
                    inflater
            );
        } else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        //create & show dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        hideErrorForEt();
        handleDimensionsInput();


    }

    //event handler
    private void hideErrorForEt() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //step1 - dimensions input
    private void handleDimensionsInput() {
        b.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get string from edit text
                String widthStr = b.width.getText().toString().trim(),
                        heightStr = b.height.getText().toString().trim();
                //guard code
                if (widthStr.isEmpty() && heightStr.isEmpty()) {
                    b.width.setError("Please enter at least one dimensions");
                    return;
                }

                //update UI
                b.imageDimensionRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);
                hideKeyboard();

                //square image
                if (widthStr.isEmpty()) {
                    int height = Integer.parseInt(heightStr);
                    fetchRandomImage(height);
                } else if (heightStr.isEmpty()) {
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }

                //rectangular image
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(height, width);
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthInput.getWindowToken(), 0);
    }

    //step2 - Fetch random image
    //rectangular image
    private void fetchRandomImage(int height, int width) {
        new ItemHelper()
                .fetchData(width, height, context, this);
                }

    //square image
    private void fetchRandomImage(int x) {
        new ItemHelper()
                .fetchData(x, context, this);
    }

    //step3 - show data
    private void showData(String redirectUrl, Set<Integer> colors, List<String> labels) {


        //loads image from glide cache
        Glide.with(context)
                .asBitmap()
                .load(redirectUrl)
                .into(b.imageView);

        //b.imageView.setImageBitmap(image);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomLabelInput();
        handleAddImageEvent();

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);
    }

    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colourChips.getCheckedChipId(), labelChipId = b.Labels.getCheckedChipId();

                //Guard code(if one of them is not checked = -1)
                if (colorChipId == -1 || labelChipId == -1) {
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label;
                //Get color and label
                if (isCustomLabel) {
                    label = b.customLabelEt.getText().toString().trim();
                    if (label.isEmpty()) {
                        Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    label = ((Chip) b.Labels.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip) b.colourChips.findViewById(colorChipId))
                        .getChipBackgroundColor().getDefaultColor();

                //Send callback
                listener.onImageAdded(new Item(redirectUrl, color, label));
                dialog.dismiss();
            }
        });
    }

//    private void handleAddImageEvent() {
//        int colorChipId = b.colourChips.getCheckedChipId();
//        int labelChipId = b.Labels.getCheckedChipId();
//
//        //Guard code
//        if (colorChipId == -1 || labelChipId == -1){
//            Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String label = null;
//
//        if (isCustomLabel){
//            label = b.customLabelEt.getText().toString().trim();
//            if (label.isEmpty()){
//                Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            else{
//                label = ((Chip) b.Labels.findViewById(labelChipId))
//                        .getText().toString();
//            }
//        }
//
//        int color = ((Chip) b.Labels.findViewById(colorChipId))
//                .getChipBackgroundColor().getDefaultColor();
//
//        //send call backs
//        listener.onImageAdded(new Item(image, color, label ));
//        dialog.dismiss();
//    }

    private void handleCustomLabelInput() {
        ChipColorBinding binding = ChipColorBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.Labels.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    b.customLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    isCustomLabel = isChecked;
            }
        });

    }

    //Label Chips
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.Labels.addView(binding.getRoot());
        }

    }

    //Color Chips
    private void inflateColorChips(Set<Integer> colors){
        for (int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colourChips.addView(binding.getRoot());

        }
    }


    @Override
    public void onFetched(String redirectUrl, Set<Integer> colors, List<String> labels) {
        showData(redirectUrl, colors, labels);
    }

    @Override
    public void onError(String error) {
        dialog.dismiss();
        listener.onError(error);
    }

    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }

}
