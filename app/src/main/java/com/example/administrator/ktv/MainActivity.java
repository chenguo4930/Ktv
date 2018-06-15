package com.example.administrator.ktv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lrcview.LrcView;
import com.example.lrcview.util.LrcUtil;

import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    private TextView mTvLrc;
    private Button mBtnDecord;
    private LrcView lrcView, lrcView2;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvLrc = findViewById(R.id.tv_lrc);
        mBtnDecord = findViewById(R.id.btn_decord_txt);
        lrcView = findViewById(R.id.lrc_view);
        lrcView2 = findViewById(R.id.lrc_view2);
        mBtnDecord.setOnClickListener(this);

        checkPermission();
    }

    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            Toast.makeText(this, "读取授权成功！", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "checkPermission: 读取授权成功！");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_decord_txt:
                decodeTxt();
                break;
            default:
        }
    }

    /**
     * 解析歌词文件为String
     */
    private void decodeTxt() {

//        String path = Environment.getExternalStorageDirectory().getPath() + "/Download/textLrc.txt ";
        String pathLrc = "/storage/sdcard0/Download/textLrc.txt";
        String pathMp3 = "/storage/sdcard0/Download/花一开满就相爱.mp3";
        Log.e(TAG, "-------path" + pathLrc);
        File fileLrc = new File(pathLrc);
        File fileMp3 = new File(pathMp3);
        if (!fileLrc.exists()) {
            Toast.makeText(this, "歌词文件不存在", Toast.LENGTH_SHORT).show();
        } else if (!fileMp3.exists()) {
            Toast.makeText(this, "MP3音乐文件不存在", Toast.LENGTH_SHORT).show();
        } else {
            try {
                String lrcString = Utile.readTxt(pathLrc);
//                Log.e(TAG, "-------lrcString=" + lrcString);

//                mTvLrc.setText(lrcString);
//                LrcUtil.parseStr2List(lrcString);
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                mMediaPlayer.setDataSource(pathMp3);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                // 通过异步的方式装载媒体资源
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                });
                mMediaPlayer.setOnCompletionListener(mp -> {
                    Log.e(TAG, "------setOnCompletionListener--播放完成-");
                });
                mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "------setOnErrorListener--播放出错-");
                    return false;
                });

                lrcView.setLrc(lrcString);
                lrcView.setPlayer(mMediaPlayer);
                lrcView.setMode(LrcView.MODE_KTV);
                lrcView.setIsPlayer(true);
                lrcView.init();

                lrcView2.setLrc(lrcString);
                lrcView2.setPlayer(mMediaPlayer);
                lrcView2.setMode(LrcView.MODE_NORMAL);
                lrcView2.setIsPlayer(false);
                lrcView2.init();

                Toast.makeText(this, "解析成功", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "-------IOException=" + e);
            }
        }
    }
}
