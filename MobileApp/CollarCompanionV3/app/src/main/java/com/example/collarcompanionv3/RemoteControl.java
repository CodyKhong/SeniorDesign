package com.example.collarcompanionv3;

import java.nio.charset.StandardCharsets;

public class RemoteControl {
    //    private final static byte START = 0x11;
    private final static byte LED_STATE = 0x1;
    private final static byte LED_COLOR = 0x2;

    private final static byte red = 0x11;
    private final static byte green = 0x22;
    private final static byte blue = 0x33;

    private final static byte VALUE_OFF = 0x11;
    private final static byte VALUE_ON = 0x10;

    private BLEController bleController;

    public RemoteControl(BLEController bleController) {
        this.bleController = bleController;
    }

    private byte [] createControlWord(byte type, byte ... args) {
        byte [] command = new byte[args.length + 3];
//        command[0] = START;
        command[0] = type;
        command[1] = (byte)args.length;
        for(int i=0; i<args.length; i++)
            command[i+2] = args[i];

        return command;
    }

    public void switchLED(boolean on) {
        this.bleController.sendData(createControlWord(LED_STATE, on?VALUE_ON:VALUE_OFF));
    }

    public void DATASEND(int RGB) {

        byte RedByte = (byte)((RGB>>16) & 0xFF);
        byte GreenByte = (byte)((RGB>>8) & 0xFF);
        byte BlueByte = (byte)(RGB & 0xFF);
        this.bleController.sendData(createControlWord(LED_COLOR, RedByte,GreenByte,BlueByte));
    }

//    public void heartbeat() {
//        this.bleController.sendData(createControlWord(HEARTBEAT));
//    }
}
