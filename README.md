# inspector

不重启JVM的情况下动态修改正在运行的类

## 原理

java instrument

## 如何使用

### 下载[inspector-distribute-1.0-SNAPSHOT-bin.zip](https://github.com/argszero/inspector/wiki/distribution/inspector-distribute-1.0-SNAPSHOT-bin.zip)

### 解压

### 查询你的JVM的进程Id

    jps
or
    ps -ef|grep java

### 动态替换要替换的类

    ./inspector.sh /home/xxx.class yyy

注意；这里的类可以不在相应的包里，比如org.hello.Hello类不需要再org/hello目录下。
另外，你也同时替换多个类，比如

    ./inspector.sh /home/xxx.class /home/xxx2.class yyy

也可以同时指定目录（该目录及子目录下的类都会自动替换)

    ./inspector.sh /home/xxx.class /home/classes yyy

默认替换回持续60秒，之后会还原为替换之前的类。你可以通过-Dtime指定要替换的时间,比如要持续10秒的话可以运行如下命令：

     ./inspector.sh /home/xxx.class /home/classes -Dtime=10 yyy

如果时间指定的为负数，则替换永远有效（不会还原替换之前的类）