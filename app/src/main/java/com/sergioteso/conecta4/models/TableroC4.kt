package com.sergioteso.conecta4.models

import android.widget.ImageButton
import es.uam.eps.multij.ExcepcionJuego
import es.uam.eps.multij.Movimiento
import es.uam.eps.multij.Tablero
import java.util.ArrayList

/**
 * Clase que modeliza un tablero de nuestro juego extendiendo la clase Tablero. Esta
 * nos dara información de si los movimientos son validos, realizará los movimientos
 * y permitira tanto generar un String de su estado, como de reproducir ese estado
 * una vez leido un string generado con el metodo anterior
 *
 * Propiedades heredadas
 *
 * @property numJugadores Int con el número de jugadores
 * @property turno Int que indica el id del jugador que le toca mover
 * @property estado Constante con el estado del tablero solo tres valores posibles:
 *                  EN_CURSO, FINALIZADA, TABLAS
 * @property numJugadas Int con el número de jugadas realizadas
 * @property ultimoMovimiento Movimiento que contiene el último movimiento realizado
 *
 * Propiedades propias
 *
 * @property filas Int con el numero de filas del tablero
 * @property columnas Int con el numero de columnasIds del Tablero
 * @property matriz Array bidimensional de Int donde se almacena la matriz de datos del tablero
 */
class TableroC4(var filas: Int, var columnas: Int) : Tablero() {
    val MIN_COLUMNAS = 4
    val MIN_FILAS = 4
    val MAX_COLUMNAS = 10
    val MAX_FILAS = 10
    var matriz =
        if (columnas < MIN_COLUMNAS || columnas > MAX_COLUMNAS || filas < MIN_FILAS || filas > MAX_FILAS)
            throw ExcepcionJuego("Tablero inicializado con parametros incorrectos")
        else
            Array(filas, { IntArray(columnas) })

    companion object {
        val CASILLA_J1 = 1
        val CASILLA_J2 = 2
        val CASILLA_WIN_J1 = 3
        val CASILLA_WIN_J2 = 4
    }

    constructor(filas: Int) : this(filas, columnas = filas)

    init {
        estado = 1
    }

    /**
     * funcion que devuelve el valor en la coordenada (i,j)
     *
     * @param i Int con la coordenada de la fila
     * @param j Int con la coordenada de la columna
     * @return Int con el valor de la matriz en la coordenada recibida
     */
    fun getTablero(i: Int, j: Int): Int {
        if (i < 0 || i >= MAX_FILAS || j < 0 || j >= MAX_COLUMNAS) throw ExcepcionJuego("Error getTablero")
        return matriz[i][j]
    }

    /**
     * Funcion que devuelve un String a partir de un Tablero para representar el estado de la partida
     *
     * @return String con toda la información del tablero en formato de impresion
     */
    override fun toString(): String {
        var string =
            "TableroC4\n---------------------------------\nDimensiones: ${filas}x${columnas}\nJugadores: $numJugadores Jugadas: $numJugadas\n" +
                    "Turno: $turno Estado: "
        when (getEstado()) {
            Tablero.EN_CURSO -> string += "EN CURSO"
            Tablero.FINALIZADA -> string += "FINALIZADA"
            Tablero.TABLAS -> string += "TABLAS"
            else -> string += "NO EMPEZADA"
        }
        string += "\n---------------------------------\n"
        string += tableroInString()
        string += "\n"
        for (i in 1..this.columnas) {
            string += "-"
        }
        string += "\n"
        for (i in 1..this.columnas) {
            string += "$i"
        }
        return string
    }

    /**
     * Funcion que devuelve la matriz del tablero unicamente como string
     *
     * @return String con la matriz del tablero
     */
    fun tableroInString(): String {
        var string = ""
        for (i in 0..this.filas - 1) {
            for (j in 0..columnas - 1) {
                string += matriz[i][j]
            }
            if (i != this.filas - 1) string += "\n"
        }
        return string
    }

    /**
     * función que indica si un movimiento es valido
     * En el caso del Conecta4 solo hace falta mirar si la primera fila de la
     * columna elegida en el movimiento esta vacia. En ese caso la jugada ya será válida
     * independientemente de cuantas casillas vacías tenga debajo
     *
     * @param m Movimiento movimiento a evaluar
     * @return Booleano que indica si el movimiento es válido o no
     */
    override fun esValido(m: Movimiento?): Boolean =
        if (m == null || m !is MovimientoC4) false
        else if (m.getColumna() < 0 || m.getColumna() >= columnas) false
        else (matriz[0][m.getColumna()] == 0)


    /**
     * funcion que devuelve un ArrayList con todos los movimientos validos
     *
     * @return Arraylist<Movimiento> que contiene todos los movimientos validos
     */
    override fun movimientosValidos(): ArrayList<Movimiento> {
        val array = ArrayList<Movimiento>()
        for (i in 0..columnas - 1)
            if (matriz[0][i] == 0)
                array.add(MovimientoC4(i))
        return array
    }

    /**
     * funcion que realiza el movimiento dado en el tablero
     *
     * @param m Movimiento a realizar
     */
    override fun mueve(m: Movimiento?) {
        if (!this.esValido(m) || m !is MovimientoC4) throw ExcepcionJuego("Movimiento nulo o no de tipo C4")
        for (i in filas - 1 downTo 0) {
            if (matriz[i][m.getColumna()] == 0) {
                matriz[i][m.getColumna()] = getTurno() + 1
                break
            }
        }
        ultimoMovimiento = m
        // comprobar si se ha ganado
        if (comprobacion(m)) estado = FINALIZADA
        // comrpobar si se ha acabado en tablas
        else if (comprobarTablas()) estado = TABLAS
        else cambiaTurno()
    }

    /*
    fun movimientoPrueba(m: Movimiento,player : Int){
        if (!this.esValido(m) || m !is MovimientoC4) throw ExcepcionJuego("Movimiento nulo o no de tipo C4")
        for (i in filas - 1 downTo 0) {
            if (matriz[i][m.getColumna()] == 0) {
                matriz[i][m.getColumna()] = player
                break
            }
        }
    }*/

    /**
     * Funcion que dado dos coordenadas del tablero nos dice si se ha ganado o no la partida
     * @param x Int Coordenada que representa la fila elegida
     * @param y Int Coordenada que representa la columna elegida
     * @return lista que contiene las coordenadas de las casillas que dan la victoria o null si no se ha ganado
     */
    fun comprobarIJ(x: Int, y: Int): MutableList<List<Int>>? {
        if (x < 0 || x >= filas || y < 0 || y >= columnas) throw ExcepcionJuego("Error comprobar")
        var cont = 1
        val id = matriz[x][y]
        val mapa = mutableListOf<List<Int>>()
        mapa.add(listOf(x, y))
        if (id == 0) return null

        // Arrays con la direcciones respectivas x,y para realizar la comprobacion
        val arrayX = arrayListOf(1, -1, 0, 0, 1, -1, 1, -1)
        val arrayY = arrayListOf(0, 0, 1, -1, 1, -1, -1, 1)
        var ind = 0
        var cx = x
        var cy = y


        while (ind < 8 && cont < 4) {
            cx += arrayX[ind]
            cy += arrayY[ind]
            // Si nos salimos del margen cambiamos la direccion
            if (cx < 0 || cx >= filas || cy < 0 || cy >= columnas) {
                ind += 1
                cx = x
                cy = y
                // si hemos mirado las dos direcciones escogemos otra orientacion reiniciando el cont
                if (ind % 2 == 0) {
                    cont = 1
                    mapa.clear()
                    mapa.add(listOf(x, y))
                }
            } else {
                // Si tenemos una casilla deseada aumentamos contador
                if (matriz[cx][cy] == id) {
                    cont += 1
                    mapa.add(listOf(cx, cy))
                }
                // Si no cambiamos dirección como hemos hecho previamente
                else {
                    ind += 1
                    cx = x
                    cy = y
                    if (ind % 2 == 0) {
                        cont = 1
                        mapa.clear()
                        mapa.add(listOf(x, y))
                    }
                }
            }
        }

        if (cont == 4) return mapa
        return null
    }

    /**
     * Funcion que dado un MovimientoC4 comprueba si se ha ganado o no la partida y actualiza el tablero para reflejar
     * las casillas que dan la victoria
     * @param m MovimientoC4 a comprobar
     */
    fun setComprobacionIJ(m: MovimientoC4) {
        var map: MutableList<List<Int>>? = null
        for (i in 0..filas - 1) {
            if (matriz[i][m.getColumna()] != 0) {
                map = comprobarIJ(i, m.getColumna())
                break
            }
        }
        if (map == null)
            return
        for (i in map) {
            matriz[i[0]][i[1]] = matriz[i[0]][i[1]] + 2
        }
    }

    /**
     * Funcion que dado un MovimientoC4 comprueba si se ha ganado o no la partida
     * @param m MovimientoC4 a comprobar
     * @return Boolean con true si se ha ganado la partida con ese movimiento o false si no
     */
    fun comprobacion(m: MovimientoC4): Boolean {
        for (i in 0..filas - 1) {
            if (matriz[i][m.getColumna()] != 0)
                return comprobarIJ(i, m.getColumna()) != null
        }
        return false
    }

    /**
     * Funcion que comprueba si se puede realizar algún movimiento
     * para saber si la partida queda en Tablas o no
     * @return Boolean con True si la partida esta en una situacion de tablas o false si no
     */
    fun comprobarTablas(): Boolean {
        return movimientosValidos().isEmpty()
    }

    /**
     * funcion que a partir de una cadena generada mediante el metodo tableroToString transforma
     * el estado del tablero actual al estado guardado en la cadena.
     *
     * @param cadena String generada previamente con el metodo tableroToString
     */
    override fun stringToTablero(cadena: String?) {
        val tokens = cadena?.split(",") ?: throw ExcepcionJuego("Cadena de stringToTablero es null")
        this.numJugadores =
            tokens[0].toIntOrNull() ?: throw ExcepcionJuego("numJugadores incorrecto en stringToTablero")
        this.turno = tokens[1].toIntOrNull() ?: throw ExcepcionJuego("turno incorrecto en stringToTablero")
        this.estado = tokens[2].toIntOrNull() ?: throw ExcepcionJuego("estado incorrecto en stringToTablero")
        this.numJugadas = tokens[3].toIntOrNull() ?: throw ExcepcionJuego("numJugadas incorrecto en stringToTablero")
        this.filas = tokens[4].toIntOrNull() ?: throw ExcepcionJuego("filas incorrecto en stringToTablero")
        if (this.filas < MIN_FILAS || this.filas > MAX_FILAS) throw ExcepcionJuego("filas excede limite en stringToTablero")
        this.columnas = tokens[5].toIntOrNull() ?: throw ExcepcionJuego("columnasIds incorrecto en stringToTablero")
        if (this.columnas < MIN_COLUMNAS || this.columnas > MAX_COLUMNAS) throw ExcepcionJuego("columnasIds excede limite en stringToTablero")
        val m = tokens[6].toIntOrNull() ?: throw ExcepcionJuego("Ultimo movimiento incorrecto en stringToTablero")
        this.ultimoMovimiento = if (m == -1) null
        else MovimientoC4(m)
        var i = 0
        var j = 0
        for (s in tokens[7]) {
            matriz[i][j] = s.toString().toIntOrNull() ?: throw ExcepcionJuego("Fallo al parsear numero de la matriz")
            j += 1
            if (j == columnas) {
                j = 0
                i += 1
            }
        }
    }

    /**
     * funcion que guarda el estado actual del tablero en un String para posteriormente recuperar
     * este estado mediante la función stringToTablero
     *
     * @return String que guarda el estado del tablero actual
     */
    override fun tableroToString(): String {
        if( numJugadores < 0 || numJugadas < 0 || filas <= 0 || columnas <= 0){
            throw ExcepcionJuego("Error en los parametros del tablero. No se puede pasar a String")
        }

        val m = getUltimoMovimiento()
        val mov: Int
        mov = if (m == null) -1
        else {
            m as MovimientoC4
            m.getColumna()
        }
        var string = "$numJugadores,$turno,$estado,$numJugadas,$filas,$columnas,$mov,"
        for (i in 0..filas - 1)
            for (j in 0..columnas - 1)
                string += matriz[i][j]
        return string
    }

    /**
     * Funcion que resetea el tablero poniendo la matriz a cero
     *
     * @return Boolean que indica si se realizo con éxito
     */
    override fun reset(): Boolean {
        for (i in 0..filas - 1)
            for (j in 0..columnas - 1)
                matriz[i][j] = 0
        return super.reset()
    }
}