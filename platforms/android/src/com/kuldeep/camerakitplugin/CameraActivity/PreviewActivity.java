package com.kuldeep.camerakitplugin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.ionic.starter.R;

public class PreviewActivity extends AppCompatActivity {

    ImageView imageView;
    VideoView videoView;
    TextView actualResolution;
    TextView approxUncompressedSize;
    TextView captureLatency;
    FrameLayout accept,wrong;
    Bitmap bitmapextracted = null;
    String imagepath="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        imageView = (ImageView)findViewById(R.id.image);
        videoView = (VideoView)findViewById(R.id.video);
        actualResolution = (TextView)findViewById(R.id.actualResolution);
        approxUncompressedSize = (TextView)findViewById(R.id.approxUncompressedSize);
        captureLatency = (TextView)findViewById(R.id.captureLatency);
        accept = (FrameLayout)findViewById(R.id.accept);
        wrong = (FrameLayout)findViewById(R.id.wrong);


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.putExtra("imagepath",imagepath);
                setResult(99,i);
                finish();
            }
        });

        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        byte[] jpeg = com.kuldeep.camerakitplugin.ResultHolder.getImage();
        File video = com.kuldeep.camerakitplugin.ResultHolder.getVideo();

        if (jpeg != null) {
            imageView.setVisibility(View.VISIBLE);

            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
            imagepath = storeImage(bitmap);
            if (bitmap == null) {
                finish();
                return;
            }

            imageView.setImageBitmap(bitmap);
            File image = new File(imagepath);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmapextracted = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

            bitmap = Bitmap.createScaledBitmap(bitmapextracted,1000, 1000,true);
            imageView.setImageBitmap(bitmap);
            actualResolution.setText(bitmap.getWidth() + " x " + bitmap.getHeight());
            approxUncompressedSize.setText(getApproximateFileMegabytes(bitmap) + "MB");
            captureLatency.setText(com.kuldeep.camerakitplugin.ResultHolder.getTimeToCallback() + " milliseconds");
        }

        else if (video != null) {
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(video.getAbsolutePath()));
            MediaController mediaController = new MediaController(this);
            mediaController.setVisibility(View.GONE);
            videoView.setMediaController(mediaController);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();

                    float multiplier = (float) videoView.getWidth() / (float) mp.getVideoWidth();
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (mp.getVideoHeight() * multiplier)));
                }
            });
            //videoView.start();
        }

        else {
            finish();
            return;
        }
    }



    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024 / 1024;
    }


    private String storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return "";
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }


    private  File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

}
