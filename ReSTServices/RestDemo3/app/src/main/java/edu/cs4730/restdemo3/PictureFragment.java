package edu.cs4730.restdemo3;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class PictureFragment extends DialogFragment {

    ImageView iv;
    Bitmap bm;

    public PictureFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_picture, null);
        iv = myView.findViewById(R.id.imageView1);
        iv.setImageBitmap(bm);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Theme_AppCompat));
        builder.setView(myView);
        // request a window without the title

        Dialog dialog = builder.create();

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //don't think this is working.
        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
        return dialog;
    }

    public void setpic(Bitmap bm) {
        this.bm = bm;
    }

}
