package cn.settile.partassistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{
    Context ctx;
    public static SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    LinearLayout mainLoading;
    Button mainYes;
    EditText mainInputuid;
    public Handler handler;
    public Runnable runnableUi;
    public Runnable jsonUi;

    public String jsonData;
    public static HashMap<Integer, Integer> favorList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;
        handler = new Handler();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mainLoading = findViewById(R.id.main_loading);
        mainInputuid = findViewById(R.id.main_uidinput);
        mainYes = findViewById(R.id.main_yes);

        favorList = new HashMap<Integer, Integer>();

        if(!sharedPreferences.getString("uid", "").equals(""))
        {
            mainInputuid.setText(sharedPreferences.getString("uid", ""));
            mainInputuid.setEnabled(false);
            mainYes.setText("修改提醒视频");
        }

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                mainLoading.setVisibility(View.GONE);
            }

        };

        jsonUi = new Runnable()
        {
            @Override
            public void run()
            {
                JSONObject jsonObject = null;
                //Log.i("partassistant", jsonData);
                try
                {
                    jsonObject = new JSONObject(jsonData);
                    if((int) jsonObject.get("code") == 0)
                    {
                        Intent intent = new Intent(ctx, favorboxActivity.class);
                        intent.putExtra("data", jsonData);
                        intent.putExtra("uid", mainInputuid.getText().toString());
                        startActivity(intent);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mainbutton, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuSetting:
                Intent intent = new Intent(ctx, settingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void yesClick(View view)
    {
        mainLoading.setVisibility(View.VISIBLE);
        final String uid = mainInputuid.getText().toString();
        if(!uid.equals(""))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(sharedPreferences.getString("videoList", "").equals(""))
                        //第一次进入
                        {
                            if(bilibiliApi.isUidExist(uid))
                            {
                                jsonData = bilibiliApi.getFav(uid);
                                ArrayList page = new ArrayList<Integer>();
                                ArrayList fid = new ArrayList<Integer>();
                                JSONArray data = new JSONObject(jsonData).getJSONObject("data").getJSONArray("archive");
                                for (int i = 0; i < data.length(); i++)
                                {
                                    JSONObject favor = data.getJSONObject(i);
                                    page.add((int) Math.ceil((int) favor.get("cur_count") / 30.0));
                                    fid.add((int) favor.get("fid"));
                                }

                                for (int i = 0; i < fid.size(); i++)
                                //遍历所有收藏夹
                                {
                                    for (int j = 1; j <= (int) page.get(i); j++)
                                    //遍历收藏夹的每个30页
                                    {
                                        String result = bilibiliApi.getFavVideo(uid, fid.get(i), j);
                                        JSONObject jdata = new JSONObject(result);
                                        if((int) jdata.get("code") == 0)
                                        {
                                            JSONArray favor = jdata.getJSONObject("data").getJSONArray("archives");
                                            for (int k = 0; k < favor.length(); k++)
                                            //遍历每条视频
                                            {
                                                if(((int) favor.getJSONObject(k).get("videos")) > 1 && (int) favor.getJSONObject(k).get("state") >= 0)
                                                    favorList.put(((int) favor.getJSONObject(k).get("aid")), (int) favor.getJSONObject(k).get("videos"));
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                Looper.prepare();
                                new AlertDialog.Builder(ctx).setMessage("uid不存在！请检查你的uid!").setPositiveButton("确定", null).show();
                                handler.post(runnableUi);
                                Looper.loop();
                            }
                        }
                        else
                        {
                            favorList = new HashMap<Integer, Integer>();
                            String list = sharedPreferences.getString("videoList", "[]");
                            ArrayList<String> vlist = list.equals("[]") ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(list.substring(1, list.length() - 1).split(", ")));
                            String partlist = sharedPreferences.getString("videoPartList", "[]");
                            ArrayList<String> vplist = partlist.equals("[]") ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(partlist.substring(1, partlist.length() - 1).split(", ")));
                            for (int i = 0; i < vlist.size(); i++)
                                favorList.put(Integer.parseInt(vlist.get(i)), Integer.parseInt(vplist.get(i)));
                        }
                        handler.post(runnableUi);
                        handler.post(jsonUi);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        else
        {
            new AlertDialog.Builder(ctx).setMessage("请输入你的uid！").setPositiveButton("确定", null).show();
            handler.post(runnableUi);
        }
    }

    public void whatuidClick(View view)
    {
        new AlertDialog.Builder(ctx).setMessage("如何查看UID？\n\n网页端：\n进入个人中心，uid在右下方。\n\n手机端：\n点击头像进入个人空间，uid在右上角的账号资料中\n\n（只能输入一次，请谨慎输入，要更改请情书应用数据或卸载重新安装）").setPositiveButton("确定", null).show();
    }
}
