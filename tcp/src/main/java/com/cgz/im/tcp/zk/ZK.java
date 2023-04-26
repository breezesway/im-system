package com.cgz.im.tcp.zk;

import com.cgz.im.codec.config.BootstrapConfig;
import com.cgz.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ZK {

    private static final Logger logger = LoggerFactory.getLogger(ZK.class);
    private static ZkClient zkClient;
    private static String ip;
    private static BootstrapConfig.TcpConfig tcpConfig;

    public static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZK.zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(), config.getLim().getZkConfig().getZkConnectTimeOut());
        ZK.ip = hostAddress;
        ZK.tcpConfig = config.getLim();

        createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        createNode(webPath);
        logger.info("Registry zookeeper webPath success, msg=[{}]", webPath);
    }

    //im-coreRoot/tcp/ip:port
    public static void createRootNode(){

        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot+Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot+Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot+Constants.ImCoreZkRootWeb);
        if(!webExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot+Constants.ImCoreZkRootWeb);
        }
    }

    //ip+port
    public static void createNode(String path){
        if(!zkClient.exists(path)){
            zkClient.createEphemeral(path);
        }
    }

}
