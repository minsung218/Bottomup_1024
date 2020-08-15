package com.sample.bottom_project;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.InputStream;
import java.net.URL;

public class ImgDownActivity extends AppCompatActivity {

    Handler handler = new Handler();
    private Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_down);

        // 워드클라우드 이미지 주소
        final String addr = "http://192.168.0.3:5000/static/wordcloud.png";
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(addr);
                Toast.makeText(ImgDownActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        // 워드 클라우드 보여주기
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final ImageView iv = (ImageView)findViewById(R.id.img);
                    URL url = new URL("http://192.168.0.3:5000/static/wordcloud.png");
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(bm);
                        }
                    });
                    iv.setImageBitmap(bm);
                } catch(Exception e){

                }
            }
        });
        t.start();
    }

    // url 주소 이미지 저장
    public void downloadFile(String url) {
        DownloadManager mgr = (DownloadManager) ImgDownActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Sample")
                .setDescription("Something useful. No, Really")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "wordCloud.jpg");
        mgr.enqueue(request);
    }

    public void onBackButtonClicked(View v) {
        finish();
    }
}