package servidor;

/* Esta clase guarda toda la informacion que puede tener una parcela */
public class Parcela {

    private boolean esMina;
    private boolean estaDescubierta;
    private int cantidadMinasVecinas;
    private int claveBanderaColocada;
    private boolean esDescubrible;
    private final int coordenadaX;
    private final int coordenadaY;

    public Parcela(int coordenadaX, int coordenadaY) {
        esMina = false;
        estaDescubierta = false;
        cantidadMinasVecinas = 0;
        claveBanderaColocada = -1;
        esDescubrible = false;
        this.coordenadaX = coordenadaX;
        this.coordenadaY = coordenadaY;
    }

    public void aumentarCantidadMinasVecinas() {
        cantidadMinasVecinas++;
    }

    public int obtenerCantidadMinasVecinas() {
        return cantidadMinasVecinas;
    }

    public void descubrir() {
        estaDescubierta = true;
    }

    public boolean estaDescubierta() {
        return estaDescubierta;
    }

    public void colocarClaveBandera(int claveBandera) {
        this.claveBanderaColocada = claveBandera;
    }

    public int obtenerClaveBandera() {
        return claveBanderaColocada;
    }

    public void plantar() {
        esMina = true;
    }

    public boolean estaPlantada() {
        return esMina;
    }

    public void hacerDescubrible() {
        esDescubrible = true;
    }

    public boolean esDescubrible() {
        return esDescubrible;
    }

    public int obtenerCoordenadaX() {
        return coordenadaX;
    }

    public int obtenerCoordenadaY() {
        return coordenadaY;
    }

}
