package com.liwy.rxbus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.R.attr.tag;
import static com.liwy.rxbus.RxbusActivity.rxbusTag;

public class MainActivity extends AppCompatActivity {
    public static final String mainTag = "main";
    Observable<String> observableMain;
    private TextView tvContent;
    private Button delayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvContent = (TextView)findViewById(R.id.tv_content_main);
        delayBtn = (Button)findViewById(R.id.btn_main);
        delayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此时rxbus观察者尚未注册，采用基于缓存的post发送
                RxBus.getInstance().postWithCache(rxbusTag,"hello,rxbus,i'm MainActivity");
                Intent intent = new Intent(MainActivity.this,RxbusActivity.class);
                startActivity(intent);
            }
        });
        initEvent();
    }

    // 注册监听事件
    void initEvent(){
        observableMain = RxBus.getInstance().register(mainTag,String.class);
        observableMain.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                tvContent.setText(s);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(rxbusTag,observableMain);//取消注册，否则内存泄漏
    }
}
