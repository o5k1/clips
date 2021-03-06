package com.leaf.clips.model;
/**
 * @author Federico Tavella
 * @version 0.01
 * @since 0.00
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.leaf.clips.model.beacon.BeaconManagerAdapter;
import com.leaf.clips.model.beacon.BeaconRanger;
import com.leaf.clips.model.beacon.MyBeacon;
import com.leaf.clips.model.beacon.PeriodType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;


/** 
*Classe base per la comunicazione con le classi che si occupano del rilevamento dei beacon
*/ 
public abstract class AbsBeaconReceiverManager extends BroadcastReceiver {

    /**
     * PriorityQueue, eventualmente vuota, contenente gli ultimi beacon rilevati
     */
    volatile static protected PriorityQueue<MyBeacon> lastBeaconsSeen = new PriorityQueue<>();

    /**
     * Collection dei listener della classe
     */
    protected Collection<Listener> listeners;

    /**
     * Service che si occupa del rilevamento dei beacon
     */
    private BeaconRanger beaconManagerAdapter;

    /**
     * Contesto dell'applicazione
     */
    private final Context context;

    /**
     * Connessione con il Service per la comunicazione con il service stesso
     */
    private ServiceConnection serviceConnection;

    /**
     * Intent per ricevere i beacon inviati dal BeaconManagerAdapter
     */
    private final Intent serviceStart;

    /**
     * Variabile booleana che indica l'avvenuta connesione tra il Service e la ServiceConnection
     */

    volatile private boolean isBound; // TODO: 04/05/2016 va bene volatile? o meglio synchronized?

    /**
     * Costruttore della classe AbsBeaconReceiverManager
     */
    public AbsBeaconReceiverManager(Context context){
        this.context = context;
        serviceStart = new Intent(context, BeaconManagerAdapter.class);
        serviceConnection = new ServiceConnectionImp();
        context.bindService(serviceStart, serviceConnection,Context.BIND_AUTO_CREATE);
        listeners = new LinkedList<>();
        LocalBroadcastManager.getInstance(context).registerReceiver(this,
                new IntentFilter("beaconsDetected"));
    }

    /**
     * Metodo che ritorna il contesto dell'applicazione
     * @return  Context
     */
    protected Context getContext(){
        return context;
    }

    /**
     * Metodo che permette di modificare il tempo tra una scansione per la ricerca dei beacon
     * e la successiva
     * @param p Periodo di scansione dei beacon da scansionare
     * @param type Parametro per decidere se cambiare il periodo di scansione in Foreground
     *             o in Background, di scansione o di non scansione
     */
    public void modifyScanningPeriod(long p, PeriodType type){
        beaconManagerAdapter.modifyScanPeriod(p, type);
    }

    /**
     * Metodo che permette di attivare il service che si occupa di fare le scansioni per trovare
     * i beacon
     */
    public void startService(){
        if(!isBound)
            context.bindService(serviceStart, serviceConnection,Context.BIND_AUTO_CREATE);
        context.startService(serviceStart);

    }

    /**
     * Metodo che permette di fermare il service che si occupa di fare le scansioni per trovare
     * i beacon
     */
    public void stopService(){
        context.unbindService(serviceConnection);
        context.stopService(serviceStart);

    }

    /**
     * Classe che permette la connessione al Service che si occupa del rilevamento dei beacon
     */
    public class ServiceConnectionImp implements ServiceConnection {

        /**
         * Metodo che viene invocato alla connessione con il Service
         * @param className Nome della classe del Service
         * @param service Bind con il Service
         */
        public void onServiceConnected(ComponentName className, IBinder service) {
            BeaconManagerAdapter.LocalBinder binder = (BeaconManagerAdapter.LocalBinder) service;
            beaconManagerAdapter = binder.getService();
            isBound = true;
        }

        /**
         * Metodo che viene invocato alla disconnessione con il service
         * @param arg0
         */
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    }

    /**
     * Metodo che viene invocato alla ricezione della lista di beacon rilevata
     * @param context Contesto di esecuzione dell'applicazione
     * @param intent Intent del messaggio mandato in broadcast
     */
    @Override //Method of BroadcastReceiver
    abstract public void onReceive(Context context, Intent intent);

    /**
     * Metodo che permette di registrare un listener
     * @param listener Listener che deve essere aggiunto alla lista di Listener
     */
    protected void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Metodo che permette di rimuovere un listener
     * @param listener Listener che deve essere rimosso dalla lista di Listener
     */
    protected void removeListener(Listener listener) {
        listeners.remove(listener);
    }

}

