package http;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JCPClassLoader extends ClassLoader{
    private static final String HOME = System.getenv("JERRYMOUSE_HOME");
    private String[] getClassPaths(){
        return new String[]{
                HOME+ File.separator+"work",
                HOME+File.separator+"webapps"+File.separator+"WEB-INF"+File.separator+"classes",
        };
    }

    private File findClassFile(String name){
         for(String path:getClassPaths()){
             String filename = path+File.separator+name+".class";
             File file = new File(filename);
             if(file.exists()){
                 return file;
             }
         }
         return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        try{
            return super.findClass(name);
        }catch (ClassNotFoundException ignored){

        }
        name = name.replace(".",File.separator);
        File file = findClassFile(name);
        if(file == null){
            throw new ClassNotFoundException(name);
        }

        byte[] raw;
        try {
            raw = getBytes(file);
        }catch (IOException E){
            throw new ClassNotFoundException(name);
        }
        return defineClass(name,raw,0,raw.length);
    }

    private byte[] getBytes(File classFile)throws IOException{
        int len =(int)classFile.length();
        InputStream is = new FileInputStream(classFile);
        byte[] buf = new byte[len];
        is.read(buf);
        return buf;
    }

}
