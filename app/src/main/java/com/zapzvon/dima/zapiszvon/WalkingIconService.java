package com.zapzvon.dima.zapiszvon;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WalkingIconService extends Service {

    public static WalkingIconService Ser=null;

    private MediaRecorder mediaRecorder = null;

    private boolean zvon=false;

    private String createDataString()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdf.format(c.getTime());
    }

    File outFZv;
    boolean B;

    public  void setZvon(String Imia,boolean b) {
        if (!zvon) {
            B=b;
            zvon = true;
            File dirZv = Environment.getExternalStorageDirectory();
            dirZv = new File(dirZv, "ZapZvon");
            dirZv.mkdirs();

            outFZv = new File(dirZv, Imia + "_" + createDataString() + ".3gpp");
            creatAudioMediaRecorder(outFZv);
        }
    }

    public void konZvon() {
        if (zvon) {
            if (mediaRecorder != null) {
                releaseMediaRecorder();
            }

            String fg = null;
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(outFZv.getPath());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                int finalTime = mediaPlayer.getDuration();
                mediaPlayer.release();

                fg=String.format("%d-%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) finalTime)));

                String s1=outFZv.getParent();
                String s2;
                if (B)
                    s2=outFZv.getName().replace(".3gpp","_")+fg+"_"+"in"+".3gpp";
                else
                    s2=outFZv.getName().replace(".3gpp","_")+fg+"_"+"out"+".3gpp";


                if (finalTime>10000)
                    outFZv.renameTo(new File(s1,s2));
                else
                    outFZv.delete();

            } catch (IOException e) {
                e.printStackTrace();
            }

            zvon = false;
        }
    }

    private void  creatAudioMediaRecorder(File outFile) {
        try {
            releaseMediaRecorder();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(outFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Ser=this;
        return super.onStartCommand(intent, flags, startId);
    }

    private static void DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

}