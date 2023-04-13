package com.cgz.im.common.route.algorithm.loop;

import com.cgz.im.common.enums.UserErrorCode;
import com.cgz.im.common.exception.ApplicationException;
import com.cgz.im.common.route.RouterHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 负载均衡：轮询
 */
public class LoopHandle implements RouterHandle {

    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        Long i = index.incrementAndGet() % size;
        if(i<0){
            i = 0L;
        }
        return values.get(i.intValue());
    }
}
