import org.firmata4j.IODevice;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.parts.Localpart;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

public class Standard {

    static Properties prop = new Properties();
    static BufferedReader inTeclado;

    // Conexión Arduino
    static IODevice arduino;
    static double temperatura, humedad, sensorAuxiliar;

    // Conexion XMPP
    static  String  dominio = "localhost",
            usuario = "thing-1",
            contrasena = "";
    static XMPPTCPConnectionConfiguration config = null;
    static AbstractXMPPConnection con = null;


    public Standard() throws IOException {
        arduino = new FirmataDevice("/dev/ttyACM0"); // Contructor de la instancia de Firmaata

        inTeclado = new BufferedReader(new InputStreamReader(System.in));


        //String dir = System.getProperty("user.dir");
        //System.out.println("Ruta de propiedades: " + dir);

        File f = new File(System.getProperty("user.dir") + "/config.properties");
        InputStream in;


        // Si no existe el archivo de configuración se crea y se completa
        if (!f.exists()) {
            System.out.println("Creando archivo de configuración: " + System.getProperty("user.dir") + "/config.properties");
            OutputStream out =  new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
            System.out.print("Dominio al cual conectar: ");
            dominio = inTeclado.readLine();
            usuario = UUID.randomUUID().toString().replace("-", "");
            contrasena = usuario;
            try {
                prop.setProperty("dominio", dominio);
                prop.setProperty("usuario", usuario);
                prop.setProperty("contrasena", contrasena);
                prop.store(out, "Configuración de Thing");
            } catch (IOException ioe) {
                System.out.println("Error al almacenar los datos");
            } finally {
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

                    AccountManager ac = AccountManager.getInstance(con);

                    ac.sensitiveOperationOverInsecureConnection(true);
                    ac.createAccount(Localpart.from(usuario), contrasena);
                } catch (Exception multyEx){
                    System.out.println("Error al crear la identidad en el servidor XMPP");
                    //System.
                } finally {
                    if (con != null)
                        con.disconnect();
                }

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Si el archivo de configuración existe, se carga la configuración
        if(f.exists() && !f.isDirectory()) {
            try {
                in = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
                System.out.println("Cargando archivo de configuración: " + System.getProperty("user.dir") + "/config.properties");
                prop.clear();
                prop.load(in);

                dominio = prop.getProperty("dominio");
                usuario = prop.getProperty("usuario");
                contrasena = prop.getProperty("contrasena");
                in.close();
            } catch (FileNotFoundException e){
                System.out.println("¡Error! No existe archivo de configuración");
            }
        }


        try {
            config = XMPPTCPConnectionConfiguration.builder()
                    .setXmppDomain(dominio)
                    .setHost(dominio)
                    .setUsernameAndPassword(usuario, usuario)
                    .setDebuggerEnabled(false)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource("test")
                    .build();

            con = new XMPPTCPConnection(config);
            con.connect().login();
            System.out.println("Conectado a " + con.getHost());

        } catch (Exception e){
            System.out.println("Error al conectarse a " + con.getHost());
        }

        // acepta todas las solicitudes
        Roster roster = Roster.getInstanceFor(con);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

        ChatManager chatManager = ChatManager.getInstanceFor(con);
        chatManager.addListener(new IncomingChatMessageListener() {
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                System.out.println("<IN : " + message.getBody());
                Message message1;
                if (message.getBody().equalsIgnoreCase("temp")){
                    try {
                        message1 = new Message(message.getTo(), "temp:" + temperatura);
                        message1.setType(Message.Type.chat);
                        System.out.println(">OUT:" + message1.toString());
                        chat.send(message1);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else if (message.getBody().equalsIgnoreCase("humi")){
                    try {
                        message1 = new Message(new Message(message.getTo(), "humi:" + humedad));
                        message1.setType(Message.Type.chat);
                        System.out.println(">OUT:" + message1.toString());
                        chat.send(message1);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (message.getBody().equalsIgnoreCase("auxi")){
                    try {
                        message1 = new Message(new Message(message.getTo(), "auxi:" + sensorAuxiliar));
                        message1.setType(Message.Type.chat);
                        System.out.println(">OUT:" + message1.toString());
                        chat.send(message1);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (message.getBody().equalsIgnoreCase("all")){
                    try {
                        message1 = new Message(new Message(message.getTo(), "temp:" + temperatura + ";humi:" + humedad + ";auxi:" + sensorAuxiliar ) );
                        message1.setType(Message.Type.chat);
                        System.out.println(">OUT:" + message1.toString());
                        chat.send(message1);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        message1 = new Message(new Message(message.getTo(), "Hola, soy una Cosa, que puede responder \"temp\", \"humi\", \"auxi\" y \"all\" "));
                        message1.setType(Message.Type.chat);
                        System.out.println(">OUT:" + message1.toString());
                        chat.send(message1);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        try {
            arduino.start(); // initiate communication to the device
            arduino.ensureInitializationIsDone(); // wait for initialization is done
        }
        catch (IOException IOe){
            System.out.println("Ha ocurrido un error al conectarse al arduino\n\t" + IOe.getMessage());
        }
        catch (InterruptedException Iex){
            System.out.println("La conexion ha sido interrumpida.\n\t" + Iex.getMessage());
            arduino.stop();

        }

        //System.out.println("PinsCount es: " + device.getPinsCount());
        Pin pinTemp = arduino.getPin(14);

        pinTemp.setMode(Pin.Mode.ANALOG);
        pinTemp.addEventListener(new PinEventListener() {
            public void onModeChange(IOEvent ioEvent) {
                //System.out.println("El MODE del pinTEMP ha cambiado.");
            }

            public void onValueChange(IOEvent ioEvent) {
                //System.out.println("La temperatura es: " + (double)(ioEvent.getValue()) * (5/10.24));
                temperatura = (double)(ioEvent.getValue()) * (5/10.24);
            }
        });

        Pin pinHum = arduino.getPin(15);
        pinHum.setMode(Pin.Mode.ANALOG);
        pinHum.addEventListener(new PinEventListener() {
            public void onModeChange(IOEvent ioEvent) {
                //System.out.println("El MODE del pinHUM ha cambiado.");
            }

            public void onValueChange(IOEvent ioEvent) {
                //System.out.println("La humedad es: " + (double)(ioEvent.getValue()) * (5/10.24));
                humedad = (double)(ioEvent.getValue()) * (5/10.24);
            }
        });

        Pin pinAux = arduino.getPin(16);
        pinAux.setMode(Pin.Mode.ANALOG);
        pinAux.addEventListener(new PinEventListener() {
            public void onModeChange(IOEvent ioEvent) {
                //System.out.println("El MODE del pinHUM ha cambiado.");
            }

            public void onValueChange(IOEvent ioEvent) {
                //System.out.println("La humedad es: " + (double)(ioEvent.getValue()) * (5/10.24));
                sensorAuxiliar = (double)(ioEvent.getValue());
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (con.isConnected())
                    System.out.println("Cerrado... Desconectado...");
                    con.disconnect();
            }
        });


        while(true){

        }
    }
}
