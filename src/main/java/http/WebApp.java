package http;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebApp {
    private static final String HOME = System.getenv("JERRYMOUSE_HOME");
    private Map<String,String> mappings = new HashMap<>();
    private Map<String,String> classes = new HashMap<>();
    private Map<Class<? extends Controller>, Controller> objects = new HashMap<>();

    private WebApp(){

    }

    //解析xml文件，把controller和mapping放在对应的map中
    public static WebApp fromWebXML() throws IOException{
        WebApp webApp = new WebApp();
        String filename = HOME+ File.separator+"webapps"+File.separator+"WEB-INF"+File.separator+"web.xml";
        Document document = null;
        try{
            document = new SAXReader().read(filename);
        }catch (DocumentException e){
            throw new IOException(e);
        }
        Iterator<Element> iterator = document.getRootElement().elementIterator();
        while (iterator.hasNext()){
            Element element = iterator.next();
            switch(element.getName()){
                case"controller":{
                    String name = element.element("name").getText();
                    String cls = element.element("class").getText();
                    webApp.classes.put(name,cls);//把name 和class放入classes的Map中
                    break;
                }
                case"mapping": {
                    String name = element.element("name").getText();
                    String urlPattern = element.element("url-pattern").getText();
                    webApp.mappings.put(name, urlPattern);//把name 和url-pattern放入mapping的Map中
                    break;
                }
            }
        }
                return webApp;
    }

    //传入url
    public Controller findController(String url){
        String name = findName(url);//返回Post
        if(name == null){
            return null;
        }
        Class<? extends Controller> cls = findClass(name);//传入Post，找到PostController
        if(cls == null){
            return null;
        }

        Controller controller = objects.get(cls);//根据cls

        if(controller == null){
            controller = initializeController(cls);
            controller.init();
        }
        return controller;
    }

    public void destroy(){
        for(Controller controller : objects.values()){
            controller.destroy();
        }
    }


    public Controller initializeController(Class<? extends Controller> cls){
        try{
            Controller controller = (Controller)cls.newInstance();//初始化类的对象
            objects.put(cls,controller);
            return controller;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    //传入Post，找到PostController，加载PostController.class
    private Class<? extends Controller> findClass(String name){
        String className = classes.get(name);
        if(className == null){
            return null;
        }
        Class<?> cls;
        try{
            cls = new JMClassLoader().loadClass(className);//className是PostController
            //cls是一个类
        }catch (ClassNotFoundException e){
            return null;
        }
        if(!Controller.class.isAssignableFrom(cls)){
            return null;
        }
        return (Class<? extends Controller>)cls;
    }

    //根据url返回Post
    private String findName(String url){
        for(Map.Entry<String,String> entry:mappings.entrySet()){
            Pattern pattern = Pattern.compile(entry.getValue());
            Matcher matcher = pattern.matcher(url);
            if(matcher.matches()){
                return entry.getKey();
            }
        }
        return null;
    }

}
