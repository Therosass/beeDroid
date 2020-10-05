package com.example.openglwindow;

import java.util.ArrayList;

public class Object {
    ArrayList<Position> positions = new ArrayList<Position>();
    ArrayList<Texel> texels = new ArrayList<Texel>();
    ArrayList<Normal> normals = new ArrayList<Normal>();
    ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    ArrayList<Face> faces = new ArrayList<Face>();
    Transformation transform = new Transformation();
    private float[] hitbox;

    public int VBOindex;
    public int IBOindex;
    public int textureIndex;

    static class Transformation{
        float[] mat = {
            1.0f,0.0f,0.0f,0.0f,
            0.0f,1.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,-0.0f,0.0f,1.0f
        };
    }

    public void moveObject(float[] newPos) {
        transform.mat[12] = newPos[0];
        transform.mat[13] = newPos[1];
        transform.mat[14] = newPos[2];
        calcHitbox();
    }



    public void changeTransform(float[] newNumbers){
        for(int i = 0; i < 16; i++) {
            transform.mat[i] = newNumbers[i];
        }
        calcHitbox();
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

    private float findWidth(){
        float minX = vertices.get(0).pos.x;
        float maxX = vertices.get(0).pos.x;

        for(Vertex vertex : vertices){
            if(vertex.pos.x < minX){
                minX = vertex.pos.x;
            }
            if(vertex.pos.x > maxX){
                maxX = vertex.pos.x;
            }
        }
        return maxX - minX;
    }

    private float findThird(){
        float minZ = vertices.get(0).pos.z;
        float maxZ = vertices.get(0).pos.z;

        for(Vertex vertex : vertices){
            if(vertex.pos.x < minZ){
                minZ = vertex.pos.z;
            }
            if(vertex.pos.x > maxZ){
                maxZ = vertex.pos.z;
            }
        }
        return maxZ - minZ;
    }

    private float findHeight(){
        float minY = vertices.get(0).pos.y;
        float maxY = vertices.get(0).pos.y;

        for(Vertex vertex : vertices){
            if(vertex.pos.y < minY){
                minY = vertex.pos.y;
            }
            if(vertex.pos.y > maxY){
                maxY = vertex.pos.y;
            }
        }
        return maxY - minY;
    }
    public float[] getHitBox(){
        return hitbox;
    }

    public void calcHitbox(){
        if(hitbox == null) {
            hitbox = new float[8];
        }
        float width = findWidth()/2 * transform.mat[0];
        float height = findHeight()/2 * transform.mat[0];
        hitbox[0] = -width + transform.mat[12];
        hitbox[1] = -height + transform.mat[14];
        hitbox[2] = width + transform.mat[12];
        hitbox[3] = -height + transform.mat[14];
        hitbox[4] = -width + transform.mat[12];
        hitbox[5] = height + transform.mat[14];
        hitbox[6] = width + transform.mat[12];
        hitbox[7] = height + transform.mat[14];
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
