package com.example.test;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CyftleFragment extends Fragment {

    private static final int REQUEST_CODE_STT = 1;
    private TextView mSttTextView;
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cyftle, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        Button sttButton = (Button) view.findViewById(R.id.sttbtn);
        mSttTextView = view.findViewById(R.id.sttTxt);

        // 버픈 눌렀을 때 stt 실행
        sttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStt();
            }
        });

        return view;
    }

    // stt 시작
    private void startStt() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "듣고 싶은 동화의 내용을 말해주세요!");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        try {
            startActivityForResult(intent, REQUEST_CODE_STT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "언어 인식이 불가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }


    // 음성 인식 동작 관련 리스너
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_STT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = results.get(0);

            // textview 스크롤 가능하게 한다
            mSttTextView.setMovementMethod(new ScrollingMovementMethod());
            mSttTextView.setText(text);
            sharedViewModel.setStt(text);
        }
    }
}
