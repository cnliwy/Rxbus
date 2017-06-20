package com.liwy.rxbus;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


/**
 * Created by liwy on 2017/3/13.
 */

public class RxBus {
    public static final int THREAD_MAIN = 1;//主线程
    public static final int THREAD_IO = 2;// 子线程
    // 一个tag可拥有多个Subject对象
    private HashMap<Object,List<Subject>> maps = new HashMap<Object,List<Subject>>();
    // 一个tag只能拥有一个Subject对象
    private HashMap<Object,Subject> singleMaps = new HashMap<Object,Subject>();
    // 待发送的消息缓存
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

    /**
     * 注册事件，可配置事件源和观察者的线程，并设置回调
     * @param tag           事件tag
     * @param clazz         数据类型
     * @param subscribeOn  事件源线程
     * @param observeOn    观察者线程
     * @param callback      事件回调
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback){
        Observable<T> subject = register(tag,clazz);
        subject = makeThreadCallback(subject,subscribeOn,observeOn,callback);
        getData(tag);
        return subject;
    }
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback){
        Observable<T> subject = register(tag,clazz);
        subject.subscribeOn(subscribeOn).observeOn(observeOn).subscribe(new Consumer<T>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                callback.call(t);
            }
        });
        getData(tag);
        return subject;
    }


    /**
     * 根据传入数据对象注册事件
     * @param obj
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object  obj){
        String tag = obj.getClass().getName();
        Class clazz = obj.getClass();
        return register(tag,clazz);
    }

    /**
     * 根据传入数据对象注册事件并设置回调
     * @param obj
     * @param callback
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object  obj, @NonNull final PostCallback<T> callback){
        Observable<T> subject = register(obj);
        makeCallback(subject,callback);
        getData(obj.getClass().getName());
        return subject;
    }





    /**
     * 注册回调，并主动拉取历史缓存
     * @param tag
     * @param clazz
     * @param callback
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, @NonNull final PostCallback<T> callback){
        PublishSubject<T> subject = (PublishSubject<T>) register(tag,clazz);
        makeCallback(subject,callback);
        getData(tag);
        return subject;
    }
    /**
     * 一个tag只有一个subject对象
     * @param tag
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz){
        PublishSubject<T> subject = (PublishSubject<T>) singleMaps.get(tag);
        if (subject == null)subject = PublishSubject.<T>create();
        singleMaps.put(tag,subject);
        return subject;
    }

    public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, @NonNull final PostCallback callback){
        PublishSubject<T> subject = (PublishSubject<T>) registerSingle(tag,clazz);
        makeCallback(subject,callback);
        getData(tag);
        return subject;
    }

    public <T> Observable<T> registerSingle(@NonNull Object obj){
        String tag = obj.getClass().getName();
        Class clazz = obj.getClass();
        return registerSingle(tag,clazz);
    }

    public <T> Observable<T> registerSingle(@NonNull Object obj, @NonNull final PostCallback callback){
        PublishSubject<T> subject = (PublishSubject<T>)registerSingle(obj);
        makeCallback(subject,callback);
        getData(obj.getClass().getName());
        return subject;
    }
    /**
     * 注册事件，可配置事件源和观察者的线程，并设置回调
     * @param tag           事件tag
     * @param clazz         数据类型
     * @param subscribeOn  事件源线程 1主线程 2子线程
     * @param observeOn    观察者线程 1主线程 2子线程
     * @param callback      事件回调
     * @param <T>
     * @return
     */
    public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback){
        Observable<T> subject = registerSingle(tag,clazz);
        subject = makeThreadCallback(subject,subscribeOn,observeOn,callback);
        getData(tag);
        return subject;
    }
    public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback){
        Observable<T> subject = registerSingle(tag,clazz);
        subject.subscribeOn(subscribeOn).observeOn(observeOn).subscribe(new Consumer<T>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                callback.call(t);
            }
        });
        getData(tag);
        return subject;
    }

    /**
     * 根据传入参数设置Observable的事件生产消费线程及回调事件
     * @param subject
     * @param subscribeOn 1主线程 2子线程
     * @param observeOn    1主线程 2子线程
     * @param callback
     * @param <T>
     * @return
     */
    public <T> Observable<T> makeThreadCallback(Observable<T> subject, int subscribeOn, int observeOn, final PostCallback<T> callback){
        Scheduler main = AndroidSchedulers.mainThread();
        Scheduler io = Schedulers.io();
        if (subscribeOn == THREAD_MAIN && observeOn == THREAD_MAIN){
            subject.subscribeOn(main).observeOn(main).subscribe(new Consumer<T>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                    callback.call(t);
                }
            });
        }else if (subscribeOn == THREAD_MAIN && observeOn == THREAD_IO){
            subject.subscribeOn(main).observeOn(io).subscribe(new Consumer<T>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                    callback.call(t);
                }
            });
        }else if (subscribeOn == THREAD_IO && observeOn == THREAD_MAIN){
            subject.subscribeOn(io).observeOn(main).subscribe(new Consumer<T>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                    callback.call(t);
                }
            });
        }else if (subscribeOn == THREAD_IO && observeOn == THREAD_IO){
            subject.subscribeOn(io).observeOn(io).subscribe(new Consumer<T>() {
                @Override
                public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                    callback.call(t);
                }
            });
        }
        return  subject;
    }

    /**
     * 给事件源设置事件回调
     * @param subject
     * @param callback
     * @param <T>
     */
    private <T> Observable<T> makeCallback(Observable<T> subject, final PostCallback<T> callback){
        subject.subscribe(new Consumer<T>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull T t) throws Exception {
                callback.call(t);
            }
        });
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
        data.remove(tag);
        singleMaps.remove(tag);
    }

    //拉取data缓存里的延迟数据
    public void getData(@NonNull Object tag){
        Object obj = data.get(tag);
        if (obj != null){
            post(tag,obj);
            data.remove(tag);//发送过后清理缓存
        }
    }

    // 发送消息至已注册的事件,tag就是该数据的对象名称
    public void post(@NonNull Object o){
        post(o.getClass().getName(), o);
    }


    // 给同属同一tag的事件群发消息
    public void post(@NonNull Object tag, @NonNull Object o){
        // 发送多个
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
        // 发送单例
        postSingle(tag,o);
    }

    // 发送消息至已注册的事件,tag就是该数据的对象名称，如果为注册则缓存
    public void postDelayed(@NonNull Object o){
        postDelayed(o.getClass().getName(), o);
    }

    // 给同属同一tag的事件群发消息(如果该tag未注册，则缓存)
    public void postDelayed(@NonNull Object tag, @NonNull Object o){
        List<Subject> subjects = maps.get(tag);
        Subject singleSubject = singleMaps.get(tag);
        boolean hasValue = false;
        if (subjects != null && !subjects.isEmpty()){
            hasValue = true;
            for (Subject subject : subjects){
                subject.onNext(o);
                // 发送消息后检查该tag下的缓存并移除
                if (data.get(tag) != null){
                    data.remove(tag);
                }
            }
        }
        if (singleSubject != null){
            hasValue = true;
            singleSubject.onNext(o);
        }

        if (!hasValue){
            // 如果尚不存在此观察者则将消息存入data缓存，等待观察者拉取
            data.put(tag, o);
        }
    }

    private void postSingle(@NonNull Object tag, @NonNull Object o){
        Subject subject = singleMaps.get(tag);
        if (subject != null)subject.onNext(o);
    }
}
