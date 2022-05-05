package servidor;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Esta clase contiene al metodo principal por el cual ser reciben y validan los
 * parametros para dar de alta un ServerSocket y generar instancias de la clase
 * Campo a la cual se le agregan los Sockets aceptados por el servidor.
 */
public class AdministradorConexiones {

    public static void main(String[] args) throws Exception {
        ServerSocket servidorSockets = null;
        IConectarSocketJugador campo = Campo.campoNulo;
        int cantidadParcelasX = 0;
        int cantidadParcelasY = 0;
        int cantidadMinas = 0;
        int intervaloEntreExplosiones = 0;
        int numeroPuertoServidor = 0;
        int numeroPartidasTotales = 0;
        String erroresConcatenados = "";

        if (args.length != 5) {
            System.err.println("\nArgumentos:"
                    + "\n1.- Cantidad de parcelas en X [ 1 - 50 ]."
                    + "\n2.- Cantidad de parcelas en Y [ 1 - 50 ]."
                    + "\n3.- Cantidad minas, desde 1 y menor a la cantidad de percelas totales."
                    + "\n4.- Milisegundos para el intervalo de explosiones automáticas, 0 infinito."
                    + "\n5.- Número de puerto [ 0 - 65535 ].");
            System.exit(1);
        }

        try {
            cantidadParcelasX = Integer.valueOf(args[0]);
            if (cantidadParcelasX < 1 || cantidadParcelasX > 50) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            erroresConcatenados += "\n1.- La cantidad de parcelas en X tiene que ser un entero en el rango [ 1 - 50 ].";
        }
        try {
            cantidadParcelasY = Integer.valueOf(args[1]);
            if (cantidadParcelasY < 1 || cantidadParcelasY > 50) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            erroresConcatenados += "\n2.- La cantidad de parcelas en Y tiene que ser un entero en el rango [ 1 - 50 ].";
        }

        try {
            cantidadMinas = Integer.valueOf(args[2]);
            if (cantidadMinas < 1 || cantidadMinas >= cantidadParcelasX * cantidadParcelasY) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            erroresConcatenados += "\n3.- La cantidad de minas tiene que ser un entero, desde 1 y menor a la cantidad de percelas totales.";
        }

        try {
            intervaloEntreExplosiones = Integer.valueOf(args[3]);
        } catch (NumberFormatException ex) {
            erroresConcatenados += "\n4.- Los milisegundos de intervalo entre explosiones automáticas tienen que ser entero, 0 para infinito.";
        }

        try {
            numeroPuertoServidor = Integer.valueOf(args[4]);
            if (numeroPuertoServidor < 1 || numeroPuertoServidor > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            erroresConcatenados += "\n5.- El número de puerto tiene que ser un entero en el rango [ 0 - 65535 ].";
        }

        if (erroresConcatenados.length() != 0) {
            System.err.println(erroresConcatenados);
            System.exit(1);
        }

        try {
            servidorSockets = new ServerSocket(numeroPuertoServidor);
        } catch (BindException ex) {
            System.err.println("El número de puerto está ocupado.");
            System.exit(1);
        }

        while (true) {
            System.out.println("Esperando a recibir conexión.");
            Socket socketJugador = servidorSockets.accept();
            System.out.println("Nueva conexión recibida.");
            if (!campo.conectarSocketJugador(socketJugador)) {
                campo = new Campo(cantidadParcelasX, cantidadParcelasY, cantidadMinas, intervaloEntreExplosiones);
                campo.conectarSocketJugador(socketJugador);
                System.out.println("Partida número: " + ++numeroPartidasTotales + " creada.");
            }
        }
    }
}
