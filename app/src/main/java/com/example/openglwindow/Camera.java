package com.example.openglwindow;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.Matrix;


public class Camera {
        private final float[] vPMatrix = new float[16];
        private final float[] projectionMatrix = new float[16];
        private final float[] viewMatrix = new float[16];
        private float[] lookPos = new float[3];

        public float[] getCameraPos(){
                return lookPos;
        }


        public void setFrustrum(float ratio){
                int scale = 10;
                Matrix.frustumM(projectionMatrix, 0, -ratio/scale, ratio/scale, -1.0f/scale, 1.0f/scale, 1.0f/scale, 1.0f*scale);
        }

        public float[] getCamera(){
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

                return vPMatrix;
        }

        public void setCamera(float[] lookingFrom, float[] lookingAt){
                lookPos = lookingFrom;
                Matrix.setLookAtM(viewMatrix, 0, lookingFrom[0], lookingFrom[1], lookingFrom[2], lookingAt[0], lookingAt[1], lookingAt[2], 0f, 1.0f, 0.0f);

        }
}
