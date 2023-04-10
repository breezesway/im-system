package com.cgz.im.common.enums.command;

public enum SystemCommand implements Command{

    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    ;

    private int command;

    SystemCommand(int code){this.command = code;}

    public int getCode(){return command;}

    @Override
    public int getCommand() {
        return command;
    }
}