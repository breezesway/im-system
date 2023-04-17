package com.cgz.im.tcp.feign;

import com.cgz.im.common.ResponseVO;
import com.cgz.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    ResponseVO checkSendMessage(CheckSendMessageReq o);
}
