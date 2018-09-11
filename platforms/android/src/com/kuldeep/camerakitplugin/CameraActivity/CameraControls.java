package com.kuldeep.camerakitplugin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;

import io.ionic.starter.R;


public class CameraControls extends LinearLayout {

    private int cameraViewId = -1;
    private CameraView cameraView;

    private int coverViewId = -1;
    private View coverView;

    ImageView facingButton;

    ImageView flashButton;
    ImageView captureButton;
    private long captureDownTime;
    private long captureStartTime;
    private boolean pendingVideoCapture;
    private boolean capturingVideo;

    public CameraControls(Context context) {
        this(context, null);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(getContext()).inflate(R.layout.camera_controls, this);
        facingButton = (ImageView)findViewById(R.id.facingButton);
        flashButton = (ImageView)findViewById(R.id.flashButton);
        captureButton = (ImageView)findViewById(R.id.captureButton);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CameraControls,
                    0, 0);

            try {
                cameraViewId = a.getResourceId(R.styleable.CameraControls_camera, -1);
                coverViewId = a.getResourceId(R.styleable.CameraControls_cover, -1);
            } finally {
                a.recycle();
            }
        }

        facingButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                handleViewTouchFeedback(v, event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        coverView.setAlpha(0);
                        coverView.setVisibility(VISIBLE);
                        coverView.animate()
                                .alpha(1)
                                .setStartDelay(0)
                                .setDuration(300)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (cameraView.isFacingFront()) {
                                            cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                                            changeViewImageResource((ImageView) v, R.drawable.ic_facing_front);
                                        } else {
                                            cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                                            changeViewImageResource((ImageView) v, R.drawable.ic_facing_back);
                                        }

                                        coverView.animate()
                                                .alpha(0)
                                                .setStartDelay(200)
                                                .setDuration(300)
                                                .setListener(new AnimatorListenerAdapter() {
                                                    @Override
                                                    public void onAnimationEnd(Animator animation) {
                                                        super.onAnimationEnd(animation);
                                                        coverView.setVisibility(GONE);
                                                    }
                                                })
                                                .start();
                                    }
                                })
                                .start();

                        break;
                    }
                }
                return true;
            }
        });

        captureButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                handleViewTouchFeedback(v, event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        captureDownTime = System.currentTimeMillis();
                        pendingVideoCapture = true;
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (pendingVideoCapture) {
                                    capturingVideo = true;
                                    cameraView.captureVideo();
                                }
                            }
                        }, 250);
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        pendingVideoCapture = false;

                        if (capturingVideo) {
                            capturingVideo = false;
                            cameraView.stopVideo();
                        } else {
                            captureStartTime = System.currentTimeMillis();
                            cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                                @Override
                                public void callback(CameraKitImage event) {
                                    imageCaptured(event);
                                }
                            });
                        }
                        break;
                    }
                }
                return true;
            }
            });

        flashButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                handleViewTouchFeedback(v, event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        if (cameraView.getFlash() == CameraKit.Constants.FLASH_OFF) {
                            cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                            changeViewImageResource((ImageView) v, R.drawable.ic_flash_on);
                        } else {
                            cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                            changeViewImageResource((ImageView) v, R.drawable.ic_flash_off);
                        }

                        break;
                    }
                }
                return true;
            }
            });
        }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (cameraViewId != -1) {
            View view = getRootView().findViewById(cameraViewId);
            if (view instanceof CameraView) {
                cameraView = (CameraView) view;
                cameraView.bindCameraKitListener(this);
                setFacingImageBasedOnCamera();
            }
        }

        if (coverViewId != -1) {
            View view = getRootView().findViewById(coverViewId);
            if (view != null) {
                coverView = view;
                coverView.setVisibility(GONE);
            }
        }
    }

    private void setFacingImageBasedOnCamera() {
        if (cameraView.isFacingFront()) {
            facingButton.setImageResource(R.drawable.ic_facing_back);
        } else {
            facingButton.setImageResource(R.drawable.ic_facing_front);
        }
    }

    public void imageCaptured(CameraKitImage image) {
        byte[] jpeg = image.getJpeg();

        long callbackTime = System.currentTimeMillis();
        com.kuldeep.camerakitplugin.ResultHolder.dispose();
        com.kuldeep.camerakitplugin.ResultHolder.setImage(jpeg);
        com.kuldeep.camerakitplugin.ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
        com.kuldeep.camerakitplugin.ResultHolder.setTimeToCallback(callbackTime - captureStartTime);
        Intent intent = new Intent(getContext(), com.kuldeep.camerakitplugin.PreviewActivity.class);
        ((Activity) getContext()).startActivityForResult(intent,99);
    }


    public void videoCaptured(CameraKitVideo video) {
        File videoFile = video.getVideoFile();
        if (videoFile != null) {
            com.kuldeep.camerakitplugin.ResultHolder.dispose();
            com.kuldeep.camerakitplugin.ResultHolder.setVideo(videoFile);
            com.kuldeep.camerakitplugin.ResultHolder.setNativeCaptureSize(cameraView.getCaptureSize());
            Intent intent = new Intent(getContext(), com.kuldeep.camerakitplugin.PreviewActivity.class);
            getContext().startActivity(intent);
        }
    }







    boolean handleViewTouchFeedback(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownAnimation(view);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                touchUpAnimation(view);
                return true;
            }

            default: {
                return true;
            }
        }
    }

    void touchDownAnimation(View view) {
        view.animate()
                .scaleX(0.88f)
                .scaleY(0.88f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void touchUpAnimation(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.setRotation(0);
        imageView.animate()
                .rotationBy(360)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();

        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(resId);
            }
        }, 120);
    }

}
