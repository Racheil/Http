package http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JMClassLoader extends ClassLoader {
    private static final String HOME = System.getenv("JERRYMOUSE_HOME");

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // HOME/webapp/WEB-INF/classes/
        // 1. 根据类名称，找到 name 对应的 *.class 文件
        File classFile = getClassFile(name);
        // 2. 读取该文件的内容
        byte[] buf;
        try {
            buf = readClassBytes(classFile);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        // 3. 调用 defineClass，转为 Class
        return defineClass(name, buf, 0, buf.length);
    }

    private byte[] readClassBytes(File classFile) throws IOException {
        int len = (int)classFile.length();
        byte[] buf = new byte[len];
        InputStream is = new FileInputStream(classFile);
        is.read(buf, 0, len);
        return buf;
    }

    private File getClassFile(String name) {
        String filename = HOME + File.separator + "webapps" + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + name + ".class";
        System.out.println(filename);
        File file = new File(filename);
        System.out.println(file.exists());
        return new File(filename);
    }
}
