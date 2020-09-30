package com.example.openglwindow;

import android.content.res.AssetManager;
import android.opengl.Matrix;
import android.renderscript.Matrix4f;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Object {
    ArrayList<Position> positions = new ArrayList<Position>();
    ArrayList<Texel> texels = new ArrayList<Texel>();
    ArrayList<Normal> normals = new ArrayList<Normal>();
    ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    ArrayList<Face> faces = new ArrayList<Face>();
    Transformation transform = new Transformation();

    public int VBOindex;
    public int IBOindex;
    public int textureIndex;

    static class Transformation{
        float[] mat = {
            1.0f,0.0f,0.0f,0.0f,
            0.0f,1.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,-0.2f,0.0f,1.0f
        };
    }

    public void changeTransform(float[] newNumbers){
        for(int i = 0; i < 16; i++) {
            transform.mat[i] = newNumbers[i];
        }
    }

    static class Position{
        float x;
        float y;
        float z;
        float w;
    }
    static class Texel{
        float u;
        float v;
    }
    static class Normal{
        float x;
        float y;
        float z;
    }
    static class Vertex {
        Position pos;
        Texel texel;
        Normal normal;
        static int getSize(){
            return 16 + 8 + 12;
        }
    }

    static class Face {
        int[] vertices = new int[3];
        static int getSize(){
            return 3*4;
        }
    }

    public int getVerticesSize(){
        return this.vertices.size()*Vertex.getSize();
    }

    public int getIndicesSize(){
        return this.faces.size()*Face.getSize();
    }

    public static float[] returnVerticesAsArray(ArrayList<Vertex> vertices){
        int vertexAmount = vertices.size() * 9;
        float[] verticesFlatArray = new float[vertexAmount];
        for(int i = 0; i < vertices.size(); i++){
            verticesFlatArray[i*9] = vertices.get(i).pos.x;
            verticesFlatArray[i*9+1] = vertices.get(i).pos.y;
            verticesFlatArray[i*9+2] = vertices.get(i).pos.z;
            verticesFlatArray[i*9+3] = vertices.get(i).pos.w;
            verticesFlatArray[i*9+4] = vertices.get(i).texel.u;
            verticesFlatArray[i*9+5] = vertices.get(i).texel.v;
            verticesFlatArray[i*9+6] = vertices.get(i).normal.x;
            verticesFlatArray[i*9+7] = vertices.get(i).normal.y;
            verticesFlatArray[i*9+8] = vertices.get(i).normal.z;
        }
        return verticesFlatArray;
    }

    public static int[] returnIndicesAsArray(ArrayList<Face> faces){
        int indiceAmount = faces.size() * 3;
        int[] indicesFlatArray = new int[indiceAmount];
        for(int i = 0; i < faces.size(); i++){
            indicesFlatArray[i*3] = faces.get(i).vertices[0];
            indicesFlatArray[i*3+1] = faces.get(i).vertices[1];
            indicesFlatArray[i*3+2] = faces.get(i).vertices[2];
        }
        return indicesFlatArray;
    }
}
