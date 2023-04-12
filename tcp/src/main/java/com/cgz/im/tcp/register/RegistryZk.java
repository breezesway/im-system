package com.cgz.im.tcp.register;

import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目启动时开启一个线程创建节点
 */
public class RegistryZk implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RegistryZk.class);

    private ZKit zKit;

    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZk(ZKit zKit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.zKit = zKit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {
        zKit.createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        logger.info("Registry zookeeper webPath success, msg=[{}]", tcpPath);
    }
}
