package com.example.test;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

public class TaleMakeFragment extends Fragment {

    private KorTOEngInterface korapi;
    private AdultStoryInterface adltstoryapi;
    private ChildrenStoryInterface childstoryapi;
    private SummarizeInterface summarizeapi;
    private AdultDalleInterface adltdalleapi;
    private ChildDalleInterface childdalleapi;
    private SharedViewModel sharedViewModel;
    private Button taleBtn;
    private String full;
    private Object full_p_id;
    private Object full_c_id;
    private Object sum_p_id;
    private Object sum_c_id;
    private List<String> urlList = new ArrayList<>();
    private LoadingFragment loadingFragment = new LoadingFragment();
    public String camera;

    // stt로 가져온 데이터 서버로 보내기
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // stt 받아왔을 때 버튼을 누르면 해당 stt 텍스트를 서버에 전송해줌
        sharedViewModel.getStt().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                taleBtn = (Button) view.findViewById(R.id.talebtn);
                taleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendKorToServer(s);

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        loadingFragment.show(ft, "loading");
                    }
                });
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
        View view = inflater.inflate(R.layout.fragment_talemake, container, false);

        // timeout setting 해주기
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(3000, TimeUnit.SECONDS)
                .readTimeout(3000, TimeUnit.SECONDS)
                .writeTimeout(3000, TimeUnit.SECONDS)
                .build();

        // Retrofit으로 통신하기 위한 인스턴스 생성하기
        Retrofit junyoung = new Retrofit.Builder()
                .baseUrl("http://20.214.190.71/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        // api interface 인스턴스 생성
        korapi = junyoung.create(KorTOEngInterface.class);
        adltstoryapi = junyoung.create(AdultStoryInterface.class);
        childstoryapi = junyoung.create(ChildrenStoryInterface.class);
        summarizeapi = junyoung.create(SummarizeInterface.class);
        adltdalleapi = junyoung.create(AdultDalleInterface.class);
        childdalleapi = junyoung.create(ChildDalleInterface.class);

        return view;
    }


    // 한국어 영어로 변환해주는 인터페이스
    public interface KorTOEngInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/translating/?lang=kor")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }



    // Make Story 인터페이스
    public interface AdultStoryInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_story/adult")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }
    // Make Story 인터페이스
    public interface ChildrenStoryInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/make_story/children")
        Call<ResponseBody> sendText(@Body RequestBody requestBody);
    }

    // Story Summarize 인터페이스
    public interface SummarizeInterface {
        @Headers({"Content-Type: application/json"})
        @POST("/story_summarize/")
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

    // 한국어를 서버로 보내서 영어로 번역된 것 받아옴
    private void sendKorToServer(String stt) {
        try{
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", stt);
            // JSON 파일을 텍스트로 변환
            String jsonStt = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonStt);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = korapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();
                            // 번역된 결과 서버로 보냄
                            if(camera.equals("children")) {
                                sendAdultToServer(result);
                            } else if (camera.equals("adult")) {
                                sendChildToServer(result);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        loadingFragment.dismiss();
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
    // 영어로 번역된 것 서버로 보내서 ChatGPT로부터 동화를 생성 받음
    private void sendAdultToServer(String trans) {
        try {
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", trans);
            // JSON 파일을 텍스트로 변환
            String jsonTrans = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonTrans);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = adltstoryapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if (response.isSuccessful()) {
                        try {
                            String result = response.body().string();

                            Log.d("tag", "" + result);
                            // 받아온 결과를 쓸 수 있는 형태로 리포매팅
                            String rest = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\n\\\\\\\\n", "**")
                                    .replaceAll("\\\\\\\\", "");

                            int idx = rest.indexOf("\"p_id");
                            rest = "{\"full\": \"" + rest.substring(10, idx-3).replace("\"", "@") + "\", " + rest.substring(idx);

                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> map = objectMapper.readValue(rest, Map.class);
                            Object value = map.get("full");

                            full_p_id = map.get("p_id");
                            full_c_id = map.get("c_id");

                            full = value.toString().replaceAll("@", "\"");
                            sharedViewModel.setStory(full);
                            // 요약문을 만들어 달라고 id 값들을 전달함
                            sendStoryToServer(full_p_id, full_c_id);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        loadingFragment.dismiss();
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
    private void sendChildToServer(String trans) {
        try {
            // json 파일 만들기
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", trans);
            // JSON 파일을 텍스트로 변환
            String jsonTrans = jsonObject.toString();
            // request body를 json 포맷 텍스트로 생성한다
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonTrans);

            // 데이터 서버로 보내기
            Call<ResponseBody> call = childstoryapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if (response.isSuccessful()) {
                        try {
                            String result = response.body().string();

                            Log.d("tag", "" + result);
                            // 받아온 결과를 쓸 수 있는 형태로 리포매팅
                            String rest = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\n\\\\\\\\n", "**")
                                    .replaceAll("\\\\\\\\", "");

                            int idx = rest.indexOf("\"p_id");
                            rest = "{\"full\": \"" + rest.substring(10, idx-3).replace("\"", "@") + "\", " + rest.substring(idx);

                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, Object> map = objectMapper.readValue(rest, Map.class);
                            Object value = map.get("full");

                            full_p_id = map.get("p_id");
                            full_c_id = map.get("c_id");

                            full = value.toString().replaceAll("@", "\"");
                            sharedViewModel.setStory(full);
                            // 요약문을 만들어 달라고 id 값들을 전달함
                            sendStoryToServer(full_p_id, full_c_id);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        loadingFragment.dismiss();
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
    // 생성된 동화의 id값을 보내고 요약된 결과를 받아옴
    private void sendStoryToServer(Object pid, Object cid) {
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
            Call<ResponseBody> call = summarizeapi.sendText(requestBody);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공하면 해야할 반응
                    if(response.isSuccessful()) {
                        try {
                            String result = response.body().string();

                            // 받아온 결과를 사용할 수 있는 형태로 포매팅
                            String summ = result.replaceAll("^\"|\"$", "")
                                    .replaceAll("\\\\\"summarized\\\\", "\"summarized")
                                    .replaceAll("\\\\\"", "\"")
                                    .replaceAll("\\\\\\\\", "")
                                    .replaceAll("\"\"","")
                                    .replaceAll(" ,", "");

                            Log.d("tag", "" + summ);
                            ObjectMapper object = new ObjectMapper();
                            Map<String, Object> map = object.readValue(summ, Map.class);
                            Object sum = map.get("summarized");

                            sum_p_id = map.get("p_id");
                            sum_c_id = map.get("c_id");

                            sharedViewModel.setPid(sum_p_id);
                            sharedViewModel.setCid(sum_c_id);

                            // 배열에 요약문들을 담아둠
                            ArrayList<String> summArray = object.convertValue(sum, new TypeReference<ArrayList<String>>() {});

                            if(camera.equals("children")) {
                                sendChildSumToServer(summArray.get(0));
                                sendChildSumToServer(summArray.get(1));
                                sendChildSumToServer(summArray.get(2));
                                sendChildSumToServer(summArray.get(3));
                                sendChildSumToServer(summArray.get(4));
                            } else if (camera.equals("adult")) {
                                sendAdultSumToServer(summArray.get(0));
                                sendAdultSumToServer(summArray.get(1));
                                sendAdultSumToServer(summArray.get(2));
                                sendAdultSumToServer(summArray.get(3));
                                sendAdultSumToServer(summArray.get(4));
                            }

                            Log.d("tag", "요약1:" + summArray.get(0));
                            Log.d("tag", "요약2:" + summArray.get(1));
                            Log.d("tag", "요약3:" + summArray.get(2));
                            Log.d("tag", "요약4:" + summArray.get(3));
                            Log.d("tag", "요약5:" + summArray.get(4));
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        loadingFragment.dismiss();
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
    // 요약문들을 달리로 보내서 이미지 url을 받아옴
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

                            Log.d("tag", "문단 달리 : " + result);

                            ObjectMapper object = new ObjectMapper();
                            JsonNode root = object.readTree(result);
                            String url = root.get("url").asText();

                            urlList.add(url);

                            if (urlList.size() == 5){
                                sharedViewModel.setArr(urlList);

                                loadingFragment.dismiss();
                                DoneFragment doneFragment = new DoneFragment();
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                doneFragment.show(ft, "done");
                            } else if (urlList.size() == 6) {
                                urlList.remove(0);

                                sharedViewModel.setArr(urlList);
                                loadingFragment.dismiss();
                                DoneFragment doneFragment = new DoneFragment();
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                doneFragment.show(ft, "done");
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        loadingFragment.dismiss();
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
    // 요약문들을 달리로 보내서 이미지 url을 받아옴
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

                            Log.d("tag", "문단 달리 : " + result);

                            ObjectMapper object = new ObjectMapper();
                            JsonNode root = object.readTree(result);
                            String url = root.get("url").asText();

                            urlList.add(url);

                            if (urlList.size() == 5){
                                sharedViewModel.setArr(urlList);

                                loadingFragment.dismiss();
                                DoneFragment doneFragment = new DoneFragment();
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                doneFragment.show(ft, "done");
                            } else if (urlList.size() == 6) {
                                urlList.remove(0);

                                sharedViewModel.setArr(urlList);
                                loadingFragment.dismiss();
                                DoneFragment doneFragment = new DoneFragment();
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                doneFragment.show(ft, "done");
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        loadingFragment.dismiss();
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