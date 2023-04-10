package com.cgz.im.common.enums.command;

public enum SystemCommand implements Command{

    /**
     * 心跳 9999
     */
    PING(0x270f),
    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    /**
     * 登出 9003
     */
    LOGOUT(0x232b),
    ;

    private int command;

    SystemCommand(int code){this.command = code;}

    public int getCode(){return command;}

    @Override
    public int getCommand() {
        return command;
    }
}
