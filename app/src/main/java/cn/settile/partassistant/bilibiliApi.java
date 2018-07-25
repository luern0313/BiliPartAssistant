package cn.settile.partassistant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liupe on 2018/7/16.
 */

public class bilibiliApi
{
    public static String getFav(String uid)
    {
        try
        {
            String result = httpGet("https://api.bilibili.com/x/space/fav/nav?mid=" + uid);
            int code = (int) new JSONObject(result).get("code");
            if(code == 0)
            {
                return result;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFavVideo(String uid, Object fid, Object page)
    {
        try
        {
            String result = httpGet("https://api.bilibili.com/x/v2/fav/video?vmid=" + uid + "&ps=30&fid=" + fid + "&tid=0&keyword=&pn=" + page + "&order=fav_time&jsonp=jsonp");
            int code = (int) new JSONObject(result).get("code");
            if(code == 0)
            {
                return result;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVideoPartList(String vid)
    {
        try
        {
            String result = httpGet("https://api.bilibili.com/x/player/pagelist?aid=" + vid);
            int code = (int) new JSONObject(result).get("code");
            if(code == 0)
            {
                return result;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVideoInfo(String vid)
    {
        try
        {
            String result = httpGet("https://api.bilibili.com/x/web-interface/archive/stat?aid=" + vid);
            int code = (int) new JSONObject(result).get("code");
            if(code == 0)
            {
                return result;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVideoPage(String vid)
    {
        try
        {
            String result = httpGet("https://www.bilibili.com/video/av" + vid + "/?redirectFrom=h5");
            return result;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isUidExist(String uid)
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                    .build();
            Request request = new Request.Builder().url("https://m.bilibili.com/space/" + uid)
                    .header("Referer", "https://www.bilibili.com/")
                    .addHeader("Connection", "close")
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19").build();
            Response response = client.newCall(request).execute();
            if(response.code() == 200)
                return true;
            return false;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static String httpGet(String url)
    {
        try
        {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
                    .readTimeout(10, TimeUnit.SECONDS)//设置读取超时时间
                    .build();
            Request request = new Request.Builder().url(url).header("Referer", "https://www.bilibili.com/").addHeader("Connection", "close").build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful())
            {
                return response.body().string();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
