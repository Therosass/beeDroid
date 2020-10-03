package com.example.openglwindow;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FirstFragment extends Fragment {


    Object[] objects = new Object[11];
    Camera camera;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        OpenGLView mGLView = new OpenGLView(getActivity());
        mGLView.start();
        objects = mGLView.getObjects();
        camera=mGLView.getCamera();



        // Inflate the layout for this fragment
        return mGLView;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity ma = (MainActivity)getActivity();
        ma.setObjects(objects);
        ma.setCamera(camera);


    }
}