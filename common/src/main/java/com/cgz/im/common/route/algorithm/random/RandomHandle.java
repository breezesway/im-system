package com.cgz.im.common.route.algorithm.random;

import com.cgz.im.common.enums.UserErrorCode;
import com.cgz.im.common.exception.ApplicationException;
import com.cgz.im.common.route.RouterHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 负载均衡：随机
 */
public class RandomHandle implements RouterHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);

        return values.get(i);
    }
}
