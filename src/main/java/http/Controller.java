package http;

import java.io.IOException;

public abstract class Controller {
    public void init(){
    }
    public void destroy(){
    }
    public void doGet(Request request, Response response)throws IOException{
        if(request.getProtocol().endsWith("1.1")){
            response.setStatus(Status.Method_Not_Allowed);
            response.println(Status.Method_Not_Allowed.getReason());
        }else{
            response.setStatus(Status.Bad_Request);
            response.println(Status.Bad_Request.getReason());
        }
    }


    public void doPost(Request request, Response response)throws IOException{
              doGet(request,response);
    }
}
