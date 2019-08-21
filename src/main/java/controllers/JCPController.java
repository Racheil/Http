package controllers;

import http.Controller;
import http.Request;
import http.Response;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JCPController extends Controller {
    @Override
    public void doGet(Request request, Response response)throws IOException{
        Jedis jedis = new Jedis("localhost");
        List<String> ids = jedis.lrange("article_id_list",0,-1);
        List<String[]> articles = new ArrayList<>();

        for(String id:ids){
            String key = String.format("article_(%s",id);
            String author = jedis.hget(key,"author");
            String title =jedis.hget(key,"title");
            String date = jedis.hget(key,"date");
            articles.add(new String[]{id,author,title,date});
        }
        response.println("<!DOCTYPE html>");
        response.println("<html lang=\"zh\">");
        response.println("<head>");
        response.println("<meta charset=\"UTF-8\"/>");
        response.println("<title>我的博客</title>");
        response.println("</head>");
        response.println("<body>");
        response.println("<h1>文章列表</h1>");
        response.println("<ol>");
        for(String[] a:articles) {
            response.print("<li><a href='/article.jcp?id=");
            response.print(a[0]);
            response.print("'>");
            response.print(a[2]);
            response.println("</a></li>");
        }
        response.println("</ol>");
        response.println("</body>");
        response.println("</html");
    }
}
