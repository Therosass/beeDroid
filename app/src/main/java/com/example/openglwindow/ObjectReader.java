package com.example.openglwindow;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import android.content.Context;
import android.os.FileUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectReader{

    public static void parseLine(String line, Object object){
        if(line.charAt(0) == 'v'){
            if(line.charAt(1) == 'n'){
                object.normals.add(readNormalData(line));
            }
            else if(line.charAt(1) == 't'){
                object.texels.add(readTextureData(line));
            }
            else{
                object.positions.add(readPosData(line));
            }
        }
        else if(line.charAt(0) == 'f') {
            object.faces.add(readFaces(line, object));
        }
    }

    private static Object.Position readPosData(String line){
        Object.Position newVertex = new Object.Position();
        Pattern p = Pattern.compile("v ([-0-9.]*) ([-0-9.]*) ([-0-9.]*)");
        Matcher m = p.matcher(line);
        if(m.find()){
            String firstMatch = m.group(1).toString();
            newVertex.x = Float.parseFloat(m.group(1).toString());
            newVertex.y = Float.parseFloat(m.group(2).toString());
            newVertex.z = Float.parseFloat(m.group(3).toString());
        }
        return newVertex;
    }

    private static Object.Normal readNormalData(String line){
        Object.Normal newNormal = new Object.Normal();
        Pattern p = Pattern.compile("vn ([-0-9.]*) ([-0-9.]*) ([-0-9.]*)");
        Matcher m = p.matcher(line);
        if(m.find()){
            newNormal.x = Float.parseFloat(m.group(1));
            newNormal.y = Float.parseFloat(m.group(2));
            newNormal.z = Float.parseFloat(m.group(3));
        }
        return newNormal;
    }

    private static Object.Texel readTextureData(String line){
        Object.Texel newTexel = new Object.Texel();
        Pattern p = Pattern.compile("vt ([-0-9.]*) ([-0-9.]*)");
        Matcher m = p.matcher(line);
        if(m.find()){
            newTexel.u = Float.parseFloat(m.group(1));
            newTexel.v = Float.parseFloat(m.group(2));
        }
        return newTexel;
    }

    private static Object.Vertex readPoint(String pointData, Object object){
        Object.Vertex newVertex = new Object.Vertex();
        Pattern p = Pattern.compile("([0-9]*)\\/([0-9]*)\\/([0-9]*)");
        Matcher m = p.matcher(pointData);
        if(m.find()){
            newVertex.pos = object.positions.get(Integer.parseInt(m.group(1)) - 1);
            newVertex.texel = object.texels.get(Integer.parseInt(m.group(2)) - 1);
            newVertex.normal = object.normals.get(Integer.parseInt(m.group(3)) - 1);
        }
        object.vertices.add(newVertex);
        return newVertex;
    }

    public static Object.Face readFaces(String line, Object object){
        Object.Face newFace = new Object.Face();
        Pattern p = Pattern.compile("f ([0-9]*\\/[-0-9]*\\/[-0-9]*) ([0-9]*\\/[-0-9]*\\/[-0-9]*) ([0-9]*\\/[-0-9]*\\/[-0-9]*)");
        Matcher m = p.matcher(line);
        if(m.find()){
            // This is terrible, but I'm too lazy to implement a map
            for(int i = 0; i < 3; i++) {
                readPoint(m.group(i+1), object);
                newFace.vertices[i] = i + object.faces.size() * 3;
            }
        }
        return newFace;
    }

        public static Object readFile(int modelRID, Context context) throws IOException {
            Object newObject = new Object();

            InputStream inputStream = context.getResources().openRawResource(modelRID);

            Scanner sc = new Scanner(inputStream);
            while (sc.hasNextLine()){
                parseLine(sc.nextLine(),newObject);
            }

            return newObject;
        }
}

