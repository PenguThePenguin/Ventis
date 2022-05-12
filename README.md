# Ventis
Ventis is an asynchronous, clean and optimized API made for the Redis [PubSub](https://redis.io/docs/manual/pubsub/) system

## Examples

### Setup a Ventis and a Messenger instance:
```java
        VentisConfig config = VentisConfig.builder()
                .context(new JacksonContext()) // You can use any serializer, even create your own!
                .messengerType("redis")
                .channel("bukkit")
                .redisConfig(
                        RedisConfig.builder()
                                .address("localhost")
                                .port(6379)
                                .build()
                ).build();
        
        Ventis ventis = new Ventis(config);
		
        Messenger messenger = ventis.getMessenger();
```

### Register listener + Send a Packet:
```java
        ventis.registerListener(new ExampleListener());
        messenger.sendPacket(new ExamplePacket(), "channel");
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
public class ExampleListener extends PacketListener {

    @PacketHandler(channels = {"channel1", "channel2"}) // Channels are optional.
    public void onExamplePacket(ExamplePacket packet) {
        System.out.println("Penguins " + (packet.isFlying() ? "can" : "cant") + " fly.");
    }
}
```



