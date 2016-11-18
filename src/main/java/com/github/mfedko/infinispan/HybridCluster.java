package com.github.mfedko.infinispan;

import java.util.Properties;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.util.FileLookup;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolStackConfigurator;

public class HybridCluster {

    static final String JGROUPS_CONFIGURATION_FILE = "hybrid-udp.xml";

    private DefaultCacheManager cm;

    public static class MuxChannelLookup implements JGroupsChannelLookup {

        @Override
        public Channel getJGroupsChannel(Properties p) {
            FileLookup fileLookup = FileLookupFactory.newInstance();
            try {
                String configFile = p.getProperty(JGroupsTransport.CONFIGURATION_FILE);
                ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(
                        fileLookup.lookupFileLocation(configFile, HybridCluster.class.getClassLoader()));
                return new JChannel(configurator);
            } catch (Exception e) {
                throw new CacheException("Unable to start JGroups channel", e);
            }
        }

        @Override
        public boolean shouldConnect() {
            return true;
        }

        @Override
        public boolean shouldDisconnect() {
            return true;
        }

        @Override
        public boolean shouldClose() {
            return true;
        }

    }

    public HybridCluster(String configurationFile) {
        this(configurationFile, "clustered");
    }

    public HybridCluster(String configurationFile, String clusterName) {
        GlobalConfigurationBuilder global = new GlobalConfigurationBuilder();
        global.clusteredDefault().transport()
                .clusterName(clusterName)
                .nodeName("embedded")
                .addProperty(JGroupsTransport.CONFIGURATION_FILE, configurationFile)
                .addProperty(JGroupsTransport.CHANNEL_LOOKUP, MuxChannelLookup.class.getName())
        ;
        global.serialization().classResolver(HybridClassResolver.getInstance(HybridCluster.class.getClassLoader()));
        ConfigurationBuilder config = new ConfigurationBuilder();
        config.clustering().cacheMode(CacheMode.DIST_SYNC);
        cm = new DefaultCacheManager(global.build(), config.build());

        System.out.println("Cache names: " + cm.getCacheNames());
    }

    public EmbeddedCacheManager getCacheManager() {
        return cm;
    }

    public <K, V> Cache<K, V> getCache() {
        return cm.getCache("default");
    }

    public <K, V> Cache<K, V> getCache(String cacheName) {
        return cm.getCache(cacheName);
    }

}
