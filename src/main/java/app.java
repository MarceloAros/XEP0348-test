import org.apache.commons.lang.math.NumberUtils;

import java.io.IOException;

public class app {

    public static void main(String argumentos[]){
        // 0 = error ;  1 = standard ; 2 = xep0348 ; 3 = pentest
        byte camino = 0;
        if (argumentos.length == 0 || argumentos.length == 1){
            if (argumentos.length == 0) {
                camino = 1;
            } else {
                if (argumentos[0].equalsIgnoreCase("standard")) camino = 1;
                else if (argumentos[0].equalsIgnoreCase("xep0348")) camino = 2;
                else camino = 0;
            }
        } else if (argumentos.length == 3) {
            if (argumentos[0].equalsIgnoreCase("pentest") && NumberUtils.isNumber(argumentos[2])) camino = 3;
        } else
            camino = 0;

        switch (camino){
            case 0: System.out.println("Error de sintaxis. Intente con\n\tthing\n\tthing standard\n\tthing xep0348\n\tthing pentest <host> <intentos p/sec>");
                break;
            case 1:{
                System.out.println("### STANDARD ####");
                try {
                    new Standard();
                } catch (IOException e){
                    System.out.println("Error de E/S");
                }

                break;
            }
            case 2:{
                System.out.println("### XEP0348 ####");
                try {
                    new Xep0348();
                } catch (Exception e){
                    System.out.println("Error de E/S");
                }
                break;
            }
            case 3:{
                System.out.println("### PENTEST ####");
                new PenTest(argumentos[1], Integer.parseInt(argumentos[2]));
                break;
            }
            default:
                System.out.println("Error de sintaxis. Intente con\n\tthing\n\tthing standard\n\tthing xep0348\n\tthing pentest <host>");
                break;
        }
    }

}
