package controllers;

import http.Controller;
import http.MIMEType;
import http.Request;
import http.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

public class StaticController extends Controller {
 private static final String HOME = System.getenv("JERRYMOUSE_HOME");

    @Override
    public void doGet(Request request, Response response)throws IOException {
        String filename = getFilename(request.getUrl());

        String contentType = MIMEType.getInstance().getContentType(filename);
        response.setContentType(contentType);
        try(InputStream is = new FileInputStream(filename)){
            byte[] buf = new byte[1024];
            int len;
            while((len = is.read(buf))!= -1){
                response.write(buf,len);
            }
        }
 }

  private String getFilename(String url)throws IOException{
        if(url.equals("/")){
            url = "/index.html";
        }
        return HOME + File.separator+"webapps"+URLDecoder.decode(url,"UTF-8").replace("/",File.separator);
  }
}
