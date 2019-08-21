package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public final  class Request {
   private InputStream is;
   private String method;
   private String url;
   private String protocol;
   private Map<String,String> requestParams = new HashMap<>();
   private Map<String,String> headers = new HashMap<>();

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRequestParams(String name) {
        return requestParams.get(name);
    }

    public String getHeaders(String header) {
        return headers.get(header.toUpperCase());
    }


    public static Request parse(InputStream is)throws IOException {
     Request request = new Request();
     request.is = is;
     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
     parseRequestLine(reader,request);
     if(request.method.equals("POST")){
         parseRequestBody(reader,request);
     }
     return request;
    }

    private static void parseRequestBody(BufferedReader reader, Request request) throws IOException {
        if (request.getHeaders("Content-Type").toLowerCase().equals("application/x-www-form-urlencoded")) {
            int len = Integer.parseInt(request.getHeaders("Content-Length"));
            System.out.println(len);
            char[] buf = new char[len];
            reader.read(buf, 0, len);
            request.setRequestParams(new String(buf));
        }
    }

    private static void parseRequestHeaders(BufferedReader reader, Request request)throws IOException{
        String line;
        while((line = reader.readLine().trim()) != null &&  line.length() !=0){
            String[] kv = line.split(":");
            String header = kv[0].trim().toUpperCase();
            String value = kv[1].trim();
            request.headers.put(header,value);
        }
    }
    private void setRequestParams(String s) {
    }

    private static void parseRequestLine(BufferedReader reader, Request request) throws IOException{
        String line = reader.readLine();
        if(line == null){
            throw new IOException("对方提前关闭了连接");
        }
        String[] fragments = line.split(" ");
        if(fragments.length != 3){
            throw new IOException("错误的请求行");
        }
        request.setMethod(fragments[0].trim().toUpperCase());
        request.setUrl(fragments[1].trim());
        request.setProtocol(fragments[2].trim());
    }

    private void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    private void setUrl(String requestUrl)throws IOException{
        String[] fragments = requestUrl.split("\\?");
        this.url = URLDecoder.decode(fragments[0].trim(),"UTF-8");
        if(fragments.length == 2){
            setRequestParams(fragments[1].trim());
        }
    }

    private void setMethod(String method)throws IOException {
        if(method.equals("GET")|| method.equals("POST")){
            this.method = method;
            return ;
        }
        throw new IOException("不支持的方法："+method);
    }


}
