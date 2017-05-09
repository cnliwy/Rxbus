使用简介
===
总共分三步：</br>
1 注册</br>
2 发送</br>
3 取消注册</br>

&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>这里要注意，为了区分缓存和非缓存两种使用场景，发送消息提供了两类方法分别是：</font>

<font color=#000000 face="微软雅黑" size=4>1 直接发送</font>
public void post(@NonNull Object o)；
public void post(@NonNull Object tag, @NonNull Object o)；
<font color=#000000 face="微软雅黑" size=4>2 延迟发送</font>
public void postDelayed(@NonNull Object o)；
public void postDelayed(@NonNull Object tag, @NonNull Object o)；

&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>使用postDelayed的话则需要在注册观察者之后调用Rxbus.getInstance().getData(tag)去主动拉取数据。如果直接post()发送的话则不需要调用getData()方法。</font>
&ensp;&ensp;&ensp;&ensp;<font color=#000000 face="微软雅黑" size=4>上述只是Rxbus的一个简单实现，各位有什么好的意见也可以邮箱联系我哦cnliwy@163.com~</font>

