package com.plugin;

import android.content.Context;
import android.util.Log;

import br.com.gertec.gedi.GEDI;
import br.com.gertec.gedi.interfaces.IAUDIO;
import br.com.gertec.gedi.interfaces.IGEDI;


public class Beep {

    private IAUDIO iAudio;
    private IGEDI iGedi = null;
    public Beep(Context context){
        new Thread(() -> {
            GEDI.init(context);
            this.iGedi = GEDI.getInstance(context);
            this.iAudio = GEDI.getInstance().getAUDIO();
            try {
                new Thread().sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public String beep(){
        try {
            this.iAudio.Beep();
        }catch (Exception e){
            Log.e("Erro Beep",e.getMessage());
            return e.getMessage();
        }
        return "OK";
    }
}
