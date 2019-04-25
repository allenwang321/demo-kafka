# demo-kafka
springboot kafka的一个学习项目


#### 1. 下载与解压

直接去官网的下载 [页面](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.2.0/kafka_2.11-2.2.0.tgz)

![](https://ws1.sinaimg.cn/large/006tNc79gy1g2dr1cdkxsj31kt0u0h2b.jpg)

复制下载链接后直接使用wget下载

```shell
wget http://mirrors.tuna.tsinghua.edu.cn/apache/kafka/2.2.0/kafka_2.11-2.2.0.tgz
```

下载完成后解压

```shell
tar -xf kafka_2.11-2.2.0.tgz
```

下图是解压后的目录内容   

![](https://ws2.sinaimg.cn/large/006tNc79gy1g2du4vayogj31g807u0uy.jpg)

#### 2. kafka配置与启动

bin文件夹里面存放的是与kafka相关的脚本文件，删除了Windows文件夹

![](https://ws2.sinaimg.cn/large/006tNc79gy1g2duwcwz2xj31fw0oy497.jpg)

用到的脚本以及作用

|名称|作用|
|:---:|:---:|
|zookeeper-server-start.sh|用于启动zookeeper|
|kafka-server-start.sh|启动kafka|
|kafka-topics.sh|Topic有关的操作|
|kafka-console-producer.sh|创建生产者|
|kafka-console-consumer.sh|创建消费者|
|kafka-run-class.sh|检验日志|
|kafka-replay-log-producer.sh|kafka集群中读取指定Topic的消息|

使用kafka时首先要启动zookeeper

```shell
./bin/zookeeper-server-start.sh config/zookeeper.properties 
```

建议使用以下命令，可以使得直接在后台执行

```shell
nohup ./bin/zookeeper-server-start.sh config/zookeeper.properties &
```

zookeeper可以通过jps命令去查看是否启动,如下图发现有QuorumPeerMain则表明已启动

![](https://ws1.sinaimg.cn/large/006tNc79gy1g2eufo8qqwj31g8036mxo.jpg)

zookeeper默认的端口是2181也可以直接通过命令查看 `lsof -i:2181`

![](https://ws3.sinaimg.cn/large/006tNc79gy1g2eum260foj31gc038dgr.jpg)

zookeeper启动之后就可以去启动kafka了，启动时可以修改配置文件 `config/server.properties`  
如果是想要可以通过远程连接kafka需要放开 `advertised.listeners=PLAINTEXT://:9092` 的注释。  
中间填上自己自己服务器的地址即可。  
启动后通过 `jps` 命令可以看到有 `kakfa` 如下图所示  

![](https://ws2.sinaimg.cn/large/006tNc79gy1g2f88472wuj31gk04gt9l.jpg)

然后可以在终端中创建一个Topic

```shell
./bin/kafka-topics.sh --create --zookeeper localhost:2181  \
--replication-factor 1 --partitions 1 --topic testTopic
```

如下图

![](https://ws2.sinaimg.cn/large/006tNc79gy1g2f8f1y2hej31g4034752.jpg)

上述命令详细解释如下 

1. kafka-topics.sh：Kafka 提供的一个 shell 脚本文件(位于 bin 目录中)，用于创建或查看 topic 信息。 
2. --create：shell 脚本的参数，告诉 shell 脚本：要创建一个 topic。 
3. --zookeeper localhost:2181：shell 脚本的参数，告诉 shell 脚本 Zookeeper 的地址，用于保存 topic 元数据信息。 
4. --partitions 1：shell 脚本参数，告诉 shell 脚本：所创建的这个 topic 的 partition 个数为1 
5. --replication-factor 1：shell脚本参数，告诉 shell 脚本：每个 partition 的副本数为1 
6. --topic testTopic：shell 脚本参数，告诉 shell 脚本：创建的 topic 的名称为 testTopic。

可以查看topic的list来查看刚刚创建的topic

```shell
./bin/kafka-topics.sh -zookeeper localhost:2181 --list
```

如下图

![](https://ws2.sinaimg.cn/large/006tNc79gy1g2f8rxiah9j31go03cgme.jpg)

接下来测试生产者和消费者  
首先启动生产者  

```shell
./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic testTopic
```
然后新建一个终端用来启动消费者

```shell
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic testTopic
```

测试生产者输入内容

![](https://ws1.sinaimg.cn/large/006tNc79gy1g2f98wh2pzj31gc03agmb.jpg)

消费者接收内容

![](https://ws4.sinaimg.cn/large/006tNc79gy1g2f9a177rhj31ge050q3o.jpg)

可以看到消息正确的发送和接收到了  

#### 3. springboot中使用kafka

引入spring-kafka版本也还和kafka自身的版本有关，可以在如下[网页查看](https://spring.io/projects/spring-kafka) 根据个人版本挑选合适的版本。

![](https://ws4.sinaimg.cn/large/006tNc79gy1g2f9hwmahxj316c0fw76y.jpg)

这个是我自己选择的版本

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>2.2.5.RELEASE</version>
</dependency>
```

引入依赖之后还需要在配置文件中进行配置

```yml
spring:
  kafka:
    # 服务地址
    bootstrap-servers: ip:9092
    consumer:
      # 自动提交
      enable-auto-commit: true
      # 序列化
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

java 端消费者代码如下

```java
@Component
public class KafkaConsumer {
    @KafkaListener(topics = {"testTopic"}, groupId = "test")
    public void receive(String message){
        System.out.println(message);
    }
}
```
测试结果如下

![](https://ws3.sinaimg.cn/large/006tNc79gy1g2f9soarn3j31ii0ei7au.jpg)

java 端生产者代码如下

```java
@Component
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String data){
        kafkaTemplate.send("testTopic", data);
    }
}
```

测试结果如下

![](https://ws4.sinaimg.cn/large/006tNc79gy1g2fa13nfvfj31gc0tcgr9.jpg)
