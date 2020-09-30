package com.example.openglwindow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;

public class OpenGLView extends GLSurfaceView {

    Object[] object = new Object[2];
    private GLRenderer mMyRenderer;

    public void start() {
        try {
            object[0] = ObjectReader.readFile("ball_base.obj",getContext());
            object[1] = ObjectReader.readFile("palya_tris.obj",getContext());
        } catch (FileNotFoundException e) {
            System.out.println("OWO");
        } catch (IOException e) {
            System.out.println("UWU");
        }

        mMyRenderer = new GLRenderer(this.getContext());
        setRenderer(mMyRenderer);
        mMyRenderer.loadModel(object[0], 0);
        mMyRenderer.loadModel(object[1], 1);

    }

    public OpenGLView(Context context) {
        super(context);
        setEGLContextClientVersion(2);

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        return false;
    }

}