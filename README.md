使用简介
===
总共分三步：</br>
1 注册(在需要接收数据的地方)</br>
2 发送(在需要发送数据的地方)</br>
3 取消注册(不用了记得取消注册)</br>

第一步：注册
---
注册分为两类方法：register()和registerSingle()两种方法，区别就是registerSingle是单例的，可保证全局唯一tag只有一个事件源。
注册时需要获取缓存消息的请传入PostCallback回调（比如，你正在A页面，想传值给B页面，在B页面尚未创建的情况下给它发送了消息，这就需要B在注册事件源的时候传入回调以获取A发送的消息并处理）
当然了PostCallback可不仅仅是获取历史消息，PostCallback主要是用于事件的回调处理！你总不能接收了数据后啥也不干吧！
使用示例：
 eventService = RxBus.getInstance().register("tag1", String.class, new PostCallback<String>() {
            @Override
            public void call(String s) {
                System.out.println("data：" + s);
            }
        });
第二步：发送
---
意思很明确，就是要发送数据，api如下：
post(Object o);
post(Object tag, Object o);
postDelayed(Object o)
postDelayed(Object tag, Object o);
post系列和postDelayed系列的区别就是：postDelayed支持延迟发送，及当事件源不存在的情况下会存入缓存，等下该事件源注册后拉取。
使用示例：
RxBus.getInstance().postDelayed("tag1","hello,service,i'm a");
RxBus.getInstance().post("tag2","hello,service,i'm b");
第三步：取消注册:
根据注册时候的tag取消该事件源。
使用示例：
RxBus.getInstance().unregister("tag1",eventService);

ok，就这么简单。
上述只是Rxbus的一个简单实现，各位有什么好的意见也可以邮箱联系我哦cnliwy@163.com~

