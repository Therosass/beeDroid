package com.example.openglwindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.renderscript.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class GLRenderer implements GLSurfaceView.Renderer {

    public Camera camera = new Camera();

    int Translations[] = new int[1];

    private final Context mActivityContext;

    int mTextureDataHandle = -1;

    public Matrix4f mvp;

    private final String vertexShaderCode =
            "#version 300 es\n\n"+
                    "in vec2 a_TexCoordinate;\n"+
                    "uniform mat4 mvpMat;\n" +
                    "uniform mat4 transformation;\n"+
                    "out vec2 v_TexCoordinate;\n"+
                    "in vec3 inPosition;\n" +
                    "void main()\n" +
                    "{\n" +
                    " gl_Position = transformation * ( mvpMat * vec4(inPosition.xyz, 1.0));\n" +
                    " v_TexCoordinate = a_TexCoordinate;\n"+
                    "}  \n";

    private final String fragmentShaderCode =
            "#version 300 es\n\n"+
                    "in vec2 v_TexCoordinate;\n"+
                    "uniform sampler2D u_Texture;\n"+
                    "out vec4 outColor;\n"+
                    "void main()\n" +
                    "{\n" +
                        "outColor  = texture(u_Texture, v_TexCoordinate);\n" +
                    "}  \n";

    private Object[] objectsToRender = new Object[2];

    private int shaderProgram = -1;

    static final int COORDS_PER_VERTEX = 3;
    private int attribLoc = -1;

    /*
    *
    * BUFFER OBJECTS
    *
     */

    final int[] VBOs = new int[2];
    final int[] IBOs = new int[2];

    public GLRenderer(Context mActivityContext) {
        this.mActivityContext = mActivityContext;
    }

    public void loadModel(Object object, int location){
        objectsToRender[location] = object;
    }

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public void createBuffers(){
        GLES20.glGenBuffers(2, VBOs, 0);
        GLES20.glGenBuffers(2, IBOs, 0);

        for(int i = 0; i < 2; i++) {
            FloatBuffer vbo = ByteBuffer.allocateDirect(objectsToRender[i].getVerticesSize()).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vbo.put(Object.returnVerticesAsArray(objectsToRender[i].vertices)).position(0);
            IntBuffer ibo = ByteBuffer.allocateDirect(objectsToRender[i].getIndicesSize()).order(ByteOrder.nativeOrder()).asIntBuffer();
            ibo.put(Object.returnIndicesAsArray(objectsToRender[i].faces)).position(0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOs[i]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, objectsToRender[i].getVerticesSize(), vbo, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBOs[i]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, objectsToRender[i].getIndicesSize(), ibo, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        createBuffers();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        camera.setFrustrum((float) width / height);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearColor(0,0,1.0f,0);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        try {
            GLES20.glViewport(0, 0, width, height);
            int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, vertexShaderCode);
            GLES20.glCompileShader(vertexShader);

            int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
            GLES20.glCompileShader(fragmentShader);

            shaderProgram = GLES20.glCreateProgram();
            GLES20.glBindAttribLocation(shaderProgram, 0, "inPosition");
            GLES20.glBindAttribLocation(shaderProgram, 1, "a_TexCoordinate");
            GLES20.glAttachShader(shaderProgram, vertexShader);
            GLES20.glAttachShader(shaderProgram, fragmentShader);


            GLES20.glLinkProgram(shaderProgram);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0)
            {
                System.out.println("Error compiling program: " + GLES20.glGetProgramInfoLog(shaderProgram));
                GLES20.glDeleteProgram(shaderProgram);
                shaderProgram = 0;
            }
            mTextureDataHandle = loadTexture(mActivityContext,R.drawable.checkerboard);

        }
        catch (Error err){
            err.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        float scale = (float) (Math.sin(SystemClock.uptimeMillis()/100) + 1) /2;
        float[] newMatrix = {
                scale, 0.0f, 0.0f, 0.0f,
                0.0f, scale, 0.0f, 0.0f,
                0.0f, 0.0f, scale, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };
        objectsToRender[0].changeTransform(newMatrix);

        if(shaderProgram != -1) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(shaderProgram);

            int mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
            GLES20.glUniform1i(mTextureUniformHandle, 0);


            float[] cameraMatrix = camera.getCamera();

            for(int i = 0; i < 2; i++) {

                int MVPLocation = GLES20.glGetUniformLocation(shaderProgram, "mvpMat");
                GLES20.glUniformMatrix4fv(MVPLocation, 1, false, cameraMatrix, 0);

                MVPLocation = GLES20.glGetUniformLocation(shaderProgram, "transformation");
                GLES20.glUniformMatrix4fv(MVPLocation, 1, false, objectsToRender[i].transform.mat, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOs[i]);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBOs[i]);

                attribLoc = GLES20.glGetAttribLocation(shaderProgram, "inPosition");
                if (attribLoc == -1) {
                    System.out.println("Failed to get attrib location");
                }
                GLES20.glVertexAttribPointer(attribLoc, 3, GLES20.GL_FLOAT, false, 36, 0);
                GLES20.glEnableVertexAttribArray(attribLoc);
                attribLoc = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");
                if (attribLoc == -1) {
                    System.out.println("Failed to get attrib location");
                }
                GLES20.glVertexAttribPointer(attribLoc, 2, GLES20.GL_FLOAT, false, 36, 16);
                GLES20.glEnableVertexAttribArray(attribLoc);

                GLES20.glDrawElements(GLES20.GL_TRIANGLES, objectsToRender[i].vertices.size(), GLES20.GL_UNSIGNED_INT, 0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

    }
}
