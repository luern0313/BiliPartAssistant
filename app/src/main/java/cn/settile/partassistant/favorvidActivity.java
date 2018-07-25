package cn.settile.partassistant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class favorvidActivity extends AppCompatActivity
{
    ListView vidListview;
    ArrayList vidImg;
    ArrayList vidAvid;
    ArrayList vidTitle;
    ArrayList vidUp;
    ArrayList vidPlay;
    ArrayList vidPart;
    ArrayList vidstate;
    private Runnable runnableUi;
    private Runnable addlistUi;
    private Handler handler;
    mAdapter adapter;

    int page = 1;
    int totalPage = 0;
    Boolean isLoading = true;

    public int fid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorvideo);

        handler = new Handler();
        vidListview = findViewById(R.id.vid_listview);
        vidImg = new ArrayList<String>();
        vidAvid = new ArrayList<Integer>();
        vidTitle = new ArrayList<String>();
        vidUp = new ArrayList<String>();
        vidPlay = new ArrayList<String>();
        vidPart = new ArrayList<Integer>();
        vidstate = new ArrayList<Boolean>();

        Intent intent = getIntent();
        fid = intent.getIntExtra("data", 0);
        final int count = intent.getIntExtra("count", 0);
        final String uid = intent.getStringExtra("uid");

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONArray favor = new JSONObject(bilibiliApi.getFavVideo(uid, fid, 1)).getJSONObject("data").getJSONArray("archives");
                    for (int i = 0; i < favor.length(); i++)
                    {
                        JSONObject video = favor.getJSONObject(i);
                        vidImg.add(video.get("pic"));
                        vidAvid.add(video.get("aid"));
                        vidTitle.add(video.get("title"));
                        vidUp.add(video.getJSONObject("owner").get("name"));
                        vidstate.add(((int) video.get("state")) >= 0);
                        JSONObject stat = video.getJSONObject("stat");
                        vidPlay.add(numSimplify((int) stat.get("view")));
                        vidPart.add((int) video.get("videos"));
                    }

                    totalPage = (int) Math.ceil(count / 30.0);
                    adapter = new mAdapter(getLayoutInflater(), vidTitle, vidImg, vidUp, vidPlay, vidPart, vidstate, vidAvid);
                    handler.post(runnableUi);
                    isLoading = false;
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        vidListview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int position, long id)
            {
                try
                {
                    if(((CheckBox) v.findViewById(R.id.vid_chbox)).isChecked())
                    {
                        ((CheckBox) v.findViewById(R.id.vid_chbox)).setChecked(false);
                        MainActivity.favorList.remove(vidAvid.get(position));
                    }
                    else
                    {
                        ((CheckBox) v.findViewById(R.id.vid_chbox)).setChecked(true);
                        MainActivity.favorList.put((int) vidAvid.get(position), (int) vidPart.get(position));
                    }
                }
                catch (Exception e)
                {e.printStackTrace();}
            }
        });


        vidListview.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i)
            {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(visibleItemCount + firstVisibleItem == totalItemCount && page < totalPage && !isLoading)
                {
                    page++;
                    isLoading = true;
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                JSONArray favor = new JSONObject(bilibiliApi.getFavVideo(uid, fid, page)).getJSONObject("data").getJSONArray("archives");
                                for (int i = 0; i < favor.length(); i++)
                                {
                                    JSONObject video = favor.getJSONObject(i);
                                    vidImg.add(video.get("pic"));
                                    vidAvid.add(video.get("aid"));
                                    vidTitle.add(video.get("title"));
                                    vidUp.add(video.getJSONObject("owner").get("name"));
                                    vidstate.add(((int) video.get("state")) >= 0);
                                    JSONObject stat = video.getJSONObject("stat");
                                    vidPlay.add(numSimplify((int) stat.get("view")));
                                    vidPart.add((int) video.get("videos"));
                                }
                                handler.post(addlistUi);
                                isLoading = false;
                            } catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        runnableUi = new Runnable()
        {
            @Override
            public void run()
            {
                vidListview.setAdapter(adapter);
            }
        };

        addlistUi = new Runnable()
        {
            @Override
            public void run()
            {
                adapter.notifyDataSetChanged();
            }
        };
    }

    public static String numSimplify(int number)
    {
        if(number >= 10000)
            return number / 1000 / 10.0 + "万";
        return String.valueOf(number);
    }

    class mAdapter extends BaseAdapter
    {
        private ArrayList<String> mTitle;
        private ArrayList<String> mImgurl;
        private ArrayList<String> mUp;
        private ArrayList<String> mPlay;
        private ArrayList<Integer> mPart;
        private ArrayList<Boolean> mState;
        private ArrayList<Integer> mAvid;
        private LayoutInflater mInflater;

        private LruCache<String, BitmapDrawable> mImageCache;

        public mAdapter(LayoutInflater inflater, ArrayList title, ArrayList imgurl, ArrayList up, ArrayList play, ArrayList part, ArrayList state, ArrayList avid)
        {
            mInflater = inflater;
            mTitle = title;
            mImgurl = imgurl;
            mUp = up;
            mPlay = play;
            mPart = part;
            mState = state;
            mAvid = avid;

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
                convertView = mInflater.inflate(R.layout.item_favorvideo, null);
                // 初始化 ViewHolder 方便重用
                viewHolder = new ViewHolder();
                viewHolder.vImg = convertView.findViewById(R.id.vid_img);
                viewHolder.vTitle = convertView.findViewById(R.id.vid_title);
                viewHolder.vUp = convertView.findViewById(R.id.vid_up);
                viewHolder.vPlay = convertView.findViewById(R.id.vid_play);
                viewHolder.vPart = convertView.findViewById(R.id.vid_part);
                viewHolder.vnovid = convertView.findViewById(R.id.vid_novidlayout);
                viewHolder.vnovidimg = convertView.findViewById(R.id.vid_novid);
                viewHolder.vcheckbox = convertView.findViewById(R.id.vid_chbox);
                viewHolder.vnovidimg.setImageResource(R.drawable.animation_novideo);
                viewHolder.anim = (AnimationDrawable) viewHolder.vnovidimg.getDrawable();
                convertView.setTag(viewHolder);
            }
            else
            { // 否则进行重用
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.vTitle.setText(mTitle.get(position));
            viewHolder.vUp.setText("UP : " + mUp.get(position));
            viewHolder.vPlay.setText("播放 : " + mPlay.get(position));
            viewHolder.vPart.setText("分P : " + mPart.get(position));
            viewHolder.vImg.setImageResource(R.drawable.bg_favorboximg);

            if(mImgurl.size() != 0)
            {
                viewHolder.vImg.setTag(mImgurl.get(position));
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

            viewHolder.vcheckbox.setChecked(mState.get(position) && (Boolean) MainActivity.favorList.containsKey(mAvid.get(position)));

            if(!mState.get(position))//false视频消失
            {
                viewHolder.vnovid.setVisibility(View.VISIBLE);
                viewHolder.anim.start();
            }
            else
            {
                viewHolder.vnovid.setVisibility(View.GONE);
                viewHolder.anim.stop();
            }
            return convertView;
        }

        class ViewHolder
        {
            ImageView vImg;
            TextView vTitle;
            TextView vUp;
            TextView vPlay;
            TextView vPart;
            LinearLayout vnovid;
            ImageView vnovidimg;
            CheckBox vcheckbox;
            AnimationDrawable anim;
        }

        class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
        {

            private String imageUrl;

            @Override
            protected BitmapDrawable doInBackground(String... params)
            {
                imageUrl = params[0];
                Bitmap bitmap = downloadImage();
                BitmapDrawable db = new BitmapDrawable(vidListview.getResources(), bitmap);
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
                ImageView iv = (ImageView) vidListview.findViewWithTag(imageUrl);
                if(iv != null && result != null)
                {
                    //Log.i("part", "缓存!!");
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
