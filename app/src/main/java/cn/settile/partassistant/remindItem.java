package cn.settile.partassistant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by liupe on 2018/7/13.
 */

public class remindItem extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
    {
        private Context mContext;

        public ListRemoteViewsFactory(Context context, Intent intent)
        {
            mContext = context;
        }

        @Override
        public void onCreate()
        {

        }

        @Override
        public void onDataSetChanged()
        {

        }



        @Override
        public void onDestroy()
        {

        }

        @Override
        public int getCount()
        {
            return remindWidget.updateVid.size();
        }

        @Override
        public RemoteViews getViewAt(int position)
        {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_remind);

            Log.i("p", "getview");
            views.setTextViewText(R.id.widit_up, Html.fromHtml("<b>" + remindWidget.updateVidUp.get(position) + "</b>更新了新章节：<br/>" + remindWidget.updateVidPart.get(position)));
            views.setTextViewText(R.id.widit_title, (String) remindWidget.updateVidTitle.get(position));
            views.setTextViewText(R.id.widit_upp, "UP : " + remindWidget.updateVidUp.get(position));
            views.setTextViewText(R.id.widit_play, "播放 : " + remindWidget.updateVidPlay.get(position));
            views.setTextViewText(R.id.widit_danmu, "弹幕 : " + remindWidget.updateVidDanmu.get(position));
            try
            {
                views.setImageViewBitmap(R.id.widit_img, remindWidget.updateVidImg.get(position));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            //views.setImageViewBitmap(R.id.widit_img, downloadImage(nowVideo.img));
            //views.setString(R.id.widit_img, "Tag", nowVideo.img);

            Bundle extras = new Bundle();
            extras.putInt("position", position);
            Intent changeIntent = new Intent();
            changeIntent.setAction("cn.settile.partassistant.widget.click");
            changeIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.widit_alllay, changeIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView()
        {
            return null;
        }

        @Override
        public int getViewTypeCount()
        {
            return 1;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public boolean hasStableIds()
        {
            return false;
        }
    }
}
