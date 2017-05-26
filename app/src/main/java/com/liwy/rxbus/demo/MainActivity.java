package com.liwy.rxbus.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liwy.rxbus.PostCallback;
import com.liwy.rxbus.R;
import com.liwy.rxbus.RxBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.liwy.rxbus.demo.TestService.EVENT_SERVICE;

public class MainActivity extends AppCompatActivity {
    public static final String EVENT_MAIN = "main";
    public static final String EVENT_IO_THREAD = "io_thread";
    Observable<String> observableMain;
    Observable<String> observableIoThread;
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
        // 线程切换的三种方式：
        // 第一种：先获取Observable，然后设置事件生产和消费的线程，最后getData获取离线缓存消息
//        observableIoThread = RxBus.getInstance().register(EVENT_IO_THREAD, String.class);
//        observableIoThread.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                System.out.println("postCallback当前线程名称" + Thread.currentThread().getName());
//                tvContent.setText("子线程收到的数据：" + s);
//            }
//        });
//        RxBus.getInstance().getData(EVENT_IO_THREAD);

        // 第二种:直接调用可配置线程的register方法
//        observableIoThread = RxBus.getInstance().register(EVENT_IO_THREAD, String.class, Schedulers.io(), AndroidSchedulers.mainThread(), new PostCallback<String>() {
//            @Override
//            public void call(String s) {
//                System.out.println("postCallback当前线程名称" + Thread.currentThread().getName());
//                tvContent.setText("子线程收到的数据：" + s);
//            }
//        });

        // 第三种,直接调用可配置线程的register方法 参数有别于第二种
        observableIoThread = RxBus.getInstance().register(EVENT_IO_THREAD, String.class, 2,1, new PostCallback<String>() {
            @Override
            public void call(String s) {
                System.out.println("收到的数据：" + s);
                tvContent.setText("子线程收到的数据：" + s);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册，否则内存泄漏
        RxBus.getInstance().unregister(EVENT_MAIN,observableMain);
        RxBus.getInstance().unregister(EVENT_IO_THREAD,observableIoThread);
    }
}
