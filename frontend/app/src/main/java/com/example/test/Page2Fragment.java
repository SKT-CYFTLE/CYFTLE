package com.example.test;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public class Page2Fragment extends Fragment {
    private TextView tale;
    private ImageView tale_image;
    private SharedViewModel sharedViewModel;
    private String url;
    public String ttsstory;
    private TtsInterface ttsapi;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page2, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        ImageView image = (ImageView) view.findViewById(R.id.tale_image);
        // 이미지 클릭하면 tts 실행
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTTSFromServer(ttsstory);
            }
        });

        tale = (TextView) view.findViewById(R.id.tale);
        // textview 스크롤 가능하게 한다
        tale.setMovementMethod(new ScrollingMovementMethod());
        // 텍스트에 본문 표시
        sharedViewModel.getText2().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                tale.setText(s);
                ttsstory = s;
            }
        });
        // 이미지뷰에 이미지 표시
        sharedViewModel.getArr().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> urlList) {
                url = urlList.get(1);

                tale_image = view.findViewById(R.id.tale_image);
                Picasso.get().load(url).into(tale_image);
            }
        });


        // timeout setting 해주기
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();

        // Retrofit으로 통신하기 위한 인스턴스 생성하기
        Retrofit junyoung = new Retrofit.Builder()
                .baseUrl("http://20.214.190.71/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        ttsapi = junyoung.create(TtsInterface.class);

        return view;
    }


    // tts를 실행하고 싶은 문장을 보내서 결과를 받아옴
    public interface TtsInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/tts_kakao/")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }

    private void getTTSFromServer(String para) {
        try {
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", para);
            // JSON 파일을 텍스트로 변환
            String jsonStory = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStory);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = ttsapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if (response.isSuccessful()) {
                        String fileUrl = "http://20.214.190.71/tts_result/kakao";
                        // mediaplayer 선언
                        MediaPlayer mediaPlayer = new MediaPlayer();

                        try {
                            // 미디어 플레이어가 가져올 url을 넣고 시작함
                            mediaPlayer.setDataSource(fileUrl);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            Log.d("tag", "Error playing audio from URL: " + e.getMessage());
                        }
                    }
                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 실패 시
                    Toast myToast = Toast.makeText(getActivity(), "실패", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
