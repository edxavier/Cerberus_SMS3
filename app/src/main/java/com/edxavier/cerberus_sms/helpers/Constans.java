package com.edxavier.cerberus_sms.helpers;

/**
 * Created by Eder Xavier Rojas on 26/10/2015.
 */
public class Constans {

    public static final String CLARO    = "Claro";
    public static final String MOVISTAR    = "Movistar";
    public static final String COOTEL    = "CooTel";
    public static final String CONVENCIONAL    = "Línea Fija";
    public static final String INTERNACIONAL    = "Exterior";
    public static final String DESCONOCIDO    = "Desconocido";

    public static final int BLOCK_NONE    = 0;
    public static final int BLOCK_MESSAGES    = 1;
    public static final int BLOCK_CALLS    = 2;
    public static final int BLOCK_BOTH    = 3;



    public static final int MESSAGE_READ    = 1;
    public static final int MESSAGE_UNREAD    = 0;

    public static final int MESSAGE_TYPE_ALL    = 0;
    public static final int MESSAGE_TYPE_INBOX  = 1;
    public static final int MESSAGE_TYPE_SENT   = 2;
    public static final int MESSAGE_TYPE_DRAFT  = 3;
    public static final int MESSAGE_TYPE_OUTBOX = 4;
    public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
    public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later


}
