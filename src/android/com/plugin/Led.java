package com.plugin;

import android.content.Context;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.enums.GEDI_LED_e_Id;
import br.com.gertec.gedi.exceptions.GediException;
import br.com.gertec.gedi.interfaces.IGEDI;
import br.com.gertec.gedi.interfaces.ILED;

public class Led {

    private IGEDI iGedi = null;
    private ILED iLed;

    public Led(Context context){
        new Thread(() -> {
            GEDI.init(context);
            this.iGedi = GEDI.getInstance(context);
            this.iLed = GEDI.getInstance().getLED();
            try {
                new Thread().sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String ledOff() {
        for (GEDI_LED_e_Id c : GEDI_LED_e_Id.values()) {

            if (c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_RED) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_GREEN) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_ORANGE) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_BLUE)) {
                try {
                    iLed.Set(c, false);
                } catch (GediException e) {
                    return "iLed.Set - " + c + ":- FAIL -- " + e.getErrorCode().name();
                } catch (Exception e) {
                    return "iLed.Set - \t\t\t- FAIL - " + e.getMessage();
                }
            }
        }
        return "OK";
    }

    public String ledOn() {
        for (GEDI_LED_e_Id c : GEDI_LED_e_Id.values()) {

            if (c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_RED) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_GREEN) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_ORANGE) ||
                    c.equals(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_BLUE)) {

                try {
                    iLed.Set(c, true);
                } catch (GediException e) {
                    return "iLed.Set - " + c + ":\t- FAIL -- " + e.getErrorCode().name();

                } catch (Exception e) {
                    return "iLed.Set - \t- FAIL - " + e.getMessage();
                }
            }


        }
        return "OK";
    }

    public String ledRedOn(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_RED,true);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledBlueOn(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_BLUE,true);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledGreenOn(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_GREEN,true);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledOrangeOn(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_ORANGE,true);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }

    public String ledRedOff(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_RED,false);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledBlueOff(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_BLUE,false);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledGreenOff(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_GREEN,false);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
    public String ledOrangeOff(){
        try {
            iLed.Set(GEDI_LED_e_Id.GEDI_LED_ID_CONTACTLESS_ORANGE,false);
        } catch (GediException e) {
            e.printStackTrace();
            return e.getErrorCode().name();
        }
        return "OK";
    }
}
