package com.example.test;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;


public class FaceFragment extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face, container, false);

        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.adult);
        mediaPlayer.start();

        ImageView loading = (ImageView) view.findViewById(R.id.loading);
        Glide.with(view).load(R.raw.loading_character).into(loading);

        return view;
    }
}
