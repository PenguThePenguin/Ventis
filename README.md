# Ventis
Ventis is an asynchronous, clean and optimized api made for sending packets across servers.

## Support
Ventis currently supports multiple connection types:

- [Sql](https://www.mysql.com/)
- [Redis](https://redis.io/)
- [RabbitMQ](https://www.rabbitmq.com/)
- [Java's Socket](https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)

## Examples

### Setup Ventis and register a Connection:
```java
        VentisConfig config = VentisConfig.builder()
                .codec(new JacksonCodec()) // You can use any codec, even create your own!
                .channel("bukkit")
                .build();

        Ventis ventis = new Ventis(config);

        RedisConfig redisConfig = RedisConfig.builder()
                .address("localhost")
                .port(6379)
                .build();

        RedisConnection connection = new RedisConnection(ventis, redisConfig);
        
        // Get a connection from its class:
        RedisConnection redisConnection = ventis.getConnection("redis", RedisConnection.class); 
```

### Register listener + Send a Packet:
```java
        ventis.registerListener(new ExampleListener());
        connection.sendPacket(new ExamplePacket(), "channel");
```

### Create a packet
```java
@Getter
public class ExamplePacket extends Packet {

    private boolean flying = false;
}
```

### Create a listener
```java
public class ExampleListener implements PacketListener {

    @PacketHandler(channels = {"channel1", "channel2"}) // Channels are optional.
    public void onExamplePacket(ExamplePacket packet) {
        System.out.println("Penguins " + (packet.isFlying() ? "can" : "cant") + " fly.");
    }
}
```



