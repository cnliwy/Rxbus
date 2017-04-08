package com.liwy.rxbus;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * Created by liwy on 2017/3/13.
 */

public class RxBus {
    private HashMap<Object,List<Subject>> maps = new HashMap<Object,List<Subject>>();
    private HashMap<Object,Object> data = new HashMap<Object,Object>();
    private static RxBus instance;

    // 获取单例对象
    public static RxBus getInstance(){
        if (instance == null){
            synchronized (RxBus.class){
                if (instance == null){
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    // 注册事件
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz){
        List<Subject> subjects =  maps.get(tag);
        if (subjects == null){
            subjects = new ArrayList<Subject>();
            maps.put(tag,subjects);
        }
        PublishSubject<T> subject = PublishSubject.<T>create();
        subjects.add(subject);
        return subject;
    }
    //取消注册
    public void unregister(@NonNull Object tag, @NonNull Observable observable){
        List<Subject> subjects = maps.get(tag);
        if (subjects != null){
            subjects.remove((Subject)observable);
            if (subjects.isEmpty()){
                maps.remove(tag);
            }
        }
    }
    //延迟数据拉取
    public void getData(@NonNull Object tag){
        Object obj = data.get(tag);
        if (obj != null){
            post(tag,obj);
            data.remove(tag);//发送过后清理缓存
        }
    }

    // 发送消息至已注册的事件,tag就是该数据的对象名称
    public void post(@NonNull Object o){
        post(o.getClass().getSimpleName(), o);
    }
    // 发送消息至已注册的事件,tag就是该数据的对象名称，如果为注册则缓存
    public void postWithCache(@NonNull Object o){
        postWithCache(o.getClass().getSimpleName(), o);
    }

    // 给同属同一tag的事件群发消息
    public void post(@NonNull Object tag, @NonNull Object o){
        List<Subject> subjects = maps.get(tag);
        if (subjects != null && !subjects.isEmpty()){
            for (Subject subject : subjects){
                subject.onNext(o);
                // 发送消息后检查该tag下的缓存
                if (data.get(tag) != null){
                    data.remove(tag);
                }
            }
        }
    }

    // 给同属同一tag的事件群发消息(如果该tag未注册，则缓存)
    public void postWithCache(@NonNull Object tag, @NonNull Object o){
        List<Subject> subjects = maps.get(tag);
        if (subjects != null && !subjects.isEmpty()){
            for (Subject subject : subjects){
                subject.onNext(o);
                // 发送消息后检查该tag下的缓存
                if (data.get(tag) != null){
                    data.remove(tag);
                }
            }
        }else{
            // 如果尚不存在此观察者则将消息存入data缓存，等待观察者拉取
            data.put(tag, o);
        }
    }

    //给所有的发送注册事件发送消息(前提要保证所有观察者所注册的接收对象是一致的)
    public void postAll(@NonNull Object o){
        if (maps != null){
            // 遍历整个maps
            for (Object key : maps.entrySet()){
                post(key,o);
            }
        }
    }
}
