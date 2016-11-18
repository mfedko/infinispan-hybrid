# infinispan-hybrid

This is minimal clone of [infinispan-playground-hybrid](https://github.com/tristantarrant/infinispan-playground-hybrid) for Infinispan 8.2.

Start the server using the following:
```bash
    ./bin/standalone.sh -c clustered.xml -Djboss.node.name=nodeA
```

Run the HybridCacheTest (it uses the following java options: -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1):
```bash
    ./gradlew run
```
