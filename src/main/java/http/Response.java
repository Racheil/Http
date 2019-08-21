package http;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class Response {
   private final byte[] buf = new byte[8192];
   private int contentLength = 0;
   private OutputStream os;
   private Map<String,String> headers = new HashMap<>();
   private boolean hasSentHeaders = false;
   private Status status = Status.OK;

  public static Response build(OutputStream os){
       Response response = new Response();
       response.setOutputStream(os);
       response.setContentType("text/html");
       response.setDate();
       response.setHeader("Server","jerrymouse/1.0.0");
       return response;
   }
   private void setOutputStream(OutputStream os){
       this.os = os;
   }
   private void setDate(){
       SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",Locale.ENGLISH);
       String value = sdf.format(new Date());
       setHeader("Date",value);
   }

   public void setStatus(Status status){
       if(hasSentHeaders){
           throw new RuntimeException("Response headers 已经发送");
       }
       this.status = status;
   }

   public  void setContentType(String contentType){
       setHeader("Content-type",contentType+"; charset=UTF-8");
   }

   public void setHeader(String header,String value){
       if(hasSentHeaders){
           throw new RuntimeException("Response headers 已经发送");
       }
       headers.put(header,value);
   }

   public void write(byte[] b,int off,int len)throws IOException {
       if(contentLength +len >8192){
           throw new RuntimeException("Response 支持的最大长度只有8192");
       }
       System.arraycopy(b,off,buf,contentLength,len);
       contentLength += len;
   }

   public void write(byte[] b ,int len)throws IOException{
       write(b,0,len);
   }

   public void write(byte[] b)throws IOException{
       write(b,b.length);
   }

   public void printf(String format,Object... args)throws IOException{
       write(new Formatter().format(format,args).toString().getBytes("UTF-8"));
   }

   public void format(String format,Object... args)throws IOException{
       write(new Formatter().format(format,args).toString().getBytes("UTF-8"));
   }

   public void print(Object o)throws IOException{
       format("%S",o.toString());
   }

   public void println(Object o)throws IOException{
       format("%s%n",o.toString());
   }

   public void flush() throws IOException{
       if(!hasSentHeaders){
           sendResponseLine();
           sendResponseHeaders();
           hasSentHeaders = true;
       }
       os.write(buf,0,contentLength);
       os.flush();
   }

   public void sendResponseLine() throws IOException{
       String responseLine = String.format("HTTP/1.0 %d %s\r\n",status.getCode(),status.getReason());
       os.write(responseLine.getBytes("UTF-8"));
   }

   public void sendResponseHeaders()throws IOException{
       for(Map.Entry<String,String> entry:headers.entrySet()){
           String headerLine = String.format("%s: %s\r\n",entry.getKey(),entry.getValue());
           os.write(headerLine.getBytes("UTF-8"));
       }
       os.write("\r\n".getBytes("UTF-8"));
   }
}
