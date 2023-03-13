package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DrawerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // 툴바 생성
        Toolbar toolbar = findViewById(R.id.drawerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView logo = (ImageView) findViewById(R.id.cyftle_logo);
        TextView text = (TextView) findViewById(R.id.cyftle_text);

        // 로고 누르면 화면 이동
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoIntent = new Intent(DrawerActivity.this, ViewActivity.class);
                startActivity(logoIntent);
            }
        });
        // 텍스트 누르면 화면 이동
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent(DrawerActivity.this, ViewActivity.class);
                startActivity(textIntent);
            }
        });
    }
    
    // 툴바 초기화
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_title, menu);
        return true;
    }
    // 툴바 버튼 기능 구현
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Log.d("test", "test");

                return true;
            case R.id.close:
                finish();
                // 액티비티가 왼쪽으로 빠지는 애니메이션
                overridePendingTransition(R.anim.anim_none, R.anim.anim_slide_out_left);

                return true;
        }
        return super .onOptionsItemSelected(item);
    }
}
