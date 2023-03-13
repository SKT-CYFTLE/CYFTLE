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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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


public class Page1Fragment extends Fragment {

    private TextView tale;
    private ImageView tale_image;
    private SharedViewModel sharedViewModel;
    private EngToKorInterface engapi;
    private TtsInterface ttsapi;
    private String url;
    private String ttstory;
    public Object p_id;
    public Object c_id;
    public Object ans_p_id;
    public Object ans_c_id;
    private QuestionInterface questionapi;
    private AnswerInterface answerapi;
    private List<String> question;
    private List<String> answer;
    private SummarizeInterface summaryapi;
    private AdultDalleInterface adltdalleapi;
    private ChildDalleInterface childdalleapi;
    private List<String> urlList = new ArrayList<>();
    public String camera;

    // stt로 가져온 데이터 서버로 보내기
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // 본문 서버로 보내기
        sharedViewModel.getStory().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                sendEngToServer(s);
            }
        });
        // 받은 url로 동화 만들기
        sharedViewModel.getArr().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> urlList) {
                url = urlList.get(0);

                tale_image = view.findViewById(R.id.tale_image);
                Picasso.get().load(url).into(tale_image);
            }
        });
        sharedViewModel.getPid().observe(getViewLifecycleOwner(), new Observer<Object>() {
            @Override
            public void onChanged(Object pid) {
                p_id = pid;
                Log.d("tag", "pid" + p_id);
            }
        });
        sharedViewModel.getCid().observe(getViewLifecycleOwner(), new Observer<Object>() {
            @Override
            public void onChanged(Object cid) {
                c_id = cid;
                Log.d("tag", "cid" + c_id);
            }
        });
        sharedViewModel.getCamera().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                camera = s;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page1, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        tale = (TextView) view.findViewById(R.id.tale);
        // textview 스크롤 가능하게 한다
        tale.setMovementMethod(new ScrollingMovementMethod());

        ImageView image = (ImageView) view.findViewById(R.id.tale_image);
        // 이미지 누르면 tts 통신 시작
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTTSFromServer(ttstory);
                getQuestionFromServer(p_id, c_id);
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

        // Retrofit으로 통신하기 위한 인스턴스 생성하기
        Retrofit hoonseo = new Retrofit.Builder()
                .baseUrl("http://20.249.75.188/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        engapi = junyoung.create(EngToKorInterface.class);
        ttsapi = junyoung.create(TtsInterface.class);
        questionapi = hoonseo.create(QuestionInterface.class);
        answerapi = hoonseo.create(AnswerInterface.class);
        summaryapi = hoonseo.create(SummarizeInterface.class);
        adltdalleapi = hoonseo.create(AdultDalleInterface.class);
        childdalleapi = hoonseo.create(ChildDalleInterface.class);

        return view;
    }


    // 영어 한국어로 변환해주는 인터페이스
    public interface EngToKorInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/translating/?lang=eng")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }


    // tts 인터페이스
    public interface TtsInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/tts_kakao/")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }

    // 질문 인터페이스
    public interface QuestionInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_question/")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }


    // 답변 인터페이스
    public interface AnswerInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_answer/")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }


    // Story Summarize 인터페이스
    public interface SummarizeInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/sentence_summarize/")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }


    // Dall-e 인터페이스
    public interface AdultDalleInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_image/adult")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }
    // Dall-e 인터페이스
    public interface ChildDalleInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_image/child")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }

    private void sendEngToServer(String story) {
        try {
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", story);
            // JSON 파일을 텍스트로 변환
            String jsonStory = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStory);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = engapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if (response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            String[] story = result.split("\\*\\*");

                            if (story.length == 6) {
                                for (int i = 0; i < story.length - 1; i++) {
                                    story[i] = story[i+1];
                                }
                                story = Arrays.copyOf(story, story.length - 1);

                            } else if(story.length != 5) {
                                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.retry);
                                mediaPlayer.start();
                            }

                            Log.d("tag", "단락1:" + story[0]);
                            Log.d("tag", "단락2:" + story[1]);
                            Log.d("tag", "단락3:" + story[2]);
                            Log.d("tag", "단락4:" + story[3]);
                            Log.d("tag", "단락5:" + story[4]);

                            // 텍스트 뷰에 텍스트 띠우기
                            tale.setText(story[0]);
                            ttstory = story[0];
                            // sharedViewModel에 저장하기
                            sharedViewModel.setText2(story[1]);
                            sharedViewModel.setText3(story[2]);
                            sharedViewModel.setText4(story[3]);
                            sharedViewModel.setText5(story[4]);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast myToast = Toast.makeText(getActivity(), "에러", Toast.LENGTH_SHORT);
                        myToast.show();
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
    // id값을 전달해서 질문을 받아오는 기능
    private void getQuestionFromServer(Object pid, Object cid) {
        try{
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("p_id", pid);
            jsonObject.put("c_id", cid);
            // JSON 파일을 텍스트로 변환
            String jsonStory = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStory);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = questionapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            Log.d("tag", "" + result);

                            // question 부분 포매팅
                            String question_string = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"question\\\\", "\"question")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\", "")
                                    .replaceAll("\"\"", "")
                                    .replaceAll(" ,", "");
                            ObjectMapper quest = new ObjectMapper();
                            Map<String, Object> questmap = quest.readValue(question_string, Map.class);
                            Object questions = questmap.get("question");

                            Object pid = questmap.get("p_id");
                            Object cid = questmap.get("c_id");

                            getAnswerFromServer(pid, cid);
                            question = quest.convertValue(questions, new TypeReference<ArrayList<String>>() {});

                            if (question.size() == 4) {
                                question.remove(0);
                            }
                            sharedViewModel.setQuestion(question);
                            Log.d("tag", "quest:" + question);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        errorFragment.show(ft, "error");
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 실패 시
                    Toast myToast = Toast.makeText(getActivity(),"실패", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getAnswerFromServer(Object pid, Object cid) {
        try{
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("p_id", pid);
            jsonObject.put("c_id", cid);
            // JSON 파일을 텍스트로 변환
            String jsonStory = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStory);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = answerapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            Log.d("tag", "" + result);

                            // answer 부분 포매팅
                            String answer_string = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"answer\\\\", "\"answer")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\", "")
                                    .replaceAll("\"\"", "")
                                    .replaceAll(" ,", "");
                            ObjectMapper ans = new ObjectMapper();
                            Map<String, Object> ansmap = ans.readValue(answer_string, Map.class);
                            Object answers = ansmap.get("answer");

                            ans_p_id = ansmap.get("p_id");
                            ans_c_id = ansmap.get("c_id");

                            answer = ans.convertValue(answers, new TypeReference<ArrayList<String>>() {});
                            if (answer.size() == 4) {
                                answer.remove(0);
                            }
                            sharedViewModel.setAnswer(answer);

                            sendAnswerToServer(ans_p_id, ans_c_id);
                            Log.d("tag", "ans:" + answer);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        errorFragment.show(ft, "error");
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 실패 시
                    Toast myToast = Toast.makeText(getActivity(),"실패", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 생성된 동화의 id값을 보내고 요약된 결과를 받아옴
    private void sendAnswerToServer(Object pid, Object cid) {
        try {
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("p_id", pid);
            jsonObject.put("c_id", cid);
            // JSON 파일을 텍스트로 변환
            String jsonStory = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStory);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = summaryapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if (response.isSuccessful()) {
                        try {
                            String result = response.body().string();

                            // 받아온 결과를 사용할 수 있는 형태로 포매팅
                            String summ = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"summarized\\\\", "\"summarized")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\", "")
                                    .replaceAll("\"\"", "")
                                    .replaceAll(" ,", "");

                            Log.d("tag", "" + summ);
                            ObjectMapper object = new ObjectMapper();
                            Map<String, Object> map = object.readValue(summ, Map.class);
                            Object sum = map.get("summarized");

                            Log.d("tag", "sentence 요약: " + sum);

                            ArrayList<String> summArray = object.convertValue(sum, new TypeReference<ArrayList<String>>() {});

                            if(camera.equals("children")) {
                                sendChildSumToServer(summArray.get(0));
                                sendChildSumToServer(summArray.get(1));
                                sendChildSumToServer(summArray.get(2));
                            } else if (camera.equals("adult")) {
                                sendAdultSumToServer(summArray.get(0));
                                sendAdultSumToServer(summArray.get(1));
                                sendAdultSumToServer(summArray.get(2));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        errorFragment.show(ft, "error");
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
    private void sendAdultSumToServer(String summary) {
        try{
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", summary);
            // JSON 파일을 텍스트로 변환
            String jsonStt = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStt);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = adltdalleapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            Log.d("tag", "문장 달리 : " + result);

                            ObjectMapper object = new ObjectMapper();
                            JsonNode root = object.readTree(result);
                            String url = root.get("url").asText();

                            urlList.add(url);

                            if (urlList.size() == 3){
                                sharedViewModel.setUrl(urlList);
                            }

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        errorFragment.show(ft, "error");
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 실패 시
                    Toast myToast = Toast.makeText(getActivity(),"실패", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void sendChildSumToServer(String summary) {
        try{
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", summary);
            // JSON 파일을 텍스트로 변환
            String jsonStt = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStt);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = childdalleapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            Log.d("tag", "문장 달리 : " + result);

                            ObjectMapper object = new ObjectMapper();
                            JsonNode root = object.readTree(result);
                            String url = root.get("url").asText();

                            urlList.add(url);

                            if (urlList.size() == 3){
                                sharedViewModel.setUrl(urlList);
                            }

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorFragment errorFragment = new ErrorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        errorFragment.show(ft, "error");
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 실패 시
                    Toast myToast = Toast.makeText(getActivity(),"실패", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

