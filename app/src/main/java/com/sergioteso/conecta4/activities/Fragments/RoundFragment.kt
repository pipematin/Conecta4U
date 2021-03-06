package com.sergioteso.conecta4.activities.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.sergioteso.conecta4.R
import com.sergioteso.conecta4.firebase.FRDataBase
import com.sergioteso.conecta4.models.*
import es.uam.eps.multij.*
import kotlinx.android.synthetic.main.fragment_round.*
import java.lang.Exception


/**
 * Fragmento que modela una partida con su tablero y sus listeners oportunos.
 * Inicializa al jugador local y el tablero y muestra este ultimo por pantalla para
 * poder jugar.
 */
class RoundFragment : Fragment(), PartidaListener {
    private lateinit var round: Round
    private lateinit var name: String
    private lateinit var game: Partida
    private lateinit var tablero: TableroC4
    var listener: OnRoundFragmentInteractionListener? = null

    /**
     * interfaz que deben a implementar las clases que quieran ejecutar codigo cuando
     * ocurre una interaccion en la ronda de este fragmento. Principalmente actualizaciones de UI.
     */
    interface OnRoundFragmentInteractionListener {
        fun onRoundUpdated(round: Round)
    }

    /**
     * Metodo que ejecutado al crear la vista que se encarga de obtener la ronda de
     * RoundRepository en la cual se va a jugar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            arguments?.let {
                round = Round.fromJSONString(it.getString(ARG_ROUND)!!)
            }
        } catch (e: Exception) {
            Log.d("DEBUG", e.message)
            activity?.finish()
        }

    }

    /**
     * Metodo llamado al crear la vista en el cual se le indica el layout a inflar.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_round, container, false)
    }

    /**
     * Metodo llamado una vez la vista es creada. Es el encargado en asignar al jugador local y el tablero y
     * establecer el listener del boton de reset
     */
    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_title.text = round.title
        if (savedInstanceState != null) {
            round.board.stringToTablero(savedInstanceState.getString(BOARDSTRING))
        }
        tablero = round.board

        if (RoundRepositoryFactory.LOCAL) {
            reset_round_fab.setOnClickListener {
                if (tablero.estado != Tablero.EN_CURSO) {
                    Snackbar.make(
                        view,
                        R.string.round_already_finished, Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    tablero.reset()
                    startRound()
                    listener?.onRoundUpdated(round)
                    //updateUI()
                    board_viewc4.invalidate()
                    Snackbar.make(
                        view, R.string.round_restarted,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            reset_round_fab.visibility = View.GONE
        }

    }

    /**
     * Metodo que inicia una ronda. Para ello añade a la partida un jugador local y un jugador aleatorio y
     * la inicializa.
     */
    private fun startRound() {
        val players = ArrayList<Jugador>()
        name = round.secondPlayerName
        val local = RoundRepositoryFactory.LOCAL
        val localPlayer: Jugador
        val remotePlayer: Jugador
        if (local) {
            remotePlayer = JugadorAleatorio("Random Player")
            localPlayer = LocalPlayerC4(name, 0)
            players.add(localPlayer)
            players.add(remotePlayer)
        } else {
            val frDataBase = FRDataBase(context!!)

            if (frDataBase.checkPlayerPosition(round.firstPlayerName) == 1) {
                localPlayer = LocalPlayerC4(round.firstPlayerName, 0)
                remotePlayer = RemotePlayerC4(round.secondPlayerName, 1)
                players.add(localPlayer)
                players.add(remotePlayer)
            } else {
                localPlayer = LocalPlayerC4(round.firstPlayerName, 1)
                remotePlayer = RemotePlayerC4(round.secondPlayerName, 0)
                players.add(remotePlayer)
                players.add(localPlayer)
            }
        }
        game = Partida(tablero, players)
        game.addObservador(this)
        localPlayer.setPartida(game)
        board_viewc4.setBoard(tablero)
        board_viewc4.setOnPlayListener(localPlayer)
        if (!local) {
            val callback = object : RoundUICallback {
                override fun updateUI(round_updated: Round) {
                    try {
                        val remoteP = remotePlayer as RemotePlayerC4
                        if (remoteP.turno != round_updated.board.turno && remoteP.turno == game.tablero.turno && round_updated.board.estado != Tablero.FINALIZADA && board_viewc4 != null) {
                            game.realizaAccion(AccionMover(remotePlayer, round_updated.board.ultimoMovimiento))
                            listener?.onRoundUpdated(round_updated)
                        }

                    } catch (e: ExcepcionJuego) {
                        //Nada
                    }
                    if (board_viewc4 != null) {
                        board_viewc4.invalidate()
                    }
                }

                override fun onError() {
                    Toast.makeText(context, "Error on UpdateUIRound", Toast.LENGTH_SHORT).show()
                }
            }
            FRDataBase(context!!).startListeningBoardChanges(callback, round.id)
        }


        if (game.tablero.estado == Tablero.EN_CURSO)
            game.comenzar()
    }

    /**
     * Metodo que se ejecuta al empezar la aplicacion. Crea el tablero e inicia la ronda.
     */
    override fun onStart() {
        super.onStart()
        startRound()


    }

    interface RoundUICallback {
        fun updateUI(round_updated: Round)
        fun onError()
    }

    /**
     *
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d("DEBUG", "attach")
        if (context is OnRoundFragmentInteractionListener)
            listener = context
        else {
            throw RuntimeException(
                context.toString() +
                        " must implement OnRoundFragmentInteractionListener"
            )
        }
    }

    /**
     *
     */
    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(BOARDSTRING, round.board.tableroToString())
        super.onSaveInstanceState(outState)
    }

    /**
     * Al resumir la actividad actualiza la interfaz
     */
    override fun onResume() {
        super.onResume()
        board_viewc4.invalidate()
    }

    /**
     * Funcion que se ejecuta siempre que hay un cambio en la partida mediante la interfaz PartidaListener.
     * Principalmente actualiza la UI de la aplicacion
     */
    override fun onCambioEnPartida(evento: Evento?) {
        when (evento?.tipo) {
            Evento.EVENTO_CAMBIO -> {
                board_viewc4.invalidate()
                listener?.onRoundUpdated(round)
            }
            Evento.EVENTO_FIN -> {
                if (tablero.estado == Tablero.TABLAS) {
                    Toast.makeText(context, "Tablas - Game Over", Toast.LENGTH_SHORT).show()
                } else {
                    tablero.setComprobacionIJ(tablero.ultimoMovimiento as MovimientoC4)
                    Toast.makeText(context, "Gana - ${game.getJugador(tablero.turno).nombre}", Toast.LENGTH_LONG)
                    .show()
                }
                listener?.onRoundUpdated(round)

                board_viewc4.invalidate()
                if (RoundRepositoryFactory.LOCAL) {
                    AlertDialogFragment().show(
                        activity?.supportFragmentManager, "ALERT_DIALOG"
                    )
                }
            }
        }
    }

    /**
     * objeto estatico que permite a clases externas pasar el argumento del id de la ronda al instanciarse.
     */
    companion object {
        private const val ARG_ROUND = "com.sergioteso.conecta4.round"
        private const val BOARDSTRING = "com.sergioteso.conecta4.boardstring"
        @JvmStatic
        fun newInstance(round: String) =
            RoundFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROUND, round)
                }
            }
    }
}
