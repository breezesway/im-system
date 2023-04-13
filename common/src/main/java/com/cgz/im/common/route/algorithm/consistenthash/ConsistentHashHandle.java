package com.cgz.im.common.route.algorithm.consistenthash;

import com.cgz.im.common.route.RouterHandle;

import java.util.List;

/**
 * 负载均衡：一致性哈希
 */
public class ConsistentHashHandle implements RouterHandle {

    private AbstractConsistentHash consistentHash;

    public void setConsistentHash(AbstractConsistentHash consistentHash) {
        this.consistentHash = consistentHash;
    }

    @Override
    public String routeServer(List<String> values, String key) {

        return consistentHash.process(values,key);
    }
}
