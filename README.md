<center>android 具有缓存机制的Rxbus</center>
===
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>app内部的消息传递以前用的最多的应该是Eventbus了，然而老夫并没有用过。。。想起了以前那些惨无人道灭绝人寰的屎一样的代码，深感罪孽深重，举个栗子:静态变量、各种接口回调、handler等....代码难看耦合度高，不利于维护，不利于阅读，跟扩展更是几乎绝缘了。所以以后要痛改前非，洗心革面，重新coding。别问我为什么不用Eventbus，最近迷上了rxjava.....好，下面我们进入正题。</font>
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>Rxbus当然是基于rxjava,实现的原理也是基于观察者模式。我们先简单看下两种用法：直接发送和缓存延迟发送。这里新建两个Activity:MainActivity和RxbusActivity,分别用于缓存发送和直接发送。</font>
####&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>首先在MainActivity里调用缓存发送并且在RxbusActivity里接受，代码如下：</font>

```
MainActivity
 // 此时rxbus观察者尚未注册，采用基于缓存的post发送
                RxBus.getInstance().postWithCache(rxbusTag,"hello,rxbus,i'm MainActivity");
                Intent intent = new Intent(MainActivity.this,RxbusActivity.class);
                startActivity(intent);

RxbusActivity
// 注册观察者
Observable<String> observableRxbus = RxBus.getInstance().register(rxbusTag,String.class);
        observableRxbus.observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                tvContent.setText(s);
            }
        });
        RxBus.getInstance().getData(rxbusTag);//拉取缓存消息
```
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>在MainActivity里点击按钮发送消息，调用了postWithCache并跳转至RxbusActivity页面里，发送消息的时候observableRxbus尚未注册，这里采取了消息缓存的机制：</font>

```
private HashMap<Object,Object> data = new HashMap<Object,Object>();
 // 给同属同一tag的事件群发消息(如果该tag未注册，则缓存)
    public void postWithCache(@NonNull Object tag, @NonNull Object o){
        List<Subject> subjects = maps.get(tag);
        if (subjects != null && !subjects.isEmpty()){
            for (Subject subject : subjects){
                subject.onNext(o);
                // 发送消息后检查该tag下的缓存并移除
                if (data.get(tag) != null){
                    data.remove(tag);
                }
            }
        }else{
            // 如果尚不存在此观察者则将消息存入data缓存，等待观察者拉取
            data.put(tag, o);
        }
    }
```
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>这里要注意，为了区分缓存和非缓存两种使用场景，发送消息提供了两类方法分别是：</font>

<font color=#000000 face="微软雅黑" size=4>1 直接发送</font>
public void post(@NonNull Object o)；
public void post(@NonNull Object tag, @NonNull Object o)；
<font color=#000000 face="微软雅黑" size=4>2 延迟发送</font>
public void postWithCache(@NonNull Object o)；
public void postWithCache(@NonNull Object tag, @NonNull Object o)；

&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>使用postWithCache的话则需要在注册观察者之后调用Rxbus.getInstance().getData(tag)去主动拉取数据。如果直接post()发送的话则不需要调用getData()方法。</font>
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>上述只是Rxbus的一个简单实现，下面附上Rxbus完整源码及demo下载地址，各位有什么好的意见也可以邮箱联系我cnliwy@163.com哦~</font>

```
public class RxBus {
	// 已注册的观察者
    private HashMap<Object,List<Subject>> maps = new HashMap<Object,List<Subject>>();
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
                // 发送消息后检查该tag下的缓存并移除
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
```
</font>
[传送门](https://github.com/cnliwy/Rxbus)
