package cliente;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Esta clase se utiliza para administrar las actualizaciones de la interfaze de
 * los marcadores de los jugadores.
 */
public class Marcador extends JScrollPane {

    private JLabel etiquetasJugadores[][];
    private JPanel panel;
    private JButton botonComenzar;

    public Marcador(Tablero tablero, String fuente, int tamanioFuente, int estiloFuente, Color coloresJugadores[]) {

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        etiquetasJugadores = new JLabel[4][2];

        for (int c = 0; c < 4; c++) {
            etiquetasJugadores[c][0] = new JLabel("Desconectado");
            etiquetasJugadores[c][0].setFont(new Font(fuente, estiloFuente, tamanioFuente));
            etiquetasJugadores[c][0].setForeground(coloresJugadores[c]);
            etiquetasJugadores[c][0].setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(etiquetasJugadores[c][0]);

            etiquetasJugadores[c][1] = new JLabel("0");
            etiquetasJugadores[c][1].setFont(new Font(fuente, estiloFuente, tamanioFuente));
            etiquetasJugadores[c][1].setForeground(coloresJugadores[c]);
            etiquetasJugadores[c][1].setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(etiquetasJugadores[c][1]);
        }
        botonComenzar = new JButton("Comenzar");
        botonComenzar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tablero.enviarClickServidor("0,0,0");
            }
        });
        botonComenzar.setFocusPainted(false);
        botonComenzar.setFont(new Font(fuente, estiloFuente, tamanioFuente));
        botonComenzar.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(botonComenzar);

        panel.setPreferredSize(new Dimension(tamanioFuente * 9, tamanioFuente * 12));

        setViewportView(panel);
    }

    /**
     * Actualiza el texto de las etiquetas de jugadores haciendo split a un
     * string que contiene el protocolo de aztualización.
     *
     * @param etiquetasJugadores String con el protocolo de actualizacion
     * Ejemplo: 0:Vivo:69-3:Muerto:90 Son 3 partes del protocolo separadas por
     * dos puntos clave de jugador, estaod, numero de banderas.
     */
    public void actualizar(String etiquetasJugadores) {
        for (String etiquetaJugadorColapsada : etiquetasJugadores.split("-", -1)) {
            String etiquetaJugador[] = etiquetaJugadorColapsada.split(":", -1);
            if (etiquetaJugador[1].length() > 0) {
                this.etiquetasJugadores[Integer.valueOf(etiquetaJugador[0])][0].setText(etiquetaJugador[1]);
            }
            if (etiquetaJugador[2].length() > 0) {
                this.etiquetasJugadores[Integer.valueOf(etiquetaJugador[0])][1].setText(etiquetaJugador[2]);
            }
        }
        /* Para recalcular el tamaño correcto del scroll */
        setViewportView(panel);
    }

    public void comenzado() {
        botonComenzar.setText("!Comenzado¡");
        botonComenzar.setEnabled(false);
    }

    public void finalizado() {
        botonComenzar.setText("Finalizado");
    }
}
