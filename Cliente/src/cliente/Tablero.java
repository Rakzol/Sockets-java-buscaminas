package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Esta clase maneja los flujos de entrada y salida con el servidor para enviar
 * y recibir actualizaciones al apartado gráfico del marcador y el campo.
 */
public class Tablero extends JFrame {

    private Socket socket;
    private DataInputStream flujoEntrada;
    private DataOutputStream flujoSalida;

    private Marcador marcador;
    private Campo campo;

    public Tablero() {
        super("Conectando");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                desconectarCanalComunicacion();
                System.exit(0);
            }
        });

        marcador = new Marcador(this, "Arial", 20, 0, new Color[]{new Color(244, 67, 54), new Color(76, 175, 80), new Color(33, 150, 243), new Color(186, 104, 200)});
        add(marcador, BorderLayout.WEST);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        conectarConServidor();
        run();
    }

    private void run() {
        while (true) {
            try {
                /* El protocolo consta de 5 secciones las cuales pueden contener informacion de actualizacion o no */
                String actualizacion[] = flujoEntrada.readUTF().split(",", -1);
                /* Actualización para generar el campo */
                if (actualizacion[0].length() > 0) {
                    String cantidadParcelasXY[] = actualizacion[0].split(":", -1);
                    campo = new Campo(this, Integer.valueOf(cantidadParcelasXY[0]), Integer.valueOf(cantidadParcelasXY[1]), Color.darkGray, 2);
                    addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            campo.reescalar();
                        }
                    });
                    add(campo, BorderLayout.CENTER);
                    pack();
                    setLocationRelativeTo(null);
                }
                /* Actualización para cambiar el titulo de la ventana */
                if (actualizacion[1].length() > 0) {
                    setTitle(actualizacion[1]);
                    setIconImage(Toolkit.getDefaultToolkit().createImage(Tablero.class.getResource("/imagenes/" + actualizacion[1] + ".png")));
                }
                /* Actualización de los estados de los marcadores de los jugadores */
                if (actualizacion[2].length() > 0) {
                    marcador.actualizar(actualizacion[2]);
                }
                /* Actulización de los iconos de las parcelas en el campo */
                if (actualizacion[3].length() > 0) {
                    campo.actualizar(actualizacion[3]);
                }
                /* Actualización del botón de comienzo de partida */
                if (actualizacion[4].length() > 0) {
                    if (actualizacion[4].equals("0")) {
                        marcador.finalizado();
                        desconectarCanalComunicacion();
                        JOptionPane.showMessageDialog(this, "Partida finalizada.", "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    } else {
                        marcador.comenzado();
                    }
                }
            } catch (Exception ex) {
                desconectarCanalComunicacion();
                JOptionPane.showMessageDialog(this, "Se perdio la conexion con el servidór.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

        }
    }

    synchronized public void enviarClickServidor(String click) {
        try {
            flujoSalida.writeUTF(click);
        } catch (Exception ex) {
        }
    }

    private void desconectarCanalComunicacion() {
        try {
            socket.close();
        } catch (Exception ex) {
        }
    }

    private void conectarConServidor() {
        JTextField direccion = new JTextField("127.0.0.1");
        JTextField puerto = new JTextField("55555");
        while (true) {
            if (JOptionPane.showConfirmDialog(this, new Object[]{"Dirección", direccion, "Puerto", puerto}, "Conectar", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    socket = new Socket(direccion.getText(), Integer.valueOf(puerto.getText()));
                    flujoEntrada = new DataInputStream(socket.getInputStream());
                    flujoSalida = new DataOutputStream(socket.getOutputStream());
                    break;
                } catch (UnknownHostException e) {
                    JOptionPane.showMessageDialog(this, "Dirección Invalida.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (ConnectException e) {
                    JOptionPane.showMessageDialog(this, "Tiempo para la conexión agotado o conexión rechazada.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "El puerto tiene que ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "El puerto tiene que estar en el rango de 0 a 65535.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "No se puede conectar con el servidor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                desconectarCanalComunicacion();
                System.exit(0);
            }
        }
    }
}
