package com.design.senior;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageOutputConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.os.Bundle;

import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.view_finder);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {
        CameraX.unbindAll();

        PreviewConfig config = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build();
        Preview preview = new Preview(config);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput previewOutput) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(previewOutput.getSurfaceTexture());

                        // Compute center of preview
                        float centerX = (float)textureView.getWidth()/2;
                        float centerY = (float)textureView.getHeight()/2;

                        int rotationDegrees;

                        switch((int)textureView.getRotation()) {
                            case Surface.ROTATION_0: rotationDegrees = 0; break;
                            case Surface.ROTATION_90: rotationDegrees = 90; break;
                            case Surface.ROTATION_180: rotationDegrees = 180; break;
                            case Surface.ROTATION_270: rotationDegrees = 270; break;
                            default: return;
                        }

                        Matrix matrix = new Matrix();
                        matrix.postRotate((float)rotationDegrees,centerX,centerY);

                        textureView.setTransform(matrix);
                    }
                });

        CameraX.bindToLifecycle(this, preview);
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}
