package cn.settile.partassistant;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implementation of App Widget functionality.
 */
public class remindWidget extends AppWidgetProvider
{
    private RemoteViews mRemoteViews;
    private ComponentName mComponentName;
    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    int[] mAppWidgetIds;

    Intent lvIntent;

    public static ArrayList updateVid = new ArrayList();
    public static ArrayList updateVidPart = new ArrayList();
    public static ArrayList updateVidUp = new ArrayList();
    public static ArrayList<Bitmap> updateVidImg = new ArrayList();
    public static ArrayList updateVidTitle = new ArrayList();
    public static ArrayList updateVidPlay = new ArrayList();
    public static ArrayList updateVidDanmu = new ArrayList();

    //当小部件被添加时或者每次小部件更新时都会调用一次该方法，配置文件中配置小部件的更新周期updatePeriodMillis，每次更新都会调用
    //对应广播 Action 为：ACTION_APPWIDGET_UPDATE 和 ACTION_APPWIDGET_RESTORED
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        Log.i("p", "onUpdate被调用开始");
        mAppWidgetIds = appWidgetIds;
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_remind);

        sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //过去更新的记录和分p和up
        String vid = sharedPreferences.getString("updateVid", "[]");
        updateVid = vid.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vid.substring(1, vid.length() - 1).split(", ")));
        String vidup = sharedPreferences.getString("updateVidUp", "[]");
        updateVidUp = vidup.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vidup.substring(1, vidup.length() - 1).split(", ")));
        String vidpart = sharedPreferences.getString("updateVidPart", "[]");
        updateVidPart = vidpart.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vidpart.substring(1, vidpart.length() - 1).split(", ")));
        //updateVidImg = new ArrayList<Bitmap>();
        //updateVidImg = vidimg.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vidimg.substring(1, vidimg.length() - 1).split(", ")));
        String vidtitle = sharedPreferences.getString("updateVidTitle", "[]");
        updateVidTitle = vidtitle.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vidtitle.substring(1, vidtitle.length() - 1).split(", ")));
        String vidplay = sharedPreferences.getString("updateVidPlay", "[]");
        updateVidPlay = vidplay.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(vidplay.substring(1, vidplay.length() - 1).split(", ")));
        String viddanmu = sharedPreferences.getString("updateVidDanmu", "[]");
        updateVidDanmu = viddanmu.equals("[]") ? new ArrayList() : new ArrayList<String>(Arrays.asList(viddanmu.substring(1, viddanmu.length() - 1).split(", ")));

        mRemoteViews.setProgressBar(R.id.wid_seekbar, 100, 0, false);
        mRemoteViews.setViewVisibility(R.id.wid_refresh, View.GONE);
        mRemoteViews.setViewVisibility(R.id.wid_loading, View.VISIBLE);
        mRemoteViews.setViewVisibility(R.id.wid_seekbar, View.VISIBLE);
        if(updateVid.size() == 0)
            mRemoteViews.setViewVisibility(R.id.wid_hint, View.VISIBLE);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                update(context, mRemoteViews);
            }
        }).start();

        lvIntent = new Intent(context, remindItem.class);
        mRemoteViews.setRemoteAdapter(R.id.wid_listview, lvIntent);
        mRemoteViews.setEmptyView(R.id.wid_listview, android.R.id.empty);

        Intent refIntent = new Intent(context, remindWidget.class);
        refIntent.setAction("cn.settile.partassistant.widget.refresh");
        PendingIntent refPi = PendingIntent.getBroadcast(context, 0, refIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.wid_refresh, refPi);

        Intent setIntent = new Intent(context, settingActivity.class);
        PendingIntent setPi = PendingIntent.getActivity(context, 0, setIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.wid_setting, setPi);

        Intent toIntent = new Intent(context, remindWidget.class);
        toIntent.setAction("cn.settile.partassistant.widget.click");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, toIntent, 0);
        mRemoteViews.setPendingIntentTemplate(R.id.wid_listview, pendingIntent);

        mComponentName = new ComponentName(context, remindWidget.class);
        appWidgetManager.updateAppWidget(mComponentName, mRemoteViews);
    }

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        super.onReceive(context, intent);
        switch (intent.getAction())
        {
            case "cn.settile.partassistant.widget.refresh":
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                onUpdate(context, appWidgetManager, null);
                break;
            case "cn.settile.partassistant.widget.click":
                Uri uri = Uri.parse("http://www.bilibili.com/video/av" + updateVid.get(intent.getExtras().getInt("position")) + "?share_medium=android&share_source=qq");
                Intent yintent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(yintent);
                break;
        }

    }

    private void update(Context context, RemoteViews remoteViews)
    {
        try
        {
            //储存的要检查更新的视频和当前的分p
            String vidd = sharedPreferences.getString("videoList", "[]");
            String[] vidlist = vidd.substring(1, vidd.length() - 1).split(", ");
            String videopartt = sharedPreferences.getString("videoPartList", "[]");
            ArrayList<String> vidpartlist = new ArrayList<String>(Arrays.asList(videopartt.substring(1, videopartt.length() - 1).split(", ")));

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, remindWidget.class);

            for (int i = 0; i < vidlist.length; i++)
            {
                try
                {
                    JSONArray json = new JSONObject(bilibiliApi.getVideoPartList(vidlist[i])).getJSONArray("data");
                    if(json.length() > Integer.valueOf(vidpartlist.get(i)))
                    {
                        Elements tempHead = Jsoup.parse(bilibiliApi.getVideoPage((String) vidlist[i])).head().getElementsByTag("meta");
                        JSONObject tempInfo = new JSONObject(bilibiliApi.getVideoInfo((String) vidlist[i])).getJSONObject("data");
                        updateVid.add(0, vidlist[i]);
                        ArrayList<String> string = new ArrayList<String>();
                        for (int j = Integer.valueOf(vidpartlist.get(i)); j < json.length(); j++)
                        {
                            string.add("P" + (j + 1) + " : " + json.getJSONObject(j).get("part"));
                        }
                        updateVidPart.add(0, join(string.toArray(new String[]{}), "<br/>"));
                        updateVidUp.add(0, tempHead.select("meta[itemprop=author]").attr("content"));

                        updateVidTitle.add(0, tempHead.select("meta[itemprop=keywords]").attr("content").split(",")[0]);
                        updateVidPlay.add(0, favorvidActivity.numSimplify((int) tempInfo.get("view")));
                        updateVidDanmu.add(0, favorvidActivity.numSimplify((int) tempInfo.get("danmaku")));
                        vidpartlist.set(i, String.valueOf(json.length()));
                    }
                    mRemoteViews.setProgressBar(R.id.wid_seekbar, 100, Math.round(100 * i / vidlist.length), false);
                    appWidgetManager.updateAppWidget(componentName, remoteViews);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    continue;
                }
            }
            if(updateVid.size() > 30)
            //只保留前三十个
            {
                for (int k = 30; k < updateVid.size();)
                {
                    updateVid.remove(k);
                    updateVidPart.remove(k);
                }
            }
            for (int i = 0; i < updateVid.size(); i++)
            {
                Elements tempHead = Jsoup.parse(bilibiliApi.getVideoPage((String) updateVid.get(i))).head().getElementsByTag("meta");
                updateVidImg.add(downloadImage(tempHead.select("meta[itemprop=image]").attr("content")));
            }
            editor.putString("updateVid", updateVid.toString());
            editor.putString("updateVidPart", updateVidPart.toString());
            editor.putString("updateVidPlay", updateVidPlay.toString());
            editor.putString("updateVidTitle", updateVidTitle.toString());
            //editor.putString("updateVidImg", updateVidImg.toString());
            editor.putString("updateVidUp", updateVidUp.toString());
            editor.putString("updateVidDanmu", updateVidDanmu.toString());

            editor.putString("videoPartList", vidpartlist.toString());
            editor.commit();

            if(updateVid.size() > 0)
                mRemoteViews.setViewVisibility(R.id.wid_hint, View.VISIBLE);
            mRemoteViews.setViewVisibility(R.id.wid_refresh, View.VISIBLE);
            mRemoteViews.setViewVisibility(R.id.wid_loading, View.GONE);
            mRemoteViews.setViewVisibility(R.id.wid_seekbar, View.GONE);
            appWidgetManager.updateAppWidget(componentName, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetIds, R.id.wid_listview);
            Log.i("P", "done");
        } catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private Bitmap downloadImage(String imageUrl)
    {
        HttpURLConnection con = null;
        Bitmap bitmap = null;
        try
        {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if(con != null)
            {
                con.disconnect();
            }
        }

        return bitmap;
    }

    //当小部件第一次被添加到桌面时回调该方法，可添加多次，但只在第一次调用
    //对用广播的 Action 为 ACTION_APPWIDGET_ENABLE。
    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    //当最后一个该类型的小部件从桌面移除时调用
    //对应的广播的 Action 为 ACTION_APPWIDGET_DISABLED
    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static String join(String[] strs, String splitter)
    {
        if(strs.length != 0)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(strs[0]);
            for (int i = 1; i < strs.length; i++)
            {
                sb.append(splitter + strs[i]);
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }
}