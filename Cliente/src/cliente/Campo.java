package cliente;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Esta clase se encarga de generar las parcelas y comunicarlas con el servidor
 * para que envien y reciben actualizaciones.
 */
public class Campo extends JScrollPane {

    private Parcela matrizParcelas[][];
    private int separacionEntreParcelas;

    public Campo(Tablero tablero, int cantidadParcelasX, int cantidadParcelasY, Color colorFondo, int separacionEntreParcelas) {
        this.separacionEntreParcelas = separacionEntreParcelas;
        JPanel panel = new JPanel(new GridLayout(cantidadParcelasY, cantidadParcelasX, separacionEntreParcelas, separacionEntreParcelas));
        panel.setBackground(colorFondo);

        matrizParcelas = new Parcela[cantidadParcelasX][cantidadParcelasY];
        for (int coordenadaParcelaY = 0; coordenadaParcelaY < cantidadParcelasY; coordenadaParcelaY++) {
            for (int coordenadaParcelaX = 0; coordenadaParcelaX < cantidadParcelasX; coordenadaParcelaX++) {
                matrizParcelas[coordenadaParcelaX][coordenadaParcelaY] = new Parcela(tablero, coordenadaParcelaX, coordenadaParcelaY);
                panel.add(matrizParcelas[coordenadaParcelaX][coordenadaParcelaY]);
            }
        }

        setViewportView(panel);
    }

    /* Cambia los iconos de multiples parcelas */
    synchronized public void actualizar(String parcelas) {
        for (String parcelaColapsada : parcelas.split("-", -1)) {
            String parcela[] = parcelaColapsada.split(":", -1);
            matrizParcelas[Integer.valueOf(parcela[0])][Integer.valueOf(parcela[1])].actualizar(parcela[2]);
        }
    }

    /* Se reescalan todos los iconos */
    synchronized public void reescalar() {
        /* Se cambia el ancho y alto de todos los iconos cargados en memoria por uno nuevo que se adapte al tamaño de la ventana */
        Parcela.reescalar((getWidth() - ((matrizParcelas.length - 1) * separacionEntreParcelas)) / matrizParcelas.length, (getHeight() - ((matrizParcelas[0].length - 1) * separacionEntreParcelas)) / matrizParcelas[0].length);
        /* Se cargan de nuevo todos los iconos reescalados para cada parcela */
        for (Parcela[] parcelasX : matrizParcelas) {
            for (Parcela parcelaY : parcelasX) {
                parcelaY.repintar();
            }
        }
    }

}
