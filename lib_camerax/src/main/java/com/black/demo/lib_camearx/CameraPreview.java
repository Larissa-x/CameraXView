package com.black.demo.lib_camearx;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;


/**
 * @author xuehao
 * 相机预览
 */
public class CameraPreview extends FrameLayout {

    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageView imageView;
    private Preview preview;

    public CameraPreview(@NonNull Context context) {
        super(context);
        init();
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        previewView = new PreviewView(getContext());
        addView(previewView);
        imageView = new ImageView(getContext());
        addView(imageView);
        preview = new Preview.Builder().build();
    }


    /**
     * 相机初始化 并打开相机
     * @param lifecycleOwner
     * @param isFrontCamera
     */
    private void setUpCamera(LifecycleOwner lifecycleOwner, boolean isFrontCamera) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(lifecycleOwner, isFrontCamera);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    /**
     * 打开相机
     * @param lifecycleOwner
     * @param isFrontCamera
     */
    public void openCamera(LifecycleOwner lifecycleOwner, boolean isFrontCamera) {
        setUpCamera(lifecycleOwner, isFrontCamera);
    }

    /**
     * 绑定预览
     * @param lifecycleOwner
     * @param isFrontCamera
     */
    private void bindPreview(
            LifecycleOwner lifecycleOwner,
            boolean isFrontCamera
    ) {
        //解除所有绑定，防止CameraProvider重复绑定到Lifecycle发生异常
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(lifecycleOwner, getCameraSelector(isFrontCamera), preview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    /**
     * 切换相机前后置
     * @param lifecycleOwner    绑定相机生命周期
     * @param isFrontCamera     true 前置 false 后置
     */
    public void switchCamera(LifecycleOwner lifecycleOwner, boolean isFrontCamera) {
        if (cameraProvider == null) {
            return;
        }
        Bitmap bitmap = ImageUtils.fastBlur(getCameraOriginBitmap(), 0.25f, 25f);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        bindPreview(lifecycleOwner, isFrontCamera);
        imageView.postDelayed(() -> imageView.setVisibility(View.GONE), 300);
    }

    /**
     * 获取相机前或后置
     * @param isFrontCamera true 前置 false 后置
     * @return
     */
    private CameraSelector getCameraSelector(boolean isFrontCamera) {
        if (isFrontCamera) {
            return CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            return CameraSelector.DEFAULT_BACK_CAMERA;
        }
    }


    /**
     * 获取相机最后一帧的图片
     * @return
     */
    private Bitmap getCameraOriginBitmap() {
        if (previewView != null) {
            return previewView.getBitmap();
        }
        return null;
    }

    /**
     * 方便扩展拍照之类的使用
     * @return
     */
    public ProcessCameraProvider getCameraProvider() {
        return cameraProvider;
    }
}
