android 具有缓存机制的Rxbus
===

app内部的消息传递以前用的最多的应该是Eventbus了，然而老夫并没有用过。。。想起了以前那些惨无人道灭绝人寰的屎一样的代码，深感罪孽深重，举个栗子:静态变量、各种接口回调、handler等....代码难看耦合度高，不利于维护，不利于阅读，跟扩展更是几乎绝缘了。
所以以后要痛改前非，洗心革面，重新coding。别问我为什么不用Eventbus，最近迷上了rxjava.....好，下面我们进入正题。

Rxbus当然是基于rxjava,实现的原理也是基于观察者模式。本次RxBus主要实现以下功能：直接发送和缓存延迟发送，单个观察者注册（一个事件对应一处监听）和分组观察者注册（同一事件多处监听）


使用简介
===
**总共分三步：**
1. 注册(在需要接收数据的地方)
2. 发送(在需要发送数据的地方)
3. 取消注册(不用了记得取消注册，比如onDestrory里)

**第一步：注册**
```
注册分为两类方法：register()和registerSingle()两种方法，区别就是registerSingle是单例的，可保证全局唯一tag只有一个事件源。而register方法同一个tag可以注册多个观察者，在多处监听事件</br>
注册方法总共两套10个：
public <T> Observable<T> register(@NonNull Object  obj);
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz);
public <T> Observable<T> register(@NonNull Object  obj, @NonNull final PostCallback<T> callback);
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
 public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback)

public <T> Observable<T> registerSingle(@NonNull Object  obj);
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz);
public <T> Observable<T> registerSingle(@NonNull Object  obj, @NonNull final PostCallback<T> callback);
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
 public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback)
1）不需要切换线程也不需要拉取离线消息的可调用下面两个方法注册
public <T> Observable<T> register(@NonNull Object  obj);
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz);
registerSingle同理，也是对应的两个方法。

2）需要获取离线消息但不需要切换线程的
public <T> Observable<T> register(@NonNull Object  obj, @NonNull final PostCallback<T> callback);

注册时需要获取缓存消息的请调用有PostCallback回调的方法（比如，你正在A页面，想传值给B页面，在B页面尚未创建的情况下给它发送了消息，这就需要B在注册事件源的时候传入回调以获取A发送的消息并处理）</br>
当然了PostCallback可不仅仅是获取历史消息，PostCallback主要是用于事件的回调处理！你总不能接收了数据后啥也不干吧！
使用示例：
 eventService = RxBus.getInstance().register("tag1", String.class, new PostCallback<String>() {</br>
            @Override </br>
            public void call(String s) {
                System.out.println("data：" + s); 
            }
  });
  其实设置PostCallback就相当于设置改观察者的Action1事件
  eventService.subscribe(new Action1<String>() {
                          @Override
                          public void call(String s) {
                             ...
                          }
   });
   
 3）如果需要自定义事件源线程和观察者线程的请调用下面这四个注册方法：
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
 public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback)
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
 public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback)
 
 用法如下：
 register("tag",String.class,2,1,callback);
 或者 register("tag",String.class,Schedulers.io(), AndroidSchedulers.mainThread(),callback);
 subscribeOn,observeOn这两个参数是int类型，subscribeOn表示事件源的线程 observeOn表示观察者的线程，1 表示主线程 2表示子线程
```

**第二步：发送**

```
下面是发送消息的api，如下：
post(Object o);
post(Object tag, Object o);
postDelayed(Object o);
postDelayed(Object tag, Object o);

post系列和postDelayed系列的区别就是：
    post方法调用的时候如果该事件尚未注册，则本次发送的数据就没了。
    postDelayed支持延迟发送即离线消息，及当事件源不存在的情况下会存入缓存，等待该事件源注册后拉取。
使用示例：
RxBus.getInstance().postDelayed("tag1","hello,service,i'm a");
RxBus.getInstance().post("tag2","hello,service,i'm b");
```


**第三步：取消注册:**

```
不用的时候记得取消哦，省得造成内存泄漏。
根据注册时候的tag取消该事件源。
使用示例：</br>
RxBus.getInstance().unregister("tag1",eventService);</br>

ok，就这么简单。</br>
上述只是Rxbus的一个简单实现，各位有什么好的意见也可以邮箱联系我哦cnliwy@163.com~</br>
```
**第四步：RxBus源码:**
```
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
     * @param subscribeOn  事件源线程 1主线程 2子线程
     * @param observeOn    观察者线程 1主线程 2子线程
     * @param callback      事件回调
     * @param <T>
     * @return
     */
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback){
        Observable<T> subject = register(tag,clazz);
        subject = makeThread(subject,subscribeOn,observeOn);
        subject.subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                callback.call(t);
            }
        });
        getData(tag);
        return subject;
    }
    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback){
        Observable<T> subject = register(tag,clazz);
        subject.subscribeOn(subscribeOn).observeOn(observeOn).subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                callback.call(t);
            }
        });
        subject = makeCallback(subject,callback);
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
        subject = makeThread(subject,subscribeOn,observeOn);
        subject.subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                callback.call(t);
            }
        });
        getData(tag);
        return subject;
    }
    public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, Scheduler subscribeOn, Scheduler observeOn, final PostCallback<T> callback){
        Observable<T> subject = registerSingle(tag,clazz);
        subject.subscribeOn(subscribeOn).observeOn(observeOn).subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                callback.call(t);
            }
        });
        getData(tag);
        return subject;
    }

    /**
     * 给事件源设置事件回调
     * @param subject
     * @param callback
     * @param <T>
     */
    private <T> Observable<T> makeCallback(Observable<T> subject, final PostCallback<T> callback){
        subject.subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                callback.call(t);
            }
        });
        return subject;
    }

    /**
     * 设置Observable的线程
     * @param subject
     * @param subscribeOn   事件源线程
     * @param observeOn     观察者线程
     * @param <T>
     * @return
     */
    private <T> Observable<T> makeThread(Observable<T> subject,int subscribeOn,int observeOn){
        if (subscribeOn == THREAD_MAIN){
            subject.subscribeOn(AndroidSchedulers.mainThread());
        }else if (subscribeOn == THREAD_IO){
            subject.subscribeOn(Schedulers.io());
        }
        if (observeOn == THREAD_MAIN) {
            subject.observeOn(AndroidSchedulers.mainThread());
        }else if (observeOn == THREAD_IO) {
            subject.observeOn(Schedulers.io());
        }
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


```
[源码传送门](https://github.com/cnliwy/Rxbus)
