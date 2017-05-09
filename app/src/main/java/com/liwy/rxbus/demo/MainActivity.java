package com.liwy.rxbus.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liwy.rxbus.R;
import com.liwy.rxbus.RxBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.liwy.rxbus.demo.TestService.EVENT_SERVICE;

public class MainActivity extends AppCompatActivity {
    public static final String EVENT_MAIN = "main";
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
                // 给TestService发送消息，此时尚未启动，在RxbusAccitivy里启动。
                RxBus.getInstance().postDelayed(EVENT_SERVICE,"hello,service,i'm MainActivity");
                // 此时rxbus观察者尚未注册，采用基于缓存的post发送
                RxBus.getInstance().postDelayed(RxbusActivity.EVENT_RXBUS,"hello,rxbus,i'm MainActivity");
                //启动Activity
                Intent intent = new Intent(MainActivity.this,RxbusActivity.class);
                startActivity(intent);
            }
        });
        initEvent();
    }

    // 注册监听事件
    void initEvent(){
        observableMain = RxBus.getInstance().register(EVENT_MAIN,String.class);
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
        RxBus.getInstance().unregister(EVENT_MAIN,observableMain);//取消注册，否则内存泄漏
    }
}
