package com.liwy.rxbus.demo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.liwy.rxbus.PostCallback;
import com.liwy.rxbus.RxBus;

import rx.Observable;


public class TestService extends Service {
    public static final String EVENT_SERVICE = "service";
    public static final String EVENT_SERVICE_USER = "service_user";


    private Observable<String> eventService;
    private Observable<User> eventUser;
    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 注册单例事件，全局此tag只有一个事件源
        eventService = RxBus.getInstance().registerSingle(EVENT_SERVICE, String.class, new PostCallback<String>() {
            @Override
            public void call(String s) {
                System.out.println("服务收到：" + s);
            }
        });
        eventUser = RxBus.getInstance().register(new User(), new PostCallback<User>() {
            @Override
            public void call(User u) {
                System.out.println("用户:" + u.toString());
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unregister(User.class.getName(),eventUser);
        RxBus.getInstance().unregister(EVENT_SERVICE,eventService);
    }
}
