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

import static com.liwy.rxbus.demo.TestService.EVENT_SERVICE;

public class RxbusActivity extends AppCompatActivity {
    public static final String EVENT_RXBUS = "rxbus";
    Observable<String> observableRxbus;
    private TextView tvContent;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rxbus);
        tvContent = (TextView)findViewById(R.id.tv_content_rxbus);
        sendBtn = (Button)findViewById(R.id.btn_rxbus);
        System.out.println("RxbusActivity当前线程名称" + Thread.currentThread().getName());
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 此时main观察者已经注册，直接发送
                RxBus.getInstance().post(MainActivity.EVENT_MAIN,"hello,main,i'm RxbusActivity");
                // 此时service观察者已注册，直接发送
                RxBus.getInstance().post(EVENT_SERVICE,"hello,service,i'm RxbusActivity");
                // 在子线程里发送数据
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("send当前线程名称" + Thread.currentThread().getName());
                        RxBus.getInstance().post(MainActivity.EVENT_IO_THREAD,"hello,i'm subthread!");
                    }
                }).start();
                // 根据注册类型对象直接发送数据
                RxBus.getInstance().post(new User("李白",25,1));
                finish();
            }
        });
        initEvent();
        initService();
    }

    /**
     * 启动service
     */
    private void initService(){
        Intent intent = new Intent(RxbusActivity.this, TestService.class);
        startService(intent);
    }


    // 注册监听事件
    void initEvent(){
//        observableRxbus = RxBus.getInstance().register(rxbusTag,String.class);
//        observableRxbus.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                tvContent.setText(s);
//            }
//        });
//        RxBus.getInstance().getData(rxbusTag);//拉去消息

        observableRxbus = RxBus.getInstance().register(EVENT_RXBUS, String.class, new PostCallback<String>(){
            @Override
            public void call(String s) {
                tvContent.setText("缓存拉取=" + s);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(EVENT_RXBUS,observableRxbus);//取消注册，否则内存泄漏
    }
}
