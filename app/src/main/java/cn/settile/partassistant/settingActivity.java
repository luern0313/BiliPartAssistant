package cn.settile.partassistant;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.net.URISyntaxException;

/**
 * Created by liupe on 2018/7/21.
 */

public class settingActivity extends AppCompatActivity
{
    Context ctx;
    ListView setList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ctx = this;
        setList = findViewById(R.id.set_listview);
        final String[] menuText = new String[]{"给个好评&检查更新", "操作流程", "联系作者", "捐赠作者", "关于开源"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        adapter.addAll(menuText);
        setList.setAdapter(adapter);

        setList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                switch (menuText[position])
                {
                    case "给个好评&检查更新":
                        Uri uri = Uri.parse("market://details?id=" + "cn.settile.partassistant");
                        Intent markerintent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(markerintent);
                        break;
                    case "操作流程":
                        new AlertDialog.Builder(ctx).setMessage("进入应用，输入uid，在收藏夹中选择需要检查和提醒分P的视频，点击确定\n在桌面创建桌面小部件，系统会每30分钟检查一次，你也可以手动检查视频有无更新新章节\n如有更新，会在小部件显示").setPositiveButton("确定", null).show();
                        break;
                    case "联系作者":
                        ImageView imageView = new ImageView(ctx);
                        imageView.setImageResource(R.drawable.img_qrcode);
                        new AlertDialog.Builder(ctx).setMessage("如果你不会用/发现bug\n欢迎加入软件交流群！\nqq群：680497357\nbilibili：luern0313").setView(imageView).setPositiveButton("确定", null).show();
                        break;
                    case "捐赠作者":
                        new AlertDialog.Builder(ctx)
                                .setMessage("如你认为本应用好用，欢迎捐赠来支持我~")
                                .setNegativeButton("支付宝", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        try
                                        {
                                            String ailpay = "intent://platformapi/startapp?saId=10000007&" +
                                                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX03813TCF0WXJ1KA6XEA%3F_s" +
                                                "%3Dweb-other&_t=1472443966571#Intent;" +
                                                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
                                            Intent ailintent = Intent.parseUri(ailpay, Intent.URI_INTENT_SCHEME);
                                            startActivity(ailintent);
                                        } catch (URISyntaxException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .setPositiveButton("微信", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        ImageView imageView = new ImageView(ctx);
                                        imageView.setImageResource(R.drawable.img_wxpay);
                                        new AlertDialog.Builder(ctx).setMessage("你可以扫码来使用微信支付").setView(imageView).setPositiveButton("确定", null).show();
                                    }
                                })
                                .setNeutralButton("关闭", null).show();
                        break;
                    case "关于开源":
                        new AlertDialog.Builder(ctx).setMessage("本应用源码在github开源~\n欢迎点star&帮助修改代码~\n地址：https://github.com/luern0313/BiliPartAssistant\n（在github搜索BiliPartAssistant）").setPositiveButton("确定", null).show();
                        break;
                }
            }
        });

    }
}
