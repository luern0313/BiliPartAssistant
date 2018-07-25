package cn.settile.partassistant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by liupe on 2018/7/3.
 */

public class favorboxActivity extends AppCompatActivity
{
    Context ctx;
    public Intent intent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ListView boxListview;
    ArrayList<String> imgUrl;
    ArrayList<String> title;
    ArrayList<Integer> count;
    ArrayList<Integer> fid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorbox);

        ctx = this;
        intent = getIntent();
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        title = new ArrayList<>();
        imgUrl = new ArrayList<>();
        count = new ArrayList<>();
        fid = new ArrayList<>();
        boxListview = findViewById(R.id.favor_listview);

        try
        {
            JSONArray data = new JSONObject(intent.getStringExtra("data")).getJSONObject("data").getJSONArray("archive");
            for (int i = 0; i < data.length(); i++)
            {
                JSONObject favor = data.getJSONObject(i);
                title.add((String) favor.get("name"));
                count.add((int) favor.get("cur_count"));
                fid.add((int) favor.get("fid"));
                if(favor.has("cover"))
                    imgUrl.add((String) favor.getJSONArray("cover").getJSONObject(0).get("pic"));
                else imgUrl.add(Math.random() + "");
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        mAdapter adapter = new mAdapter(getLayoutInflater(), title, imgUrl, count);
        //boxListview.addHeaderView(findViewById(R.id.favor_tip), null, false);
        boxListview.setAdapter(adapter);
        //findViewById(R.id.favor_tip).setVisibility(View.GONE);

        boxListview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                Intent i = new Intent(ctx, favorvidActivity.class);
                i.putExtra("data", fid.get(position));
                i.putExtra("count", count.get(position));
                i.putExtra("uid", intent.getStringExtra("uid"));
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_titlebutton, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuHelp:
                new AlertDialog.Builder(ctx).setMessage("请在收藏夹内选中你要提醒的视频\n之后你可以随时在这里修改\n（默认只选中有多P的视频）").setPositiveButton("确定", null).show();
                return true;
            case R.id.menuYes:
                editor.putString("videoList", MainActivity.favorList.keySet().toString());
                editor.putString("videoPartList", MainActivity.favorList.values().toString());
                editor.putString("uid", intent.getStringExtra("uid"));
                editor.commit();
                Log.i("pp", MainActivity.favorList.keySet().toString());
                new AlertDialog.Builder(ctx).setMessage("视频提醒列表已创建完成，系统会定时检查视频是否更新，请在桌面上创建桌面小部件来查看视频更新状态").setPositiveButton("确定", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        System.exit(0);
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class mAdapter extends BaseAdapter
    {
        private ArrayList<String> mTitle;
        private ArrayList<String> mImgurl;
        private ArrayList<Integer> mCount;
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        public mAdapter(LayoutInflater inflater, ArrayList title, ArrayList imgurl, ArrayList count)
        {
            mInflater = inflater;
            mTitle = title;
            mImgurl = imgurl;
            mCount = count;

            int maxCache = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxCache / 8;
            mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
            {
                @Override
                protected int sizeOf(String key, BitmapDrawable value)
                {
                    return value.getBitmap().getByteCount();
                }
            };
        }

        @Override
        public int getCount()
        {
            return mTitle.size();
        }

        @Override
        public Object getItem(int position)
        {
            return position;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup)
        {
            //Log.i("part", position + "," + mImgurl.get(position));
            ViewHolder viewHolder;
            // 若无可重用的 view 则进行加载
            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.item_favorbox, null);
                // 初始化 ViewHolder 方便重用
                viewHolder = new ViewHolder();
                viewHolder.vImg = convertView.findViewById(R.id.favor_img);
                viewHolder.vTitle = convertView.findViewById(R.id.favor_title);
                viewHolder.vCount = convertView.findViewById(R.id.favor_count);
                viewHolder.vCountt = convertView.findViewById(R.id.favor_countt);
                convertView.setTag(viewHolder);
            }
            else
            { // 否则进行重用
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.vTitle.setText(mTitle.get(position));
            viewHolder.vCount.setText(mCount.get(position) + "个内容");
            viewHolder.vCountt.setText(mCount.get(position) + "");
            viewHolder.vImg.setImageResource(R.drawable.bg_favorboximg);
            if(mImgurl.size() != 0)
            {
                viewHolder.vImg.setTag(mImgurl.get(position));
                if(mImgurl.get(position).charAt(0) == 'h')
                {
                    if(mImageCache.get(mImgurl.get(position)) != null)
                    {
                        viewHolder.vImg.setImageDrawable(mImageCache.get(mImgurl.get(position)));
                    }
                    else
                    {
                        ImageTask it = new ImageTask();
                        it.execute(mImgurl.get(position));
                    }
                }
            }
            return convertView;
        }

        class ViewHolder
        {
            ImageView vImg;
            TextView vTitle;
            TextView vCount;
            TextView vCountt;
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {
            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                imageUrl = params[0];
                Bitmap bitmap = downloadImage();
                BitmapDrawable db = new BitmapDrawable(boxListview.getResources(), bitmap);
                // 如果本地还没缓存该图片，就缓存
                if(mImageCache.get(imageUrl) == null)
                {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }

            @Override
            protected void onPostExecute(BitmapDrawable result)
            {
                // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
                ImageView iv = boxListview.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    iv.setImageDrawable(result);
                }
            }

            /**
             * 根据url从网络上下载图片
             *
             * @return
             */
            private Bitmap downloadImage()
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

        }

    }
}
