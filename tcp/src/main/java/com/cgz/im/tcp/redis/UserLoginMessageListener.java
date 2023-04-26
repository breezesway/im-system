package com.cgz.im.tcp.redis;

import com.alibaba.fastjson.JSONObject;
import com.cgz.im.codec.proto.MessagePack;
import com.cgz.im.common.ClientType;
import com.cgz.im.common.constant.Constants;
import com.cgz.im.common.enums.DeviceMultiLoginEnum;
import com.cgz.im.common.enums.command.SystemCommand;
import com.cgz.im.common.model.UserClientDto;
import com.cgz.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * redis监听登录，根据不同策略实现多端登录
 * 1.单端登录：一端在线，踢掉除了本ClientType+imei的设备
 * 2.双端登录：允许pc和手机其中一端登录+web端
 * 3.三端登录：允许手机+pc+web，踢掉同端的其他imei，除了web
 * 4.多端登录：不做处理
 */
public class UserLoginMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin(){
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                logger.info("收到用户上线通知:"+msg);
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
                //获取该用户所有设备的登录情况，看是否需要把其他设备踢下线
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    if(loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()){
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(!(clientType+":"+imei).equals(dto.getClientType()+":"+dto.getImei())){
                            //告诉客户端，其他端登录
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }else if(loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()){
                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            continue;
                        }
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        if(clientType == ClientType.WEB.getCode()){
                            continue;
                        }
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if(!(clientType+":"+imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        //web端都可存在
                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            continue;
                        }
                        boolean isSameClient = false;
                        //手机端只允许一个存在
                        if((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) &&
                                (dto.getClientType() == ClientType.IOS.getCode() || dto.getClientType() == ClientType.ANDROID.getCode())){
                            isSameClient = true;
                        }
                        //PC端也只允许一个存在
                        if((clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode()) &&
                                (dto.getClientType() == ClientType.MAC.getCode() || dto.getClientType() == ClientType.WINDOWS.getCode())){
                            isSameClient = true;
                        }
                        if(isSameClient && !(clientType+":"+imei).equals(dto.getClientType()+":"+dto.getImei())){
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }
                }
            }
        });
    }
}
