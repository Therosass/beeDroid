package com.example.openglwindow;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.Matrix;


public class Camera {
        private final float[] vPMatrix = new float[16];
        private final float[] projectionMatrix = new float[16];
        private final float[] viewMatrix = new float[16];


        public void setFrustrum(float ratio){
                int scale = 5;
                Matrix.frustumM(projectionMatrix, 0, -ratio/scale, ratio/scale, -1.0f/scale, 1.0f/scale, 1.0f/scale, 25.0f/scale);
        }

        public float[] getCamera(){
                Matrix.setLookAtM(viewMatrix, 0, 1.5f, 0.5f, -0.3f, 0f, 0f, -0.3f, 0f, 1.0f, 0.0f);

                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

                return vPMatrix;
        }
}
