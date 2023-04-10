package com.cgz.im.common.enums;

import com.cgz.im.common.exception.ApplicationExceptionEnum;

public enum UserErrorCode implements ApplicationExceptionEnum {

    IMPORT_SIZE_TOO_LARGE(20000,"导入數量太大"),
    USER_NOT_EXIST(20001,"用户不存在"),
    SERVER_GET_USER_ERROR(20002,"服务获取用户失败"),
    MODIFY_USER_ERROR(20003,"更新用户失败"),
    SERVER_NOT_AVAILABLE(71000, "没有可用的服务"),
    ;

    private int code;
    private String error;

    UserErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}
