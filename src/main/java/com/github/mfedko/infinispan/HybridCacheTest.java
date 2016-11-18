/**
 * HybridCacheTest.java
 *
 * Copyright 2015-2016 RhythmOne. All Rights Reserved.
 *
 * This software is the proprietary information of RhythmOne.
 * Use is subject to license terms.
 */
package com.github.mfedko.infinispan;

import java.io.IOException;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Transport;

import com.google.common.base.Preconditions;

public class HybridCacheTest {

    public static void main(String[] args) throws Exception {

        HybridCluster cluster = new HybridCluster(HybridCluster.JGROUPS_CONFIGURATION_FILE, "cluster");
        EmbeddedCacheManager cacheManager = cluster.getCacheManager();

        try {

            Cache<String, String> cache;

            {
                Cache<String, String> defaultCache = getCache(cluster, "default");

                Preconditions.checkNotNull(defaultCache, "No default cache found?");

                cache = defaultCache;
            }

            System.out.println("Cache name: " + cache.getName());
            System.out.println("Keys: " + cache.keySet());
            System.out.println("TS: " + cache.get("TS"));

            cache.put("TS", String.valueOf(System.currentTimeMillis()));
            System.out.println("TS after update: " + cache.get("TS"));

        } finally {

            // Stop the cache manager and release all resources
            cacheManager.stop();
        }
    }

    private static Cache<String, String> getCache(HybridCluster cluster, String cacheName) throws IOException {

        Cache<String, String> cache = cluster.getCache(cacheName);

        Transport transport = cache.getAdvancedCache().getRpcManager().getTransport();
        System.out.printf("Node %s joined as master. View is %s.%n", transport.getAddress(), transport.getMembers());

        return cache;
    }
}
