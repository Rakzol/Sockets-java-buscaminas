package servidor;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Esta clase administra las parcelas y a los jugadores */
public class Campo implements IConectarSocketJugador {

    public static final IConectarSocketJugador campoNulo = new CampoNulo();

    private boolean estaIniciado;
    private boolean estaFinalizado;
    private int cantidadMinasDescubiertas;
    private int cantidadJugadoresVivos;

    private final ExecutorService hilosJugadores;
    private final HashMap<Integer, Jugador> mapaJugadoresConectados;
    private final String nombresJugadores[];

    private final int cantidadParcelasX;
    private final int cantidadParcelasY;
    private final int cantidadMinas;
    private final Parcela matrizParcelas[][];

    private final ArrayList<Parcela> listaParcelasExplotables;
    private final int intervaloEntreExplosiones;
    private final Random aleatorizador;
    private final Timer temporizador;
    private final TimerTask tareaTemporizador;

    public Campo(int cantidadParcelasX, int cantidadParcelasY, int cantidadMinas, int intervaloEntreExplosiones) {
        this.cantidadParcelasX = cantidadParcelasX;
        this.cantidadParcelasY = cantidadParcelasY;
        this.cantidadMinas = cantidadMinas;
        listaParcelasExplotables = new ArrayList<Parcela>();
        temporizador = new Timer();
        aleatorizador = new Random();
        this.intervaloEntreExplosiones = intervaloEntreExplosiones;
        estaIniciado = false;
        estaFinalizado = false;
        nombresJugadores = new String[]{"Rojo", "Verde", "Azul", "Morado"};
        cantidadMinasDescubiertas = 0;
        mapaJugadoresConectados = new HashMap<Integer, Jugador>();
        hilosJugadores = Executors.newFixedThreadPool(nombresJugadores.length);

        tareaTemporizador = new TimerTask() {
            @Override
            public void run() {
                explotarParcelaRandom();
            }
        };

        /* Inicializamos las parcelas */
        matrizParcelas = new Parcela[cantidadParcelasX][cantidadParcelasY];
        for (int coordenadaParcelaY = 0; coordenadaParcelaY < cantidadParcelasY; coordenadaParcelaY++) {
            for (int coordenadaParcelaX = 0; coordenadaParcelaX < cantidadParcelasX; coordenadaParcelaX++) {
                matrizParcelas[coordenadaParcelaX][coordenadaParcelaY] = new Parcela(coordenadaParcelaX, coordenadaParcelaY);
            }
        }
        /* Plantamos las minas en las parcelas */
        int cantidadMinasPlantadas = 0;
        while (cantidadMinasPlantadas < cantidadMinas) {
            int coordenadaParcelaPlantarX = aleatorizador.nextInt(cantidadParcelasX);
            int coordenadaParcelaPlantarY = aleatorizador.nextInt(cantidadParcelasY);
            if (!matrizParcelas[coordenadaParcelaPlantarX][coordenadaParcelaPlantarY].estaPlantada()) {
                matrizParcelas[coordenadaParcelaPlantarX][coordenadaParcelaPlantarY].plantar();
                listaParcelasExplotables.add(matrizParcelas[coordenadaParcelaPlantarX][coordenadaParcelaPlantarY]);
                cantidadMinasPlantadas++;
                /*Le aumentamos el numero de minas que tienen alrededor a las parcelas que esten alrededor de esta*/
                for (int coordenadaParcelaVecinaY = coordenadaParcelaPlantarY - 1; coordenadaParcelaVecinaY < coordenadaParcelaPlantarY + 2; coordenadaParcelaVecinaY++) {
                    for (int coordenadaParcelaVecinaX = coordenadaParcelaPlantarX - 1; coordenadaParcelaVecinaX < coordenadaParcelaPlantarX + 2; coordenadaParcelaVecinaX++) {
                        if (coordenadaParcelaVecinaX > -1 && coordenadaParcelaVecinaX < cantidadParcelasX && coordenadaParcelaVecinaY > -1 && coordenadaParcelaVecinaY < cantidadParcelasY && !(coordenadaParcelaVecinaX == coordenadaParcelaPlantarX && coordenadaParcelaVecinaY == coordenadaParcelaPlantarY)) {
                            matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].aumentarCantidadMinasVecinas();
                        }
                    }
                }
            }
        }
    }

    /**
     * Intenta iniciar la partida.
     *
     * @param forzarInicioPartida se usa verdadero para intentar iniciar la
     * partida con un minimo de 2 jugadores conectados.
     */
    private void iniciarPartida(boolean forzarInicioPartida) {
        if (((forzarInicioPartida && mapaJugadoresConectados.size() > 1) || (mapaJugadoresConectados.size() == nombresJugadores.length)) && !estaIniciado) {
            estaIniciado = true;
            cantidadJugadoresVivos = mapaJugadoresConectados.size();
            enviarActualizacionTableroJugadores(",,,,1");
            for (int clave : mapaJugadoresConectados.keySet()) {
                enviarActualizacionTableroJugadores(",," + clave + ":Buscando minas:" + ",,");
            }
            if (intervaloEntreExplosiones > 0) {
                temporizador.scheduleAtFixedRate(tareaTemporizador, intervaloEntreExplosiones, intervaloEntreExplosiones);
            }
        }
    }

    /**
     * Intenta finalizar la partida.
     */
    private void finalizarPartida() {
        if (cantidadMinasDescubiertas == cantidadMinas || cantidadJugadoresVivos == 0 || mapaJugadoresConectados.size() == 1) {
            System.out.print("Patida finalizada con los resultados:\n"
                    + "Minas Descubiertas: " + cantidadMinasDescubiertas + "\n"
                    + "Jugadores Vivos: " + cantidadJugadoresVivos + "\n"
                    + "Jugadores Conectados: " + mapaJugadoresConectados.size() + "\n");
            estaFinalizado = true;
            Jugador arregloJugadoresOrdenados[] = new Jugador[mapaJugadoresConectados.size()];
            int indiceJugador = 0;
            for (Jugador jugador : mapaJugadoresConectados.values()) {
                arregloJugadoresOrdenados[indiceJugador] = jugador;
                indiceJugador++;
            }
            Arrays.sort(arregloJugadoresOrdenados);
            int lugarPuntacionMasBaja = 1;
            int puntuacionMasBaja = arregloJugadoresOrdenados[0].obtenerMinasDescubiertas();
            for (Jugador jugador : arregloJugadoresOrdenados) {
                if (jugador.obtenerMinasDescubiertas() < puntuacionMasBaja) {
                    lugarPuntacionMasBaja++;
                    puntuacionMasBaja = jugador.obtenerMinasDescubiertas();
                }
                enviarActualizacionTableroJugadores(",," + jugador.obtenerClave() + ":" + lugarPuntacionMasBaja + " lugar:" + jugador.obtenerMinasDescubiertas() + ",,");
            }
            for (Jugador jugador : arregloJugadoresOrdenados) {
                desconectarJugador(jugador);
            }
            hilosJugadores.shutdown();
            temporizador.cancel();
            temporizador.purge();
        }
    }

    /**
     * Cambia el estado de las parcelas y envia las actualizaciones a todos los
     * clientes, si es un click valido.
     *
     * @param jugadorResponsableClick El jugador que realizo el click.
     * @param codigoBotonClickeado el codigo del boton con el que se realizo el
     * click.
     * @param coordenadaParcelaClickeadaX coordenada en X de la parcela
     * clieckeada.
     * @param coordenadaParcelaClickeadaY coordenada en Y de la parcela
     * clieckeada.
     */
    synchronized public void recibirClick(Jugador jugadorResponsableClick, int codigoBotonClickeado, int coordenadaParcelaClickeadaX, int coordenadaParcelaClickeadaY) {
        /* Si se realizo un click en inicar partida */
        if (codigoBotonClickeado == 0) {
            iniciarPartida(true);
            /* Si se realizo otro tipo de click sobre alguna parcela sin descubrir, la partida esta iniciada sin finalizar y el jugador sigue vivo */
        } else if (estaIniciado && !estaFinalizado && !matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].estaDescubierta() && jugadorResponsableClick.estaVivo()) {
            /* Se validara el acceso a realizar el click si es su primer turno o no */
            if (jugadorResponsableClick.esPrimerTurno()) {
                /* Se comprueba si hay contorno clickeable disponible o si se clickeo contorno */
                if (!hayContornoDescubrible() || (codigoBotonClickeado == 1 && (((coordenadaParcelaClickeadaX == 0 || coordenadaParcelaClickeadaX == cantidadParcelasX - 1) && (coordenadaParcelaClickeadaY >= 0 && coordenadaParcelaClickeadaY < cantidadParcelasY)) || ((coordenadaParcelaClickeadaY == 0 || coordenadaParcelaClickeadaY == cantidadParcelasY - 1) && (coordenadaParcelaClickeadaX >= 0 && coordenadaParcelaClickeadaX < cantidadParcelasX))))) {
                    jugadorResponsableClick.finPrimerTurno();
                } else {
                    return;
                }
            }
            /* Si se realizo un click izquierdo sobre una parcela sin bandera */
            if (codigoBotonClickeado == 1 && matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].obtenerClaveBandera() == -1) {
                /* Si la parcela esta plantada */
                if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].estaPlantada()) {
                    jugadorResponsableClick.matar();
                    cantidadJugadoresVivos--;
                    matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].descubrir();
                    cantidadMinasDescubiertas++;
                    listaParcelasExplotables.remove(matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY]);
                    enviarActualizacionTableroJugadores(",," + jugadorResponsableClick.obtenerClave() + ":Muerto:," + coordenadaParcelaClickeadaX + ":" + coordenadaParcelaClickeadaY + ":" + jugadorResponsableClick.obtenerClave() + "M,");
                    finalizarPartida();
                    /* Si la parcela no esta plantada */
                } else {
                    descubrirParcela(jugadorResponsableClick.obtenerClave(), coordenadaParcelaClickeadaX, coordenadaParcelaClickeadaY);
                }
                /* Si se realizo un click derecho sobre una parcela */
            } else if (codigoBotonClickeado == 3) {
                /* Si la parcela no tiene bandera */
                if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].obtenerClaveBandera() == -1 && jugadorResponsableClick.obtenerBanderasUtilizables() > 0) {
                    matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].colocarClaveBandera(jugadorResponsableClick.obtenerClave());
                    jugadorResponsableClick.disminuirBanderasUtilizables();
                    enviarActualizacionTableroJugadores(",," + jugadorResponsableClick.obtenerClave() + "::" + jugadorResponsableClick.obtenerBanderasUtilizables() + "," + coordenadaParcelaClickeadaX + ":" + coordenadaParcelaClickeadaY + ":" + jugadorResponsableClick.obtenerClave() + "B,");
                    if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].estaPlantada()) {
                        cantidadMinasDescubiertas++;
                        listaParcelasExplotables.remove(matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY]);
                        jugadorResponsableClick.aumentarMinasDescubiertas();
                        finalizarPartida();
                    }
                    /* Si la parcela tiene bandera */
                } else if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].obtenerClaveBandera() == jugadorResponsableClick.obtenerClave()) {
                    matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].colocarClaveBandera(-1);
                    jugadorResponsableClick.aumentarBanderasUtilizables();
                    if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].estaPlantada()) {
                        cantidadMinasDescubiertas--;
                        listaParcelasExplotables.add(matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY]);
                        jugadorResponsableClick.disminuirMinasDescubiertas();
                    }
                    if (matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].esDescubrible()) {
                        matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].descubrir();
                        enviarActualizacionTableroJugadores(",," + jugadorResponsableClick.obtenerClave() + "::" + jugadorResponsableClick.obtenerBanderasUtilizables() + "," + coordenadaParcelaClickeadaX + ":" + coordenadaParcelaClickeadaY + ":" + jugadorResponsableClick.obtenerClave() + matrizParcelas[coordenadaParcelaClickeadaX][coordenadaParcelaClickeadaY].obtenerCantidadMinasVecinas() + ",");
                    } else {
                        enviarActualizacionTableroJugadores(",," + jugadorResponsableClick.obtenerClave() + "::" + jugadorResponsableClick.obtenerBanderasUtilizables() + "," + coordenadaParcelaClickeadaX + ":" + coordenadaParcelaClickeadaY + ":C,");
                    }
                }
            }
        }
    }

    /**
     * Se descubre una parcela con la clave del jugador y las coordenadas
     * indicadas y se aplica a las parcelas vecinas enviando una actualización
     * de los cambios a todos los jugadores.
     *
     * @param claveJugador la clave del jugador que esta descubriendo la
     * parcela.
     * @param coordenadaParcelaX la coordenada en X de la parcela que se esta
     * descubriendo.
     * @param coordenadaParcelaY la coordenada en X de la parcela que se esta
     * descubriendo.
     */
    private void descubrirParcela(int claveJugador, int coordenadaParcelaX, int coordenadaParcelaY) {
        matrizParcelas[coordenadaParcelaX][coordenadaParcelaY].descubrir();
        enviarActualizacionTableroJugadores(",,," + coordenadaParcelaX + ":" + coordenadaParcelaY + ":" + claveJugador + matrizParcelas[coordenadaParcelaX][coordenadaParcelaY].obtenerCantidadMinasVecinas() + ",");
        if (matrizParcelas[coordenadaParcelaX][coordenadaParcelaY].obtenerCantidadMinasVecinas() == 0) {
            for (int coordenadaParcelaVecinaY = coordenadaParcelaY - 1; coordenadaParcelaVecinaY < coordenadaParcelaY + 2; coordenadaParcelaVecinaY++) {
                for (int coordenadaParcelaVecinaX = coordenadaParcelaX - 1; coordenadaParcelaVecinaX < coordenadaParcelaX + 2; coordenadaParcelaVecinaX++) {
                    if (coordenadaParcelaVecinaX > -1 && coordenadaParcelaVecinaX < cantidadParcelasX && coordenadaParcelaVecinaY > -1 && coordenadaParcelaVecinaY < cantidadParcelasY) {
                        if (!matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].estaDescubierta()) {
                            if (matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].obtenerClaveBandera() == -1) {
                                descubrirParcela(claveJugador, coordenadaParcelaVecinaX, coordenadaParcelaVecinaY);
                            } else if (!matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].esDescubrible()) {
                                matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].hacerDescubrible();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Intenta agregar un nuevo jugador al campo, si se cumplen la scondiciones.
     */
    @Override
    synchronized public boolean conectarSocketJugador(Socket socketJugador) {
        if (estaIniciado) {
            return false;
        }
        int claveJugador = 0;
        while (mapaJugadoresConectados.containsKey(claveJugador)) {
            claveJugador++;
        }
        Jugador jugador = new Jugador(socketJugador, this, claveJugador, cantidadMinas);
        jugador.actualizar(cantidadParcelasX + ":" + cantidadParcelasY + "," + nombresJugadores[claveJugador] + ",,,");
        for (int claveJugadores : mapaJugadoresConectados.keySet()) {
            jugador.actualizar(",," + claveJugadores + ":Esperando:" + cantidadMinas + ",,");
        }
        mapaJugadoresConectados.put(claveJugador, jugador);
        enviarActualizacionTableroJugadores(",," + claveJugador + ":Esperando:" + cantidadMinas + ",,");
        hilosJugadores.execute(mapaJugadoresConectados.get(claveJugador));
        iniciarPartida(false);
        return true;
    }

    /**
     * Desconecta un jugador del campo y cambia es estado de la partida.
     */
    synchronized public void desconectarJugador(Jugador jugadorDesconectar) {
        if (mapaJugadoresConectados.containsKey(jugadorDesconectar.obtenerClave())) {
            mapaJugadoresConectados.remove(jugadorDesconectar.obtenerClave());
            jugadorDesconectar.actualizar(",,,,0");
            jugadorDesconectar.cerrarCanalesComunicacion();
            if (!estaFinalizado) {
                enviarActualizacionTableroJugadores(",," + jugadorDesconectar.obtenerClave() + ":Desconectado:" + ",,");
            }
            if (estaIniciado && !estaFinalizado) {
                if (jugadorDesconectar.estaVivo()) {
                    cantidadJugadoresVivos--;
                }
                /* Obtenemos una actualizacion con las banderas del cliente quitadas */
                for (int coordenadaParcelY = 0; coordenadaParcelY < cantidadParcelasY; coordenadaParcelY++) {
                    for (int coordenadaParcelaX = 0; coordenadaParcelaX < cantidadParcelasX; coordenadaParcelaX++) {
                        if (matrizParcelas[coordenadaParcelaX][coordenadaParcelY].obtenerClaveBandera() == jugadorDesconectar.obtenerClave()) {
                            matrizParcelas[coordenadaParcelaX][coordenadaParcelY].colocarClaveBandera(-1);
                            if (matrizParcelas[coordenadaParcelaX][coordenadaParcelY].esDescubrible()) {
                                matrizParcelas[coordenadaParcelaX][coordenadaParcelY].descubrir();
                                enviarActualizacionTableroJugadores(",,," + coordenadaParcelaX + ":" + coordenadaParcelY + ":" + jugadorDesconectar.obtenerClave() + matrizParcelas[coordenadaParcelaX][coordenadaParcelY].obtenerCantidadMinasVecinas() + ",");
                            } else {
                                if (matrizParcelas[coordenadaParcelaX][coordenadaParcelY].estaPlantada()) {
                                    cantidadMinasDescubiertas--;
                                    listaParcelasExplotables.add(matrizParcelas[coordenadaParcelaX][coordenadaParcelY]);
                                }
                                enviarActualizacionTableroJugadores(",,," + coordenadaParcelaX + ":" + coordenadaParcelY + ":C" + ",");
                            }
                        }
                    }
                }
                finalizarPartida();
            }
        }
    }

    /**
     * Envia un mensaje a todos los jugadores.
     */
    private void enviarActualizacionTableroJugadores(String actualizacion) {
        for (Jugador jugador : mapaJugadoresConectados.values()) {
            jugador.actualizar(actualizacion);
        }
    }

    /**
     * Regresa verdadero si hay contorno cubierto y ademas que no es mina ni
     * tiene bandera.
     */
    private boolean hayContornoDescubrible() {
        for (int x = 0; x < cantidadParcelasX; x++) {
            if ((!matrizParcelas[x][0].estaDescubierta() && !matrizParcelas[x][0].estaPlantada() && matrizParcelas[x][0].obtenerClaveBandera() == -1) || (!matrizParcelas[x][cantidadParcelasY - 1].estaDescubierta() && !matrizParcelas[x][cantidadParcelasY - 1].estaPlantada() && matrizParcelas[x][cantidadParcelasY - 1].obtenerClaveBandera() == -1)) {
                return true;
            }
        }
        for (int y = 0; y < cantidadParcelasY; y++) {
            if ((!matrizParcelas[0][y].estaDescubierta() && !matrizParcelas[0][y].estaPlantada() && matrizParcelas[0][y].obtenerClaveBandera() == -1) || (!matrizParcelas[cantidadParcelasX - 1][y].estaDescubierta() && !matrizParcelas[cantidadParcelasX - 1][y].estaPlantada() && matrizParcelas[cantidadParcelasX - 1][y].obtenerClaveBandera() == -1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Esta funcion se utiliza para explotar una mina aleatoria.
     */
    synchronized private void explotarParcelaRandom() {
        if (listaParcelasExplotables.isEmpty()) {
            return;
        }
        descubrirParcelaExplotable(listaParcelasExplotables.get(aleatorizador.nextInt(listaParcelasExplotables.size())));
        finalizarPartida();
    }

    /**
     * Se descubre una mina explotable y se aplica a las parcelas vecinas
     * enviando una actualización de los cambios a todos los jugadores.
     *
     * @param parcelaExplotable la parcela a explotar.
     */
    private void descubrirParcelaExplotable(Parcela parcelaExplotable) {
        parcelaExplotable.descubrir();
        cantidadMinasDescubiertas++;
        listaParcelasExplotables.remove(parcelaExplotable);
        enviarActualizacionTableroJugadores(",,," + parcelaExplotable.obtenerCoordenadaX() + ":" + parcelaExplotable.obtenerCoordenadaY() + ":M,");
        for (int coordenadaParcelaVecinaY = parcelaExplotable.obtenerCoordenadaY() - 1; coordenadaParcelaVecinaY < parcelaExplotable.obtenerCoordenadaY() + 2; coordenadaParcelaVecinaY++) {
            for (int coordenadaParcelaVecinaX = parcelaExplotable.obtenerCoordenadaX() - 1; coordenadaParcelaVecinaX < parcelaExplotable.obtenerCoordenadaX() + 2; coordenadaParcelaVecinaX++) {
                if (coordenadaParcelaVecinaX > -1 && coordenadaParcelaVecinaX < cantidadParcelasX && coordenadaParcelaVecinaY > -1 && coordenadaParcelaVecinaY < cantidadParcelasY) {
                    if (!matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].estaDescubierta()) {
                        if (matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].estaPlantada() && matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].obtenerClaveBandera() == -1) {
                            descubrirParcelaExplotable(matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY]);
                        } else if (!matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].estaPlantada()) {
                            if (matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].obtenerClaveBandera() == -1) {
                                matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].descubrir();
                                enviarActualizacionTableroJugadores(",,," + coordenadaParcelaVecinaX + ":" + coordenadaParcelaVecinaY + ":" + matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].obtenerCantidadMinasVecinas() + ",");
                            } else if (!matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].esDescubrible()) {
                                matrizParcelas[coordenadaParcelaVecinaX][coordenadaParcelaVecinaY].hacerDescubrible();
                            }
                        }
                    }
                }
            }
        }
    }

    public static class CampoNulo implements IConectarSocketJugador {

        @Override
        public boolean conectarSocketJugador(Socket socket) {
            return false;
        }
    }

}

interface IConectarSocketJugador {

    boolean conectarSocketJugador(Socket socket);
}
