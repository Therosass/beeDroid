package com.example.openglwindow;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;

public class OpenGLView extends GLSurfaceView {

    Object[] objects = new Object[11];
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
            objects[2] = ObjectReader.readFile(R.raw.lamp_model,getContext());
            objects[3] = ObjectReader.readFile(R.raw.ball_base,getContext());
            objects[4] = ObjectReader.readFile(R.raw.ball_base,getContext());
        } catch (FileNotFoundException e) {
            System.out.println("OWO");
        } catch (IOException e) {
            System.out.println("UWU");
        }

        mMyRenderer = new GLRenderer(this.getContext());
        setRenderer(mMyRenderer);
        mMyRenderer.loadModel(objects[0], 0); // Golyo
        mMyRenderer.loadModel(objects[1], 1); // Palya
        mMyRenderer.loadModel(objects[2], 2); // Lampa1
        mMyRenderer.loadModel(objects[2], 3); // Lampa2
        mMyRenderer.loadModel(objects[2], 4); // Lampa3
        mMyRenderer.loadModel(objects[2], 5); // Lampa4
        mMyRenderer.loadModel(objects[3], 6); // Akadaly1
        mMyRenderer.loadModel(objects[3], 7); // Akadaly2
        mMyRenderer.loadModel(objects[3], 8); // Akadaly3
        mMyRenderer.loadModel(objects[4], 9); // Akadaly4
        mMyRenderer.loadModel(objects[4], 10); // Akadaly5
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