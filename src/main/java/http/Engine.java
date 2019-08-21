package http;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Engine {
    private static final String HOME = System.getenv("JERRYMOUSE_HOME");
    private static final String JAVAC = System.getenv("JAVA_HOME")+ File.separator+"bin"+File.separator+"javac";
    private static final String SOURCE_TEMPLATE_FILENAME="JCP.java.template";
    private final String sourceTemplate;
    private final StringBuilder importBuilder = new StringBuilder();
    private final StringBuilder bodyBuilder = new StringBuilder();
    private boolean inText =true;
    private final ClassLoader classLoader = new JCPClassLoader();


    public Engine()throws IOException {
        try(InputStream is = getClass().getClassLoader().getResource(SOURCE_TEMPLATE_FILENAME).openStream()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            sourceTemplate = reader.lines().reduce((s1,s2)->String.format("%S%n%s",s1,s2)).get();
        }
    }

    public Class<?> compile(String jcpFilename)throws IOException{
        InputStream is = new FileInputStream(combineJcpFilename(jcpFilename));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = reader.readLine()) !=null){
            process(line);
        }
        String source = sourceTemplate;
        String name = jcpFilename;
        name = name.replace(".","_");
        name = name.replace("/","_");
        source = source.replace("${name}",name);
        source = source.replace("${import}",importBuilder.toString());
        source = source.replace("${body}",bodyBuilder.toString());

        try{
            return generate(jcpFilename,name,source);
        }catch (ClassNotFoundException|InterruptedException e){
            throw new IOException(e);
        }
    }

    private Class<?> generate(String jcpFilename,String name,String source)throws IOException,ClassNotFoundException,InterruptedException{
        String sourceFilename = HOME+File.separator+"work"+File.separator+name+".java";
        try(OutputStream os =new FileOutputStream(sourceFilename)){
            os.write(source.getBytes("UTF-8"));
        }

        String command = String.format("%s -encoding UTF-8 %s",JAVAC,sourceFilename);
        Process process = Runtime.getRuntime().exec(command);
        int r= process.waitFor();
        if(r!=0){
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line ;
            while((line = reader.readLine()) !=null){
                System.out.println(line);
            }
        }
        return classLoader.loadClass(name);
    }


    private void process(String line){
        if(inText) {
            if (line.trim().startsWith("<@ page ")) {
                processDirective(line);
            } else if (line.trim().equals("<%")) {
                inText = false;
            } else {
                processText(line);
            }
        }else{
            if(line.trim().equals("%>")){
                inText = true;
            }else{
                processCode(line);
            }
        }
    }

    private void processCode(String line){
        bodyBuilder.append(String.format("%s%n",line));
    }

    private void processText(String line){
        line = line.replace("\"","\\\"");
        Pattern pattern = Pattern.compile("<%= [^ ]+ %>");
        Matcher matcher = pattern.matcher(line);

        int start = 0;
        while(matcher.find()){
            String text = line.substring(start,matcher.start());
            bodyBuilder.append(String.format("response.print(\"%s\");%n" ,text));
        String variable = matcher.group().substring(4,matcher.group().length()-3);
        bodyBuilder.append(String.format("response.print(%s);%n" ,variable));
        start = matcher.end();
        }
        bodyBuilder.append(String.format("response.println(\"%s\");%n" ,line.substring(start)));
    }

    private void processDirective(String line){
        String[] imports = line.substring(17,line.length()-4).split(",");
        for(String ipt : imports){
            importBuilder.append(String.format("import %s;%n", ipt.trim()));
        }
    }

    private String combineJcpFilename(String jcpFilename){
        return HOME +File.separator+"webapp"+File.separator+jcpFilename;
    }
}









