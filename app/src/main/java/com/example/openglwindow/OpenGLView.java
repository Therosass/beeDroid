package com.example.openglwindow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;

public class OpenGLView extends GLSurfaceView {

    Object[] objects = new Object[2];
    private GLRenderer mMyRenderer;
    Camera camera;

    public Object[] getObjects(){
        return objects;
    }

    public Camera getCamera(){
        return camera;
    }

    public void start() {
        try {
            objects[0] = ObjectReader.readFile(R.raw.ball_base,getContext());
            objects[1] = ObjectReader.readFile(R.raw.palya_tris,getContext());
        } catch (FileNotFoundException e) {
            System.out.println("OWO");
        } catch (IOException e) {
            System.out.println("UWU");
        }

        mMyRenderer = new GLRenderer(this.getContext());
        setRenderer(mMyRenderer);
        mMyRenderer.loadModel(objects[0], 0);
        mMyRenderer.loadModel(objects[1], 1);
        camera=mMyRenderer.camera;

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