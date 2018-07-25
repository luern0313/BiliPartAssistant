package cn.settile.partassistant;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

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
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        adapter.addAll("操作流程", "联系作者");
        setList.setAdapter(adapter);

        setList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(position == 0)
                {
                    new AlertDialog.Builder(ctx)
                            .setMessage("进入应用，输入uid，在收藏夹中选择需要检查和提醒分P的视频，点击确定\n在桌面创建桌面小部件，系统会每30分钟检查一次，你也可以手动检查视频有无更新新章节\n如有更新，会在小部件显示")
                            .setPositiveButton("确定", null).show();
                }
                else if(position == 1)
                {
                    ImageView imageView = new ImageView(ctx);
                    imageView.setImageResource(R.drawable.img_qrcode);
                    new AlertDialog.Builder(ctx)
                            .setMessage("欢迎加入软件交流群！\nqq群：680497357\nbilibili：luern0313")
                            .setView(imageView)
                            .setPositiveButton("确定", null).show();
                }
            }
        });

    }
}
