package com.example.openglwindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.renderscript.Matrix4f;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class GLRenderer implements GLSurfaceView.Renderer {

    public Camera camera = new Camera();

    int Translations[] = new int[1];

    private final Context mActivityContext;

    int[] mTextureDataHandle = new int[2];

    public Matrix4f mvp;

    private class PointLight {
        float[] position = new float[3];
        float[] diffuse = new float[3];
        float[] ambient = new float[3];
        float[] specular = new float[3];

        float constant;
        float linear;
        float quadratic;
    };

    private final PointLight[] lights = new PointLight[4];

    private final String vertexShaderCode =
            "#version 300 es\n\n"+
                    "uniform mat4 mvpMat;\n" +
                    "uniform mat4 transformation;\n"+
                    "out vec2 v_TexCoordinate;\n"+
                    "in vec2 a_TexCoordinate;\n"+
                    "in vec3 inPosition;\n" +
                    "in vec3 inNormal;\n"+
                    "out vec3 outNormal;\n"+
                    "out vec3 transPos;\n" +
                    "void main()\n" +
                    "{\n" +
                    " gl_Position = mvpMat * ( transformation * vec4(inPosition.xyz, 1.0));\n" +
                    " v_TexCoordinate = a_TexCoordinate + vec2(transformation[3].x/20.0, transformation[3].z/20.0);\n"+
                    " outNormal = inNormal;\n"+
                    " transPos = vec3(transformation * gl_Position);\n" +
                    "}  \n";

    private final String fragmentShaderCode =
            "#version 300 es\n\n"+
                    "precision mediump float;\n"+
                    "in vec2 v_TexCoordinate;\n"+
                    "uniform sampler2D u_Texture;\n"+
                    "in vec3 outNormal;\n"+
                    "in vec3 transPos;\n"+
                    "out vec4 outColor;\n"+
                    "struct PointLight {"+
                        "vec3 position;\n"+
                        "float constant;\n"+
                        "float linear;\n"+
                        "float quadratic;\n"+
                        "vec3 ambient;\n"+
                        "vec3 diffuse;\n"+
                        "vec3 specular;\n"+
                    "};\n"+
                    "#define NR_POINT_LIGHTS 4\n"+
                    "uniform PointLight pointLights[NR_POINT_LIGHTS];\n"+
                   "vec3 CalcPointLight(PointLight light, vec3 normal, vec3 fragPos, vec3 viewDir)\n"+
                    "{\n"+
                        "vec3 lightDir = normalize(light.position - fragPos);\n"+
                        "float diff = max(dot(normal, lightDir), 0.0);\n"+
                        "vec3 reflectDir = reflect(-lightDir, normal);\n"+
                        "float spec = pow(max(dot(viewDir, reflectDir), 0.0), 0.2);\n"+
                        //"float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);\n"+
                        "float distance    = length(light.position - fragPos);\n"+
                        "float attenuation = 1.0 / (light.constant + light.linear * distance +\n"+
                                "light.quadratic * (distance * distance));\n"+
                        "vec3 ambient  = light.ambient  * vec3(texture(u_Texture, v_TexCoordinate));\n"+
                        "vec3 diffuse  = light.diffuse  * diff * vec3(texture(u_Texture, v_TexCoordinate));\n"+
                        "vec3 specular = light.specular * spec * vec3(texture(u_Texture, v_TexCoordinate));\n"+
                        "ambient  *= attenuation;\n"+
                        "diffuse  *= attenuation;\n"+
                        "specular *= attenuation;\n"+
                        "return (ambient + diffuse + specular);\n"+
                    "}\n"+

                    "void main()\n" +
                    "{\n" +
                        "vec3 normal = normalize(outNormal);\n"+
                        "vec3 result = vec3(0.0,1.0,0.0);\n"+
                        "for(int i = 0; i < NR_POINT_LIGHTS; i++)\n"+
                            "result = CalcPointLight(pointLights[i], outNormal, transPos, vec3(1.5,0.5,-0.3));\n"+
                        "outColor  = vec4(result,1.0);\n"+
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
        camera.setCamera(new float[]{1.5f, 0.5f, -0.3f}, new float[]{0f, 0f, 0f});

        String maxVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION);

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
            GLES20.glBindAttribLocation(shaderProgram, 2, "inNormal");
            GLES20.glAttachShader(shaderProgram, vertexShader);
            GLES20.glAttachShader(shaderProgram, fragmentShader);


            GLES20.glLinkProgram(shaderProgram);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            System.out.println("Error compiling program: " + GLES20.glGetProgramInfoLog(shaderProgram));
            Log.d("tag", "Error compiling program: " + GLES20.glGetProgramInfoLog(shaderProgram));
            Log.d("tag", "Error compiling program: " + GLES20.glGetShaderInfoLog(shaderProgram));
            Log.d("tag", GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION));
            if (linkStatus[0] == 0)
            {

                GLES20.glDeleteProgram(shaderProgram);
                shaderProgram = 0;
            }
            mTextureDataHandle[0] = loadTexture(mActivityContext,R.drawable.checkerboard);
            mTextureDataHandle[1] = loadTexture(mActivityContext,R.drawable.wood);
            initLights();

        }
        catch (Error err){
            err.printStackTrace();
        }
    }

    private void initLights(){
        for(int i = 0; i < 4; i++){
            lights[i] = new PointLight();
            lights[i].ambient = new float[]{0.1f,0.1f,0.1f};
            lights[i].specular = new float[]{1.0f,1.0f,1.0f};
            lights[i].diffuse = new float[]{1.0f,1.0f,1.0f};
            lights[i].constant = 1.0f;
            lights[i].linear = 0.09f;
            lights[i].quadratic = 0.032f;
        }
        lights[0].position = new float[]{1.0f,0.0f,0.0f};
        lights[1].position = new float[]{0.0f,1.0f,0.0f};
        lights[2].position = new float[]{0.0f,0.0f,1.0f};
        lights[3].position = new float[]{0.5f,0.5f,-0.3f};
    }

    private void updateLights(){
        for(int i = 0; i < 4; i++) {
            int position = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].position");
            int ambient = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].ambient");
            int diffuse = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].diffuse");
            int specular = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].specular");
            int constant = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].constant");
            int linear = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].linear");
            int quadratic = GLES20.glGetUniformLocation(shaderProgram, "pointLights[" + Integer.toString(i) + "].quadratic");
            GLES20.glUniform3fv(position, 1, lights[i].position, 0);
            GLES20.glUniform3fv(ambient, 1, lights[i].ambient, 0);
            GLES20.glUniform3fv(diffuse, 1, lights[i].diffuse, 0);
            GLES20.glUniform3fv(specular, 1, lights[i].specular, 0);
            GLES20.glUniform1f(constant, lights[i].constant);
            GLES20.glUniform1f(linear, lights[i].linear);
            GLES20.glUniform1f(quadratic, lights[i].quadratic);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {

//        float timeX = (float) (Math.sin((double) SystemClock.uptimeMillis() / 1000f) + 1) /2;
//        float timeY = (float) (Math.cos((double) SystemClock.uptimeMillis() / 1000f) + 1) /5;
//        float timeZ = (float) (Math.cos((double) SystemClock.uptimeMillis() / 1000f) + 1) /2;
//        float[] newMatrix = {
//                0.2f, 0.0f, 0.0f, 0.0f,
//                0.0f, 0.2f, 0.0f, 0.0f,
//                0.0f, 0.0f, 0.2f, 0.0f,
//                timeX -0.2f , 0, timeZ - 0.2f, 1.0f
//        };
//        objectsToRender[0].changeTransform(newMatrix);

        updateLights();


        if(shaderProgram != -1) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(shaderProgram);



            float[] cameraMatrix = camera.getCamera();
            int MVPLocation = GLES20.glGetUniformLocation(shaderProgram, "mvpMat");
            GLES20.glUniformMatrix4fv(MVPLocation, 1, false, cameraMatrix, 0);


            for(int i = 0; i < 2; i++) {

                int mTextureUniformHandle = GLES20.glGetUniformLocation(shaderProgram, "u_Texture");
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle[i]);
                GLES20.glUniform1i(mTextureUniformHandle, 0);

                MVPLocation = GLES20.glGetUniformLocation(shaderProgram, "transformation");
                GLES20.glUniformMatrix4fv(MVPLocation, 1, false, objectsToRender[i].transform.mat, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOs[i]);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBOs[i]);

                attribLoc = GLES20.glGetAttribLocation(shaderProgram, "inPosition");
                if (attribLoc == -1) {
                    System.out.println("Failed to get attrib pos location");
                }
                GLES20.glVertexAttribPointer(attribLoc, 3, GLES20.GL_FLOAT, false, 36, 0);
                GLES20.glEnableVertexAttribArray(attribLoc);
                attribLoc = GLES20.glGetAttribLocation(shaderProgram, "a_TexCoordinate");
                if (attribLoc == -1) {
                    System.out.println("Failed to get attrib texel location");
                }
                GLES20.glVertexAttribPointer(attribLoc, 2, GLES20.GL_FLOAT, false, 36, 16);
                GLES20.glEnableVertexAttribArray(attribLoc);

                attribLoc = GLES20.glGetAttribLocation(shaderProgram, "inNormal");
                if (attribLoc == -1) {
                    System.out.println("Failed to get attrib normal location");
                }
                GLES20.glVertexAttribPointer(attribLoc, 3, GLES20.GL_FLOAT, false, 36, 24);
                GLES20.glEnableVertexAttribArray(attribLoc);

                GLES20.glDrawElements(GLES20.GL_TRIANGLES, objectsToRender[i].vertices.size(), GLES20.GL_UNSIGNED_INT, 0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

    }
}
