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
            objects[0] = ObjectReader.readFile(R.raw.ball_model,getContext());
            objects[1] = ObjectReader.readFile(R.raw.lamp_model,getContext());
            objects[2] = ObjectReader.readFile(R.raw.lamp_model,getContext());
            objects[3] = ObjectReader.readFile(R.raw.lamp_model,getContext());
            objects[4] = ObjectReader.readFile(R.raw.lamp_model,getContext());
            objects[5] = ObjectReader.readFile(R.raw.map_model,getContext());
            objects[6] = ObjectReader.readFile(R.raw.box_model,getContext());
            objects[7] = ObjectReader.readFile(R.raw.box_model,getContext());
            objects[8] = ObjectReader.readFile(R.raw.box_model,getContext());
            objects[9] = ObjectReader.readFile(R.raw.pyramid_model,getContext());
            objects[10] = ObjectReader.readFile(R.raw.pyramid_model,getContext());


        } catch (FileNotFoundException e) {
            System.out.println("OWO");
        } catch (IOException e) {
            System.out.println("UWU");
        }

        mMyRenderer = new GLRenderer(this.getContext());
        setRenderer(mMyRenderer);
        mMyRenderer.loadModel(objects[0], 0); // Golyo
        mMyRenderer.loadModel(objects[1], 1); // Lampa1
        mMyRenderer.loadModel(objects[2], 2); // Lampa2
        mMyRenderer.loadModel(objects[3], 3); // Lampa3
        mMyRenderer.loadModel(objects[4], 4); // Lampa4
        mMyRenderer.loadModel(objects[5], 5); // Palya
        mMyRenderer.loadModel(objects[6], 6); // Akadaly1
        mMyRenderer.loadModel(objects[7], 7); // Akadaly2
        mMyRenderer.loadModel(objects[8], 8); // Akadaly3
        mMyRenderer.loadModel(objects[9], 9); // Akadaly4
        mMyRenderer.loadModel(objects[10], 10); // Akadaly4
        camera=mMyRenderer.camera;


        //objects[0].moveObject(new float[]{2.3f, 0.2f,0.75f}); // Golyo

        objects[1].changeTransform(new float[]{
                -1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,-1.0f,0.0f,
                0.0f,-0.0f,0.0f,1.0f
        });
        objects[2].changeTransform(new float[]{
                -1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,-1.0f,0.0f,
                0.0f,-0.0f,0.0f,1.0f
        });
        objects[1].moveObject(new float[]{2.0f, 0.1f,1.0f}); // Lampa1
        objects[2].moveObject(new float[]{-2.0f, 0.1f,1.0f}); // Lampa2
        objects[3].moveObject(new float[]{2.0f, 0.1f,-1.0f}); // Lampa3
        objects[4].moveObject(new float[]{-2.0f, 0.1f,-1.0f}); // Lampa4

        objects[6].moveObject(new float[]{1.7f, 0.0f,0.675f}); // Akadaly1
        objects[7].moveObject(new float[]{-0.35f, 0.0f,-0.675f}); // Akadaly2
        objects[8].moveObject(new float[]{-0.8f, 0.0f,0.675f}); // Akadaly3

        objects[9].moveObject(new float[]{0.65f, 0.0f,0.0f}); // Akadaly4
        objects[10].moveObject(new float[]{-1.6f, 0.0f,-0.65f}); // Akadaly4

        //objects[3].changeTransform(newMatrix2);



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