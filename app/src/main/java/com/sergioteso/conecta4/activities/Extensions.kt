package com.sergioteso.conecta4.activities

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.sergioteso.conecta4.R
import com.sergioteso.conecta4.models.Round
import com.sergioteso.conecta4.models.RoundRepository
import com.sergioteso.conecta4.models.TableroC4
import kotlinx.android.synthetic.main.fragment_round_list.*
import kotlinx.android.synthetic.main.fragment_round_list.view.*

/**
 * Funcion que extiende la funcionalidad de un ImageButton permitiendo establecer
 * su background segun el valor de un player del tablero.
 */
fun ImageButton.update(player: Int) {
    when (player) {
        TableroC4.CASILLA_J1 -> this.setBackgroundResource(R.drawable.casilla_verde)
        TableroC4.CASILLA_J2 -> this.setBackgroundResource(R.drawable.casilla_roja)
        TableroC4.CASILLA_WIN_J1 -> this.setBackgroundResource(R.drawable.casilla_win_verde)
        TableroC4.CASILLA_WIN_J2 -> this.setBackgroundResource(R.drawable.casilla_win_roja)
        else -> this.setBackgroundResource(R.drawable.casilla_vacia)
    }
}

/**
 * Funcion que extiende la funcionalidad de Paint estableciendo el color a emplear segun
 * su valor en el Tablero.
 */
fun Paint.setColor(board: TableroC4, i: Int, j: Int, context: Context) {
    val player = board.getTablero(i, j)
    when (player) {
        TableroC4.CASILLA_J1 -> setColor(ContextCompat.getColor(context, R.color.darkGreen))
        TableroC4.CASILLA_J2 -> setColor(ContextCompat.getColor(context, R.color.darkRed))
        TableroC4.CASILLA_WIN_J1 -> setColor(ContextCompat.getColor(context, R.color.lightGreen))
        TableroC4.CASILLA_WIN_J2 -> setColor(ContextCompat.getColor(context, R.color.lightRed))
        else -> setColor(Color.GRAY)
    }
}

/**
 * Funcion que extiende la funcionalidad de RecyclerView Actualizando su adaptador si es null pasandole la lista de rondas
 * y un listener con una ronda como parametro.
 */
fun RecyclerView.update(onClickListener: (Round) -> Unit) {
    if (adapter == null)
        adapter = RoundAdapter(RoundRepository.rounds, onClickListener)
    adapter?.notifyDataSetChanged()
}

/**
 * Objeto empleado a modo de estatico para emitir mensajes en el log de Android principalmente a mode debug
 */
object Logger {
    private var numero: Int = 0
    fun log(text: String) {
        Log.d(
            "LifeCycleTest", Integer.toString(numero) + " : "
                    + text
        )
        numero++
    }
}