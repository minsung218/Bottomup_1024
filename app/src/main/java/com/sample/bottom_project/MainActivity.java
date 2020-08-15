package com.sample.bottom_project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // SharedPreferences 정의
    private SharedPreferences SPreferences;

    // SharedPreferences 접근 이름, 저장 데이터 초기화
    private final String NameSPreferences = "Day";
    private String strSDFormatDay = "0";

    private Context mContext;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ContentsPagerAdapter mContentsPagerAdapter;

    PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(getApplicationContext(),"권한을 허용하셨습니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // '오늘 그만 보기' 기능을 위한 날짜 획득
        long CurrentTime = System.currentTimeMillis(); // 현재 시간을 msec 단위로 얻음
        Date TodayDate = new Date(CurrentTime); // 현재 시간 Date 변수에 저장
        SimpleDateFormat SDFormat = new SimpleDateFormat("dd");
        strSDFormatDay = SDFormat.format(TodayDate); // 'dd' 형태로 포맷 변경

        // SharedPreferences 획득
        SPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strSPreferencesDay = SPreferences.getString(NameSPreferences, "0");

        // 공지사항 알림창 띄움
        if((Integer.parseInt(strSDFormatDay) - Integer.parseInt(strSPreferencesDay)) != 0)
            StartMainAlertDialog();

        int W_permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int R_permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        mContext = getApplicationContext();
        mTabLayout = (TabLayout) findViewById(R.id.layout_tab);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(createTabView("키워드 검색")));
        mViewPager = (ViewPager) findViewById(R.id.pager_content);
        mContentsPagerAdapter = new ContentsPagerAdapter(
                getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(mContentsPagerAdapter);
        mViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(W_permissonCheck != PackageManager.PERMISSION_GRANTED || R_permissonCheck != PackageManager.PERMISSION_GRANTED) {
            TedPermission.with(getApplicationContext())
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("워드클라우드 이미지 저장을 위해 저장공간 권한이 필요합니다.")
                    .setDeniedMessage("권한이 거부되었습니다.")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        }

    }

    // 초기 실행시 도움말로 유도하는 알림창
    public void StartMainAlertDialog() {
        AlertDialog.Builder MainAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);

        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        final View view = factory.inflate(R.layout.alert_dialog, null);
        MainAlertDialog.setView(view);

        // 뒤로가기, 배경터치 무시
        MainAlertDialog.setCancelable(false);
        // positive 버튼 설정
        MainAlertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            // Positive Button에 대한 클릭 이벤트 처리를 구현
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // dialog 닫기
            }
        });
        // Neutral 버튼 설정
        MainAlertDialog.setNeutralButton("오늘 그만 보기", new
                DialogInterface.OnClickListener() {
                    // Neutral Button에 대한 클릭 이벤트 처리를 구현
                    @Override public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor SPreferencesEditor = SPreferences.edit();
                        SPreferencesEditor.putString(NameSPreferences, strSDFormatDay); // 오늘 '일(day)' 저장
                        SPreferencesEditor.commit(); // important to save the preference
                        dialog.dismiss(); // dialog 닫기
                    }
                });
        // AlertDialog 화면 출력
        MainAlertDialog.show();
    }

    private View createTabView(String tabName) {
        View tabView = LayoutInflater.from(mContext).inflate(R.layout.custom_tab, null);
        TextView txt_name = (TextView) tabView.findViewById(R.id.txt_name);
        txt_name.setText(tabName);
        return tabView;
    }
    private long time= 0;
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            finish();
        }
    }
    public void onButtonClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ImgDownActivity.class);
        startActivity(intent);
    }

}