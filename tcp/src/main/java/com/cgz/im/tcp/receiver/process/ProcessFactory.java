package com.cgz.im.tcp.receiver.process;

public class ProcessFactory {

    public static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command){
        return defaultProcess;
    }
}
