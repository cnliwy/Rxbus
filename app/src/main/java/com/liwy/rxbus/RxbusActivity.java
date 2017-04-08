package com.liwy.rxbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.R.attr.tag;

public class RxbusActivity extends AppCompatActivity {
    public static final String rxbusTag = "rxbus";
    Observable<String> observableRxbus;
    private TextView tvContent;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rxbus);
        tvContent = (TextView)findViewById(R.id.tv_content_rxbus);
        sendBtn = (Button)findViewById(R.id.btn_rxbus);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此时main观察者已经注册，直接发送
                RxBus.getInstance().post(MainActivity.mainTag,"hello,main,i'm RxbusActivity");
                finish();
            }
        });
        initEvent();
    }


    // 注册监听事件
    void initEvent(){
        observableRxbus = RxBus.getInstance().register(rxbusTag,String.class);
        observableRxbus.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                tvContent.setText(s);
            }
        });
        RxBus.getInstance().getData(rxbusTag);//拉去消息
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(rxbusTag,observableRxbus);//取消注册，否则内存泄漏
    }
}
