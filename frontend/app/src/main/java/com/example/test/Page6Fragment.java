package com.example.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class Page6Fragment extends Fragment {

    private SharedViewModel sharedViewModel;
    public String quest1;
    public String quest2;
    public String quest3;
    public String ans1;
    public String ans2;
    public String ans3;
    public String url1;
    public String url2;
    public String url3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page6, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getQuestion().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> question) {
                quest1 = question.get(0);
                quest2 = question.get(1);
                quest3 = question.get(2);

                TextView qust1 = (TextView) view.findViewById(R.id.quest1);
                TextView qust2 = (TextView) view.findViewById(R.id.quest2);
                TextView qust3 = (TextView) view.findViewById(R.id.quest3);

                qust1.setText("Q1. " + quest1);
                qust2.setText("Q2. " + quest2);
                qust3.setText("Q3. " + quest3);

                qust1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedViewModel.setAns(ans1);
                        sharedViewModel.setImage(url1);
                    }
                });
                qust2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedViewModel.setAns(ans2);
                        sharedViewModel.setImage(url2);
                    }
                });
                qust3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedViewModel.setAns(ans3);
                        sharedViewModel.setImage(url3);
                    }
                });
            }
        });

        sharedViewModel.getAnswer().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> answer) {
                ans1 = answer.get(0);
                ans2 = answer.get(1);
                ans3 = answer.get(2);
            }
        });

        sharedViewModel.getUrl().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> urls) {
                url1 = urls.get(0);
                url2 = urls.get(1);
                url3 = urls.get(2);
            }
        });

        return view;
    }
}
