package quant.testclient.model;

import com.google.gson.Gson;


/**
 * Created by cz on 2017/2/6.
 */

public class Json {
    public static final Gson gson=new Gson();

    public static<T> T getObject(Class<T> clazz,String text){
        T t =null;
        try{
            t = gson.fromJson(text, clazz);
        } catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}
