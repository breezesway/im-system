package com.cgz.im.service.config;

import com.cgz.im.common.config.AppConfig;
import com.cgz.im.common.enums.ImUrlRouteWayEnum;
import com.cgz.im.common.enums.RouteHashMethodEnum;
import com.cgz.im.common.route.RouterHandle;
import com.cgz.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    /**
     * 负载均衡
     */
    @Bean
    public RouterHandle routerHandle() throws Exception{
        Integer imRouteWay = appConfig.getImRouteWay();
        String routWay;
        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routWay = handler.getClazz();
        RouterHandle o = (RouterHandle) Class.forName(routWay).newInstance();
        if(handler == ImUrlRouteWayEnum.CONSISTENT_HASH){
            //如果负载均衡是一致性哈希，反射调用配置文件的具体算法
            Method setHash = Class.forName(routWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay;
            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
            setHash.invoke(o,consistentHash);

        }
        return o;
    }

    @Bean
    public ZkClient buildZKClient(){
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

}
