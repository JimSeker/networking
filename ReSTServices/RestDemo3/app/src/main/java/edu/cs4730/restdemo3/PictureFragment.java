package edu.cs4730.restdemo3;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import edu.cs4730.restdemo3.databinding.FragmentPictureBinding;

/**
 * simple frameless dialog to display the picture.
 */
public class PictureFragment extends DialogFragment {

    Bitmap bm;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        // Inflate the layout for this fragment
        FragmentPictureBinding binding = FragmentPictureBinding.inflate(inflater);

        binding.imageView1.setImageBitmap(bm);
        binding.imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireActivity(), R.style.Theme_AppCompat));
        builder.setView(binding.getRoot());

        Dialog dialog = builder.create();
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
        return dialog;
    }

    public void setpic(Bitmap bm) {
        this.bm = bm;
    }

}
