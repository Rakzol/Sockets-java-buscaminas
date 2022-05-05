package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/* Esta clase mantiene la comunicacion con un tablero y guarda los datos de un jugador */
public class Jugador implements Runnable, Comparable<Jugador> {

    private Socket socket;
    private DataInputStream flujoEntrada;
    private DataOutputStream flujoSalida;
    private Campo campo;
    private int clave;
    private int banderasUtilizables;
    private boolean estaVivo;
    private int minasDescubiertas;
    private boolean esPrimerTurno;

    public Jugador(Socket socket, Campo campo, int clave, int banderasUtilizables) {
        this.socket = socket;
        this.campo = campo;
        this.clave = clave;
        this.banderasUtilizables = banderasUtilizables;
        estaVivo = true;
        minasDescubiertas = 0;
        esPrimerTurno = true;
        try {
            flujoEntrada = new DataInputStream(socket.getInputStream());
            flujoSalida = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
        }
    }

    @Override
    public void run() {
        String click[];
        while (true) {
            try {
                click = flujoEntrada.readUTF().split(",");
            } catch (Exception ex) {
                campo.desconectarJugador(this);
                return;
            }
            campo.recibirClick(this, Integer.valueOf(click[0]), Integer.valueOf(click[1]), Integer.valueOf(click[2]));
        }
    }

    public void actualizar(String actualizacion) {
        try {
            flujoSalida.writeUTF(actualizacion);
        } catch (Exception ex) {
        }
    }

    public void cerrarCanalesComunicacion() {
        try {
            socket.close();
            flujoEntrada.close();
            flujoSalida.close();
        } catch (Exception ex) {
        }
    }

    public void aumentarBanderasUtilizables() {
        banderasUtilizables++;
    }

    public void disminuirBanderasUtilizables() {
        banderasUtilizables--;
    }

    public int obtenerBanderasUtilizables() {
        return banderasUtilizables;
    }

    public void aumentarMinasDescubiertas() {
        minasDescubiertas++;
    }

    public void disminuirMinasDescubiertas() {
        minasDescubiertas--;
    }

    public int obtenerMinasDescubiertas() {
        return minasDescubiertas;
    }

    public int obtenerClave() {
        return clave;
    }

    public void matar() {
        estaVivo = false;
    }

    public boolean estaVivo() {
        return estaVivo;
    }

    public void finPrimerTurno() {
        esPrimerTurno = false;
    }

    public boolean esPrimerTurno() {
        return esPrimerTurno;
    }

    @Override
    public int compareTo(Jugador jugador) {
        return jugador.obtenerMinasDescubiertas() - minasDescubiertas;
    }

}
