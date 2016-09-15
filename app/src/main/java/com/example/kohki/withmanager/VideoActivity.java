package com.example.kohki.withmanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Kohki on 2016/06/28.
 */
public class VideoActivity extends Activity {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;

    private Camera mCamera;
    private final static String TAG = "VideoActivity";
    private Context context;
    private int movie_time = 5000;

    //    private String sava_path  = "/storage/emulated/legacy/WithManager/";
    private String sava_dir = "sdcard/WithManager/";

    private VideoRecorder mRecorder = null;

    public static SurfaceView mOverLaySurfaceView;
    public static SurfaceHolder mOverLayHolder;
    public static PreviewSurfaceViewCallback mPreviewCallback;

    private boolean  is_playing;

    private EventLogger mEventLogger;

    public static int[] who_is_acter = {-1,-1};
    //[0] is team.-1:? 0:myteam 1:enemyteam
    //[1] is number, -1 is ? 4...

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_standalone);

        context = this;

        //main surfaceview
        SurfaceView main_surface = (SurfaceView) findViewById(R.id.main_surface);
        mRecorder = new VideoRecorder(this, movie_time, sava_dir, main_surface, getResources());

        //sub surfaceview
        mOverLaySurfaceView = (SurfaceView) findViewById(R.id.sub_surface);
        mOverLayHolder = mOverLaySurfaceView.getHolder();
        mOverLayHolder.setFormat(PixelFormat.TRANSLUCENT);//ここで半透明にする
        mPreviewCallback = new PreviewSurfaceViewCallback(context);
        mOverLayHolder.addCallback(mPreviewCallback);
        mOverLaySurfaceView.setVisibility(SurfaceView.INVISIBLE);

        try {
            File dir_save = new File(sava_dir);
            dir_save.mkdir();
        } catch (Exception e) {
            Toast.makeText(context, "e:" + e, Toast.LENGTH_SHORT).show();
        }

        //Start button
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecorder != null)
                    mRecorder.start();
            }
        });

        //Recording stop
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_playing = false;
                mRecorder.stop();
/*              画像visible
                final RelativeLayout mRL = (RelativeLayout) findViewById(R.id.image_layout);
                final OverlayContent ol_playing = new OverlayContent(context);

                ol_playing.setOnClickListener(
                        new View.OnClickListener() {
                            public void onClick(View v) {
                                if (!is_playing) {
                            //        ol_playing.setVisibility(ol_playing.INVISIBLE);
                                    is_playing = true;
                                    mRecorder.start();
                                }
                            }
                        }
                );
                mRL.addView(ol_playing);
                mOverLaySurfaceView.setVisibility(SurfaceView.INVISIBLE);
*/
            }
        });



        //試合開始とストップ
        is_playing = false;

        /*録画開始・中断ボタン
        final RelativeLayout mRL = (RelativeLayout) findViewById(R.id.image_layout);
        final OverlayContent ol_playing = new OverlayContent(this);

        ol_playing.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (is_playing) {
                            ol_playing.setVisibility(ol_playing.VISIBLE);
                            is_playing = false;
                    //        Toast.makeText(context, "is_playing true", Toast.LENGTH_SHORT).show();
                        } else {
                            ol_playing.setVisibility(ol_playing.INVISIBLE);
                            is_playing = true;
                   //         Toast.makeText(context, "is_playing false", Toast.LENGTH_SHORT).show();
                        }
                        mRecorder.start();
                    }
                }
        );
        mRL.addView(ol_playing);
        */

        Team mTeam1 = new Team(context, (ListView) findViewById(R.id.our_team_list));
        Team mTeam2 = new Team(context, (ListView) findViewById(R.id.opposing_team_list));
        mEventLogger = new EventLogger(context,(ListView) findViewById(R.id.event_log));

        findViewById(R.id.shoot_success_2p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(2,1,"shoot");//1:point,2:is success?,3:event name
            }
        });
        findViewById(R.id.shoot_success_3p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(3,1,"shoot");//1:point,2:is success?,3:event name
            }
        });
        findViewById(R.id.shoot_failed_2p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(2,0,"shoot");//1:point,2:is success?,3:event name
            }
        });
        findViewById(R.id.shoot_failed_3p).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(3,0,"shoot");//1:point,2:is success?,3:event name
            }
        });
        findViewById(R.id.foul).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordEvent(0,1,"foul");
            }
        });

    }
    public boolean replay(String movie_name){
        mOverLaySurfaceView.setVisibility(SurfaceView.VISIBLE);
        try {
            if (mPreviewCallback.mMediaPlayer != null) {
                mPreviewCallback.mMediaPlayer.release();
                mPreviewCallback.mMediaPlayer = null;
            }
            mPreviewCallback.palyVideo(movie_name);
            mPreviewCallback.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mOverLaySurfaceView.setVisibility(SurfaceView.INVISIBLE);
                }
            });
        } catch (NullPointerException e) {
            Toast.makeText(context, "ぬるぽ", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    public void recordEvent(int point, int is_success,String event_name) {
        //TODO:録画中でないとエラー
        String file_name = "no file";
        //   if(mRecorder.mCamera != null) {
        mRecorder.stop();
        file_name = mRecorder.save();
        mRecorder.start();
        //   }
        if (event_name == "shoot" && is_success == 1) {
            final TextView tv_our_score = (TextView)findViewById(R.id.our_score);
            int our_score = Integer.parseInt(tv_our_score.getText().toString());

            final TextView tv_opp_score = (TextView)findViewById(R.id.opposing_score);
            int opp_score = Integer.parseInt(tv_opp_score.getText().toString());

            switch (who_is_acter[0]) {
                case 0:
                    int our_point = our_score + point;
                    tv_our_score.setText(Integer.toString(our_point));//intをsetText()すると落ちる
                    //    Toast.makeText(context,"味方チーム"+ who_is_acter[1]+"番 得点！",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    int opp_point = opp_score + point;
                    tv_opp_score.setText(Integer.toString(opp_point));
                    //    Toast.makeText(context,"敵チーム"+who_is_acter[1]+"番 得点！",Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(context, "(score)team isnt be selected", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "(score)team cant be specified", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        mEventLogger.addEvent(who_is_acter[0],who_is_acter[1],point,is_success,event_name,file_name);

    }

    private static class FileSort implements Comparator<File> {
        public int compare(File src, File target) {
            int diff = src.getName().compareTo(target.getName());
            return diff;
        }
    }

    @Override
    public void onResume(){ //アクティビティ再び表示されたとき
        mRecorder.resume();
        super.onResume();
    }

    @Override
    protected void onPause() { //別アクティビティ起動時
        mRecorder.pause();
        super.onPause();
    }
}
