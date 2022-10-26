package com.plugin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import static android.app.Activity.RESULT_OK;
import static android.app.Activity.RESULT_CANCELED;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import static android.hardware.Camera.Parameters.FLASH_MODE_ON;

//Imports Gertec
import com.plugin.Beep;
import com.plugin.ConfigPrint;
import com.plugin.Led;
import com.plugin.Printer;
//-----------------------------------------------------------


//imports Clisitef
import br.com.softwareexpress.sitef.android.CliSiTef;
import br.com.softwareexpress.sitef.android.CliSiTefI;
import br.com.softwareexpress.sitef.android.ICliSiTefListener;
import android.os.Handler;
import br.com.gertec.ppcomp.PPComp;

//---------------------------------------------------------------
public class MainActivity extends CordovaPlugin implements ICliSiTefListener{

    private CallbackContext callbackContext;
    private Intent intent;
    private String status;

    private Beep beep;
    private Led led;
    private Printer print;
    private ConfigPrint configPrint = new ConfigPrint();


    //Implementação Clisitef passando os dados como Paramentro

    private CliSiTef cliSiTef = null;
    private int trnResultCode;
    private static final int CAMPO_COMPROVANTE_CLIENTE = 121;
    private static final int CAMPO_COMPROVANTE_ESTAB = 122;
    private static int REQ_CODE = 4321;

    private static MainActivity instance;
    private class RequestCode {
        private static final int GET_DATA = 1;
        private static final int END_STAGE_1_MSG = 2;
        private static final int END_STAGE_2_MSG = 3;
    }
    private int id;
    private static String title;

    //Variaveis parametros entrada pagamento

    //Variaveis configuração servidor
    //-------------------------------
    private String confIpSitef;
    private String confCodigoLoja;
    private String confNumeroTerminal;
    //-------------------------------
    //Variaveis startTransaction
    //-------------------------------
    private String startValor;
    private String startCupomFiscal;
    private String startDataFiscal;
    private String startHorario;
    private String startOperador;
    //-------------------------------
    //Variaveis ContinueTransaction
    //-------------------------------
    private String contFormaPagamento;

    private int pulaLinha;
    private String mensagem;

    //Variaveis retorno Pagamento
    private static String titulo;
    private String statusPagamento="";
    private String impressão="";
    private String nsu;
    private String nsuHost;
    private String nomePortadorCartao;
    private String autorizador;
    private String tipoCartao;
    private String codigoAprovacaoTransacaoCredito;
    private String embosso;
    private String dataValidadeCartao;
    private String numeroCartao;
    private String dataHoraTransacao;
    private String codigoRedeAutorizadora;
    private String nomeInstituicao;
    private String codigoEstabelecimento;
    private String modalidade;
    private boolean isCancelado;
    private boolean isSolicitadoRemoverCartao;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
        beep = new Beep(cordova.getActivity().getApplicationContext());
        led = new Led(cordova.getActivity().getApplicationContext());
        print = new Printer(cordova.getActivity().getApplicationContext());
    }

    public MainActivity() {
        super();
    }
    public void OnDestroy(){
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        this.callbackContext = callbackContext;
        intent = null;
        //Metodos Pagamento
        if (action.equals("pagamento")) {
            //Seta valores recebidos as variaveis de configuração
            JSONObject params = args.getJSONObject(0);
            this.confIpSitef = params.getString("ipSitef");
            this.confCodigoLoja = params.getString("codigoLoja");
            this.confNumeroTerminal = params.getString("numeroTerminal");
            this.startValor = params.getString("valor");
            this.startCupomFiscal = params.getString("cupomFiscal");
            this.startDataFiscal = params.getString("dataFiscal");
            this.startHorario = params.getString("horario");
            this.startOperador = params.getString("operador");
            this.contFormaPagamento = params.getString("formaPagamento");
            this.isCancelado = false;
            this.isSolicitadoRemoverCartao = false;
            new Thread(() -> {
                int idConfig = 0;
                int retornoStartTransaction =0;
                try{
                    if(this.cliSiTef == null){
                        this.cliSiTef = new CliSiTef(cordova.getActivity().getApplicationContext());
                        this.cliSiTef.setMessageHandler(hndMessage);
                        this.cliSiTef.setDebug(true);
                        idConfig = this.cliSiTef.configure(
                                this.confIpSitef,
                                this.confCodigoLoja,
                                this.confNumeroTerminal,
                                "TipoPinPad=Android_AUTO");
                        if(idConfig!=0){
                            switch (idConfig){
                                case 1:
                                    setStatus("Endereço IP inválido ou não resolvido");
                                    break;
                                case 2:
                                    setStatus("Codigo da loja inválido.");
                                    break;
                                case 3:
                                    setStatus("Código terminal inválido");
                                    break;
                                case 6:
                                    setStatus("Erro na inicialização do Tcp/Ip");
                                    break;
                                case 7:
                                    setStatus("Falta de memória");
                                    break;
                                case 8:
                                    setStatus("Não encontrou a CliSiTef ou ela está com problemas");
                                    break;
                                case 9:
                                    setStatus("Configuração de servidores SiTef foi excedida.");
                                    break;
                                case 10:
                                    setStatus("Erro de acesso na pasta CliSiTef (possível falta de permissão para escrita).");
                                    break;
                                case 11:
                                    setStatus("Dados inválidos passados pela automação.");
                                    break;
                                case 12:
                                    setStatus("Modo seguro não ativo (possível falta de configuração no servidor SiTef do arquivo .cha).");
                                    break;
                                case 13:
                                    setStatus("Caminho DLL inválido (o caminho completo das bibliotecas está muito grande).");
                                    break;
                                default:
                                    break;
                            }
                            this.cliSiTef = null;
                            callbackContext.error("Erro configuração transação");

                        }
                    }
                    this.cliSiTef.setActivity(cordova.getActivity());
                    retornoStartTransaction = this.cliSiTef.startTransaction(
                            this,
                            0,
                            this.startValor,
                            this.startCupomFiscal,
                            this.startDataFiscal,
                            this.startHorario,
                            this.startOperador,
                            "");
                    if(retornoStartTransaction != 1000){
                        callbackContext.error(
                                    "Retorno startTransaction diferente de 10000. Código: "
                                            + retornoStartTransaction
                                            +"Configuracao = "
                                            +idConfig
                        );
                    }

                }catch (Exception e){
                    callbackContext.error("Erro " + e.getMessage());
                }

                try {
                    new Thread().sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            //Inicia biblioteca Clisitef
            this.statusPagamento="Iniciando pagamento";
            callbackContext.success("PagamentoFinalizado");
            return true;
        }
        if (action.equals("getTitulo")) {
            new Thread(() -> {
                callbackContext.success(this.statusPagamento);
                try {
                    new Thread().sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return true;
        }
        if(action.equals("GetStringImpressao")) {
            callbackContext.success(this.impressão);
            return true;
        }
        if(action.equals("cancelarTransacao")) {
            try{
                this.cliSiTef.continueTransaction("-1");
                int i = this.cliSiTef.abortTransaction(-1);
                this.onTransactionResult(1,0);
                PPComp.getInstance(context).PP_Abort();
                this.titulo="";
                this.statusPagamento="Transação Cancelada";
                this.impressão="";
                this.nsu="";
                this.nsuHost="";
                this.nomePortadorCartao="";
                this.autorizador="";
                this.tipoCartao="";
                this.codigoAprovacaoTransacaoCredito="";
                this.embosso="";
                this.dataValidadeCartao="";
                this.numeroCartao="";
                this.dataHoraTransacao="";
                this.codigoRedeAutorizadora="";
                this.nomeInstituicao="";
                this.codigoEstabelecimento="";
                this.modalidade="";


            }catch (Exception e){
                callbackContext.error(e.getMessage());
            }
            callbackContext.success("Transação Cancelada");
            return true;
        }
        if(action.equals("limpaTransacao")) {
            try{
                this.cliSiTef=null;
                this.titulo="";
                this.statusPagamento="";
                this.impressão="";
            }catch (Exception e){
                callbackContext.error(e.getMessage());
            }
            callbackContext.success("Transação Cancelada");
            return true;
        }
        if(action.equals("GetDadosTransacao")) {
            JSONObject jo = new JSONObject();
            jo.put("nsu", this.nsu);
            jo.put("nsuHost", this.nsuHost);
            jo.put("nomePortadorCartao", this.nomePortadorCartao);
            jo.put("autorizador", this.autorizador);
            jo.put("tipoCartao", this.tipoCartao);
            jo.put("codigoAprovacaoTransacaoCredito", this.codigoAprovacaoTransacaoCredito);
            jo.put("embosso", this.embosso);
            jo.put("dataValidadeCartao", this.dataValidadeCartao);
            jo.put("numeroCartao", this.numeroCartao);
            jo.put("dataHoraTransacao", this.dataHoraTransacao);
            jo.put("codigoRedeAutorizadora", this.codigoRedeAutorizadora);
            jo.put("nomeInstituicao", this.nomeInstituicao);
            jo.put("codigoEstabelecimento", this.codigoEstabelecimento);
            jo.put("modalidade", this.modalidade);

            callbackContext.success(jo);
            return true;
        }


        //Impressão
        if (action.equals("checarImpressora")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = print.getStatusImpressora();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("imprimir")) {
            try {
                print.getStatusImpressora();
                if (print.isImpressoraOK()) {
                    JSONObject params = args.getJSONObject(0);
                    String tipoImpressao = params.getString("tipoImpressao");

                    switch (tipoImpressao) {
                        case "Texto":
                            mensagem = params.getString("mensagem");
                            String alinhar = params.getString("alinhar");
                            int size = params.getInt("size");
                            String fontFamily = params.getString("font");
                            Boolean opNegrito = params.getBoolean("opNegrito");
                            Boolean opItalico = params.getBoolean("opItalico");
                            Boolean opSublinhado = params.getBoolean("opSublinhado");

                            print.confgPrint(opItalico,opSublinhado,opNegrito,size,fontFamily,alinhar);
                            print.imprimeTexto(mensagem);
                            print.ImpressoraOutput();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Erro " + e.getMessage());
            }
            callbackContext.success("Adicionado ao buffer");
            return true;
        }
        if (action.equals("impressoraOutput")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONObject params = args.getJSONObject(0);
                        if (params.has("avancaLinha")) {
                            pulaLinha = params.getInt("avancaLinha");
                            print.avancaLinha(pulaLinha);
                        }
                        print.ImpressoraOutput();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                    callbackContext.success("Buffer impresso");
                }
            });
            return true;
        }

        //Métodos Led
        if (action.equals("ledOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOn();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOff();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledRedOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledRedOn();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledBlueOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledBlueOn();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledGreenOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledGreenOn();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOrangeOn")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOrangeOn();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledRedOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledRedOff();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledBlueOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledBlueOff();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledGreenOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledGreenOff();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }
        if (action.equals("ledOrangeOff")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = led.ledOrangeOff();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }

        //Metodo Beep
        if (action.equals("beep")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        status = beep.beep();
                        callbackContext.success(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callbackContext.error("Erro " + e.getMessage());
                    }
                }
            });
            return true;
        }

        return false; // Returning false results in a "MethodNotFound" error.
    }

    private static Handler hndMessage = new Handler() {
        public void handleMessage(android.os.Message message) {
            switch (message.what) {
                case CliSiTefI.EVT_INICIA_ATIVACAO_BT:
                    //MainActivity.instance.statusPagamento ="Ativando BT";
                    Log.i("Handler","Ativando BT");
                    break;
                case CliSiTefI.EVT_FIM_ATIVACAO_BT:
                    //MainActivity.instance.statusPagamento ="PinPad";
                    Log.i("Handler","PinPad");
                    break;
                case CliSiTefI.EVT_INICIA_AGUARDA_CONEXAO_PP:
                    //MainActivity.instance.statusPagamento ="Aguardando pinpad";
                    Log.i("Handler","Aguardando pinpad");
                    break;
                case CliSiTefI.EVT_FIM_AGUARDA_CONEXAO_PP:
                    //MainActivity.instance.statusPagamento ="";
                    Log.i("Handler","");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURANDO:
                    //MainActivity.instance.statusPagamento ="Configurando pinpad";
                    Log.i("Handler","Configurando pinpad");
                    break;
                case CliSiTefI.EVT_PP_BT_CONFIGURADO:
                    //MainActivity.instance.statusPagamento ="Pinpad configurado";
                    Log.i("Handler","Pinpad configurado");
                    break;
                case CliSiTefI.EVT_PP_BT_DESCONECTADO:
                    //MainActivity.instance.statusPagamento ="Pinpad desconectado";
                    Log.i("Handler","Pinpad desconectado");
                    break;
            }
        }
    };

    @Override
    public void onData(int stage, int command, int fieldId, int minLength, int maxLength, byte[] input) {
        String data = "";
        if (stage == 1) {
            // Evento onData recebido em uma startTransaction
            Log.i("Stage1","Comando: "+command+" fieldId: "+fieldId+" "+this.cliSiTef.getBuffer());
            //Tratamento Retorno cartao
            switch (fieldId){
                //Verifica se em caso de cancelamnto pelo pinpad o cartão foi retirado
                //-------------------------------------------------------------------------
                case -1:

                    if(isCancelado && isSolicitadoRemoverCartao){
                        this.statusPagamento = "Cartão removido";

                    }else{
                        if(this.cliSiTef.getBuffer().equals("13 - Operacao Cancelada")){
                            this.isCancelado = true;
                        }
                        if(this.cliSiTef.getBuffer().equals("Retire o cartao da leitora")){
                            this.isSolicitadoRemoverCartao = true;
                        }
                    }
                    break;
                //--------------------------------------------------------------------------------
                case 133:
                    this.nsu = this.cliSiTef.getBuffer();
                    break;
                case 134:
                    this.nsuHost = this.cliSiTef.getBuffer();
                    break;
                case 1003:
                    this.nomePortadorCartao = this.cliSiTef.getBuffer();
                    break;
                case 131:
                    this.autorizador = this.cliSiTef.getBuffer();
                    break;
                case 132:
                    this.tipoCartao = this.cliSiTef.getBuffer();
                    break;
                case 135:
                    this.codigoAprovacaoTransacaoCredito = this.cliSiTef.getBuffer();
                    break;
                case 1190:
                    this.embosso = this.cliSiTef.getBuffer();
                    break;
                case 1002:
                    this.dataValidadeCartao = this.cliSiTef.getBuffer();
                    break;
                case 2021:
                    this.numeroCartao = this.cliSiTef.getBuffer();
                    break;
                case 105:
                    this.dataHoraTransacao =this.cliSiTef.getBuffer();
                    break;
                case 158:
                    this.codigoRedeAutorizadora =this.cliSiTef.getBuffer();
                    break;
                case 156:
                    this.nomeInstituicao = this.cliSiTef.getBuffer();
                    break;
                case 157:
                    this.codigoEstabelecimento = this.cliSiTef.getBuffer();
                    break;
                case 101:
                    this.modalidade = this.cliSiTef.getBuffer();
                    break;
                default:
                    break;
            }

        } else if (stage == 2) {
            // Evento onData recebido em uma finishTransaction
            Log.i("Stage2",this.cliSiTef.getBuffer());

        }
        switch (command) {
            case CliSiTef.CMD_RESULT_DATA:
                switch (fieldId) {
                    case CAMPO_COMPROVANTE_CLIENTE:
                        Log.i("CAMPO_COMPROVANTE_CLIENTE",this.cliSiTef.getBuffer());
                    case CAMPO_COMPROVANTE_ESTAB:
                        Log.i("CAMPO_COMPROVANTE_ESTAB",this.cliSiTef.getBuffer());
                        imprimir(this.cliSiTef.getBuffer());
                }
                break;
            case CliSiTef.CMD_SHOW_MSG_CASHIER:
            case CliSiTef.CMD_SHOW_MSG_CUSTOMER:
            case CliSiTef.CMD_SHOW_MSG_CASHIER_CUSTOMER:
                Log.i("CMD_SHOW_MSG_CASHIER_CUSTOMER",this.cliSiTef.getBuffer());

                setStatus(this.cliSiTef.getBuffer());
                break;
            case CliSiTef.CMD_SHOW_MENU_TITLE:
            case CliSiTef.CMD_SHOW_HEADER:
                //Primiro Entrada
                title = this.cliSiTef.getBuffer();
                setStatus(this.cliSiTef.getBuffer());
                Log.i("CMD_SHOW_HEADER",this.cliSiTef.getBuffer());
                break;
            case CliSiTef.CMD_CLEAR_MSG_CASHIER:
            case CliSiTef.CMD_CLEAR_MSG_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MSG_CASHIER_CUSTOMER:
            case CliSiTef.CMD_CLEAR_MENU_TITLE:
            case CliSiTef.CMD_CLEAR_HEADER:
                Log.i("CMD_CLEAR_HEADER",this.cliSiTef.getBuffer());
                setStatus(this.cliSiTef.getBuffer());
                break;
            case CliSiTef.CMD_CONFIRM_GO_BACK:
            case CliSiTef.CMD_CONFIRMATION: {
                Log.i("CMD_CONFIRMATION",this.cliSiTef.getBuffer());
                setStatus(this.cliSiTef.getBuffer());
                String ret = this.cliSiTef.getBuffer();
                int i = this.cliSiTef.abortTransaction(-1);
                return;
            }
            case CliSiTef.CMD_GET_FIELD_CURRENCY:
            case CliSiTef.CMD_GET_FIELD_BARCODE:
            case CliSiTef.CMD_GET_FIELD: {
                Log.i("CMD_GET_FIELD",this.cliSiTef.getBuffer());
                setStatus(this.cliSiTef.getBuffer());
                String ret = this.cliSiTef.getBuffer();
                this.cliSiTef.continueTransaction("");
                return;
            }
            case CliSiTef.CMD_GET_MENU_OPTION: {
                Log.i("CMD_GET_MENU_OPTION",this.cliSiTef.getBuffer());
                String ret = this.cliSiTef.getBuffer();
                switch (ret){
                    case "1:Cheque;2:Cartao de Debito;3:Cartao de Credito;4:Cartao Private Label;5:Confirmacao de Pre-autorizacao;":
                        data = this.contFormaPagamento;
                        this.cliSiTef.continueTransaction(data);
                        break;
                    default:
                        data = "1";
                        this.cliSiTef.continueTransaction(data);
                        break;
                }
                return;
            }
            case CliSiTef.CMD_PRESS_ANY_KEY: {
                Log.i("CMD_PRESS_ANY_KEY",this.cliSiTef.getBuffer());
                String ret = this.cliSiTef.getBuffer();
                int i = this.cliSiTef.abortTransaction(-1);
                setStatus(this.cliSiTef.getBuffer());
                return;
            }
            case CliSiTef.CMD_ABORT_REQUEST:
                Log.i("CMD_ABORT_REQUEST",this.cliSiTef.getBuffer());
                setStatus(this.cliSiTef.getBuffer());
                break;
            default:
                Log.i("default","default");
                break;
        }
        this.cliSiTef.continueTransaction(data);
    }
    public void setStatus(String s){
        if(!s.equals("")){
            this.statusPagamento = s;
        }

    }
    private void alert(String message) {
        String mensagem = message;
    }
    @Override
    public void onTransactionResult(int stage, int resultCode) {
        trnResultCode = resultCode;
        //alert ("Fim do estágio " + stage + ", retorno " + resultCode);
        if (stage == 1 && resultCode == 0) { // Confirm the transaction
            try {
                setStatus(this.cliSiTef.getBuffer());
                this.cliSiTef.finishTransaction(1);
                setStatus("Finalizado");
            } catch (Exception e) {
                Log.e("onTransactionResult",e.getMessage());
            }
        } else {
            if (resultCode == 0) {
                //Transação ok e pode exibir comprovante
                setStatus(this.cliSiTef.getBuffer());
            } else {
                //Finaliza aplicação
            }
        }
    }
    private void imprimir(String texto){
        this.impressão = texto;
    }

}
