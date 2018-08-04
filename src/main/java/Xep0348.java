import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.UUID;

public class Xep0348 {

    static XMPPTCPConnectionConfiguration config = null;
    static AbstractXMPPConnection con = null;

    private static final String CONSUMER_KEY = "a979a77ba735b9b0aa9bdd27595308ce647cbba72a2aa970e6ea80dc0c96fdc3";
    private static final String CONSUMER_SECRET = "502e77edce94ae0486809cb169b830bdbd698ce02bc5cae01f0dbdef5e7c6cf6";

    public Xep0348() {

        String dominio = "xmpp.binarylamp.cl";
        //String dominio = "localhost";
        String randomUUID = "",
                idRandom = "3212";



        try {
            config = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(dominio)
                    .setHost(dominio)
                    .setDebuggerEnabled(true)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .build();



            con = new XMPPTCPConnection(config);
            con.connect();
            System.out.println("Connected to " + con.getHost());

            randomUUID = UUID.randomUUID().toString();
            randomUUID = idRandom + randomUUID;

            AccountManager ac = AccountManager.getInstance(con);

            ac.sensitiveOperationOverInsecureConnection(true);


            DataForm dataFormRecibido = ac.getDataFormAttributes();
            Form FormReg = new Form(dataFormRecibido);

            Form formRespuesta = FormReg.createAnswerForm();
            formRespuesta.setAnswer("username", randomUUID);
            formRespuesta.setAnswer("password", randomUUID);
            formRespuesta.setAnswer("name", randomUUID);
            formRespuesta.setAnswer("oauth_consumer_key", CONSUMER_KEY);
            formRespuesta.setAnswer("oauth_signature", CONSUMER_SECRET);

            ac.createAccount(formRespuesta.getDataFormToSend());

            System.out.println("######### USUARIO CREADO #########\n#\tusuario: " + randomUUID + "\n#\tcontraseña: " + randomUUID + "\n#################################");


        } catch (Exception e) {
            System.out.println("ERROR: " + e.getStackTrace() + "\n" + e.getMessage()  );

        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (con.isConnected())
                    System.out.println("Cerrado... Desconectado...");
                con.disconnect();
            }
        });


        while (true){

        }

    }
}
