package com.example.test;

import android.media.AudioManager;
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


public class Page7Fragment extends Fragment {

    private SharedViewModel sharedViewModel;
    public ImageView image;
    public String ans;
    private TtsInterface ttsapi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page7, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getAns().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                ans = s;
                TextView answer = (TextView) view.findViewById(R.id.tale);
                answer.setText(s);
                // textview 스크롤 가능하게 한다
                answer.setMovementMethod(new ScrollingMovementMethod());
            }
        });
        sharedViewModel.getImage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d("tag", "그릴 url: " +s);
                image = (ImageView) view.findViewById(R.id.tale_image);
                Picasso.get().load(s).into(image);

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getTTSFromServer(ans);
                    }
                });
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

    // tts 인터페이스
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
                        MediaPlayer mediaPlayer = new MediaPlayer();

                        try {
                            mediaPlayer.setDataSource(fileUrl);
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
