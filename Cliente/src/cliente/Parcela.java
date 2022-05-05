package cliente;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Esta clase tiene funciones y variables para cargar en memoria los iconos
 * reescalados, tambien guarda la información del icono asignado a el JLable que
 * instancia esta clase y su funcionalidad cuando se hace algun click sobre el.
 */
public class Parcela extends JLabel {

    private static HashMap<String, Image> mapaImagenesCargadas = new HashMap<String, Image>();
    private static HashMap<String, ImageIcon> mapaIconosReescalados = new HashMap<String, ImageIcon>();
    private static int relacionAspecto = 24;
    private static int ancho = relacionAspecto;
    private static int alto = relacionAspecto;

    private String nombreImagen;

    public Parcela(Tablero tablero, int coordenadaX, int coordenadaY) {
        actualizar("C");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                tablero.enviarClickServidor(me.getButton() + "," + coordenadaX + "," + coordenadaY);
            }
        });
    }

    /**
     * Cambia el icono de este boton por el nombre de la imagen idicada en el
     * parametro, lo busca en memoria sino lo encuentra lo carga reescalandolo
     * al tamaño adecuado.
     *
     * @param nombreImagen nombre de la imagen para cargar como icono a este
     * JLabel.
     */
    public void actualizar(String nombreImagen) {
        this.nombreImagen = nombreImagen;
        if (!mapaImagenesCargadas.containsKey(nombreImagen)) {
            mapaImagenesCargadas.put(nombreImagen, Toolkit.getDefaultToolkit().createImage(Parcela.class.getResource("/imagenes/" + nombreImagen + ".png")));
            mapaIconosReescalados.put(nombreImagen, new ImageIcon(mapaImagenesCargadas.get(nombreImagen).getScaledInstance(ancho, alto, Image.SCALE_FAST)));
        }
        setIcon(mapaIconosReescalados.get(nombreImagen));
    }

    /**
     * Reescala todos los iconos cargados en memoria por nuevas imagen
     * reescalada.
     *
     * @param ancho el nuevo ancho para los iconos.
     * @param alto el nuevo alto para los iconos
     */
    public static void reescalar(int ancho, int alto) {
        Parcela.ancho = ancho > relacionAspecto ? ancho : relacionAspecto;
        Parcela.alto = alto > relacionAspecto ? alto : relacionAspecto;
        for (Entry<String, ImageIcon> iconoImagen : mapaIconosReescalados.entrySet()) {
            mapaIconosReescalados.replace(iconoImagen.getKey(), new ImageIcon(mapaImagenesCargadas.get(iconoImagen.getKey()).getScaledInstance(Parcela.ancho, Parcela.alto, Image.SCALE_FAST)));
        }
    }

    /**
     * Reasigna el objeto que se pinta como icono.
     */
    public void repintar() {
        setIcon(mapaIconosReescalados.get(nombreImagen));
    }
}
