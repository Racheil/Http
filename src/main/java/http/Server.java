package http;

import controllers.JCPController;
import controllers.StaticController;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;

public class Server {
    private static final String HOME = System.getenv("JERRYMOUSE_HOME");
    private final Controller staticController = new StaticController();
    private JCPController jcpController = new JCPController();
    private final WebApp webApp = WebApp.fromWebXML();//找到对应的controller和mapping

    public Server() throws DocumentException, IOException {
    }

    public void run(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Request request = Request.parse(socket.getInputStream());
                Response response = Response.build(socket.getOutputStream());
                Controller controller =null;

                if(isJCP(request)){
                    controller = jcpController;
                }else if(hasStatic(request)){
                    controller = staticController;
                }else{
                    controller = webApp.findController(request.getUrl());
                }
                if (controller == null) {
                    response.setStatus(Status.Not_Found);
                    response.println(Status.Not_Found.getReason());
                    return;
                }

                if(request.getMethod().equals("GET")) {
                    controller.doGet(request, response);
                }else if(request.getMethod().equals("POST")){
                    controller.doPost(request, response);
                }else{
                    response.setStatus(Status.Method_Not_Allowed);
                    response.println(Status.Method_Not_Allowed.getReason());
                }
                response.flush();
                socket.close();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isJCP(Request request){
        return request.getUrl().endsWith(".jcp");
    }

    private boolean hasStatic(Request request)throws IOException{
        String filename = HOME+File.separator+"webapps"+URLDecoder.decode(request.getUrl(),"UTF-8").replace("/",File.separator);
        File file = new File(filename);
        return file.exists();
    }

    public static void main(String[] args) throws DocumentException, IOException {
        new Server().run(8080);
    }



}