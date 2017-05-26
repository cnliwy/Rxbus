使用简介
===
**总共分三步：**
1. 注册(在需要接收数据的地方)
2. 发送(在需要发送数据的地方)
3. 取消注册(不用了记得取消注册，比如onDestrory里)

**第一步：注册**
```
注册分为两类方法：register()和registerSingle()两种方法，区别就是registerSingle是单例的，可保证全局唯一tag只有一个事件源。而register方法同一个tag可以注册多个观察者，在多处监听事件</br>
注册方法总共两套八个：
public <T> Observable<T> register(@NonNull Object  obj);
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz);
public <T> Observable<T> register(@NonNull Object  obj, @NonNull final PostCallback<T> callback);
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);

public <T> Observable<T> registerSingle(@NonNull Object  obj);
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz);
public <T> Observable<T> registerSingle(@NonNull Object  obj, @NonNull final PostCallback<T> callback);
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);

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
   
 3）如果需要自定义事件源线程和观察者线程的请调用下面这两个注册方法：
public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
public <T> Observable<T> registerSingle(@NonNull Object tag, @NonNull Class<T> clazz, int subscribeOn, int observeOn, final PostCallback<T> callback);
 用法如下：
 register("tag",String.class,2,1,callback);
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
