package com.leaf.clips.model.beacon;
/**
 * @author Federico Tavella
 * @version 0.01
 * @since 0.00
 */

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.leaf.clips.presenter.MyApplication;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Classe che si occupa del rilevamento dei beacon. Estende la
 * classe android.app.Service e implementa le interfacce org.altbeacon.beacon.BeaconConsumer
 * e org.altbeacon.beacon.startup.BootstrapNotifier
 */
public class BeaconManagerAdapter extends Service implements BeaconRanger, BeaconConsumer, BootstrapNotifier, RangeNotifier {

    static{
        Log.i("SERVICE", "STATIC SERVICE");
    }

    /**
     * Riferimento alla classe che permette di gestire il rilevamento dei beacon
     */
    private BeaconManager beaconManager;

    /**
     * Riferimento al LocalBinder che detiene il collegamento con il Service
     */
    private IBinder locBinder;

    /**
     * Rappresenta un criterio che serve ad eseguire il match con un beacon
     */
    private Region region;

    /**
     * Rappresenta una stringa che identifica la marca del Beacon o del protocollo
     * Per informazioni sui dati dei beacon https://support.kontakt.io/hc/en-gb/articles/201492522-Scan-response-packet-structure
     * offset campo d 24
     */
    //AltBeacon beacons
    private static String beaconLayout = MyApplication.getConfiguration().getBeaconLayout();

    /**
     * Insieme dei periodi di scan del BeaconManager
     */
    private Map<PeriodType, Long> periods;

    /**
     * Indica se il BeaconManager è in background o meno
     */
    boolean isBackground = false;

    /**
     * Metodo che inizializza i parametri della classe alla creazione di un’istanza
     */
    @Override
    public void onCreate(){

        Log.i("SERVICE", "CREATING THE SERVICE");
        super.onCreate();
        locBinder = new LocalBinder();
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(beaconLayout));
        UUID uuid = UUID.fromString(MyApplication.getConfiguration().getApplicationUUID());
        region = new Region("Region", Identifier.fromUuid(uuid), null, null);
        //region = new Region("Region", null, null, null);
        periods = new HashMap<>();
        setMonitorNotifier(this);
        BeaconManager.setRegionExitPeriod(2000);
        beaconManager.setForegroundBetweenScanPeriod(10);
        beaconManager.setForegroundScanPeriod(1800);
        beaconManager.setRangeNotifier(this);
        beaconManager.bind(this);

        /*new Thread(
                new Runnable() {
                    String minor = "0";
            @Override
            public void run() {

                while(true){
                    try {
                        Thread.sleep(1*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LinkedList<Long> l = new LinkedList<Long>();
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long)0);
                    l.add((long) 0);
                    MyBeacon b = new MyBeaconImp(new Beacon.Builder()
                            .setId1("f7826da6-4fa2-4e98-8024-bc5b71e0893e")
                            .setId2("666").setId3(minor).setDataFields(l).build());
                    PriorityQueue<MyBeacon> p = new PriorityQueue<>();

                    p.add(b);

                    Intent msg = new Intent("beaconsDetected");
                    msg.putExtra("queueOfBeacons", p);
                    LocalBroadcastManager.getInstance(BeaconManagerAdapter.this).sendBroadcast(msg);
                    if (minor.equals("0"))
                        minor = "1004";
                    else if (minor.equals("1004"))
                        minor = "1000";
                    else if (minor.equals("1000"))
                        minor = "1008";
                    else
                        minor = "0";
                    try {
                        if (minor.equals("0"))
                            Thread.sleep(60*1000);
                        else
                            Thread.sleep(10*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/
    }

    /**
     * Metodo che esegue le azioni necessarie alla distruzione del Service
     */
    @Override
    public void onDestroy(){
        Log.i("SERVICE", "DESTROYING THE SERVICE");
        setMonitorNotifier(null);
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.getBeaconParsers().clear();
        beaconManager.unbind(this);
        periods.clear();
        region = null;
        beaconManager = null;
        super.onDestroy();
    }

    /**
     * Metodo che permette di impostare il MonitorNotifier per il Servidce
     * @param monitorNotifier MonitorNotifier da impostare
     */
    private void setMonitorNotifier(MonitorNotifier monitorNotifier) {
        Log.i("SERVICE", "SETTING MONITOR NOTIFIER");
        beaconManager.setMonitorNotifier(monitorNotifier);
    }

    /**
     * Metodo per decidere come comportarsi alla connessione del Service
     */
    @Override
    public void onBeaconServiceConnect() {
        try {
            Log.i("SERVICE", "STARTING MONITORING");
            beaconManager.startRangingBeaconsInRegion(region);
            //beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i("SERVICE", "NOT MONITORING");
        }
    }

    /**
     * Questo metodo serve per far partire il Ranging dei Beacon
     */
    private void startRanging() {

        try {
            Log.i("SERVICE", "STARTING RANGING");
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che definisce delle azioni da eseguire nel momento
     * in cui il dispositivo rileva uno o più beacon nella Region
     * @param region Criterio che serve ad eseguire il match con un beacon
     */
    @Override
    public void didEnterRegion(Region region) {
        startRanging();
    }

    /**
     * Metodo che definisce delle azioni da eseguire nel momento
     * in cui il dispositivo non rileva più beacon nella Region
     * @param region Criterio che serve ad eseguire il match con un beacon
     */
    @Override
    public void didExitRegion(Region region) {
        Log.i("SERVICE", "start monitoring");
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            beaconManager.startMonitoringBeaconsInRegion(region);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che determina se un dispositivo è presente all'interno di una Region
     * @param i Stato della Region che può essere MonitorNotifier.INSIDE o MonitorNotifier.OUTSIDE
     * @param region Criterio che serve ad eseguire il match con un beacon
     */
    @Override
    public void didDetermineStateForRegion(int i, Region region) {

        if(i==MonitorNotifier.INSIDE) {
            try {
                beaconManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.i("BEACON_MANAGER_ADAPTER", "Beacons are visible in the region");
        }
        else{
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.startMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.i("BEACON_MANAGER_ADAPTER", "No beacons are visible in the region");
        }

    }

    /**
     * Metodo che viene invocato quando vengono rilevati beacon nella Region
     * @param collection Beacon rilevati
     * @param region Region in cui sono rilevati i beacon
     */
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        if (collection.size() > 0) {
            Log.i("SERVICE", "BEACONS DETECTED");
            PriorityQueue<MyBeacon> p = new PriorityQueue<>(collection.size());

            p.clear();

            for (Beacon oneBeacon : collection) {
                p.add(new MyBeaconImp(oneBeacon));
                Log.i("Beaconz", new MyBeaconImp(oneBeacon).toString());
            }

            Intent msg = new Intent("beaconsDetected");
            msg.putExtra("queueOfBeacons", p);
            LocalBroadcastManager.getInstance(BeaconManagerAdapter.this).sendBroadcast(msg);
        }
        else {
            Log.i("SERVICE", "COLLECTION EMPTY");
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
                beaconManager.startMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo che serve a definire determinate azioni nel momento
     * in cui una classe viene collegata ad un Service
     * @param intent Intent del Service di cui si vuole fare il collegamento
     * @return IBinder Binder per effettuare il bind con il Service
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SERVICE", "BINDING THE SERVICE");
        return locBinder;
    }

    /**
     * Metodo per notificare al Service che l’applicazione sta andando in background
     * @param mode Parametro che serve per impostare se l’applicazione sta andando in background o no
     */
    @Override
    public void setBackgroundMode(boolean mode) {
        beaconManager.setBackgroundMode(mode);
        isBackground = mode;
    }

    /**
     * Metodo per impostare il periodo che determina l’uscita di un
     * beacon da una Region
     * @param p Questo parametro richiede il periodo in millisecondi
     */
    @Override
    public void setRegionExitPeriod(long p) {
        BeaconManager.setRegionExitPeriod(p);
    }

    /**
     * Metodo che serve a modificare il periodo di scansione per il rilevamento
     * dei beacon
     * @param p Periodo di scansione
     * @param type Parametro per decidere se cambiare il periodo di scansione in Foreground o in Background,
     *             di scansione o di non scansione
     */
    @Override
    public void modifyScanPeriod(long p, PeriodType type) {
        switch (type){
            case BACKGROUND_BETWEEN :
            {
                beaconManager.setBackgroundBetweenScanPeriod(p);
                periods.put(PeriodType.BACKGROUND_BETWEEN, p);
            }
            break;
            case BACKGROUND :
            {
                beaconManager.setBackgroundScanPeriod(p);
                periods.put(PeriodType.BACKGROUND, p);
            }
            break;
            case FOREGROUND :
            {
                beaconManager.setForegroundScanPeriod(p);
                periods.put(PeriodType.FOREGROUND, p);
            }
            break;
            case FOREGROUND_BETWEEN :
            {
                beaconManager.setForegroundBetweenScanPeriod(p);
                periods.put(PeriodType.FOREGROUND_BETWEEN, p);
            }
            break;
            default :
                break;

        }
        try {
            beaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * Classe definita per permettere la comunicazione tra processi
     * (IPC), in questo caso permette di comunicare con i metodi pubblici
     * definiti internamente ad una classe Service. Estende la classe
     * android.os.Binder
     */
    public class LocalBinder extends Binder {

        /**
         * Metodo restituisce il riferimento al Service BeaconManagerAdapter
         * @return BeaconManagerAdapter Riferimento al BeaconManagerAdapter per invocare metodi pubblici
         */

        public BeaconManagerAdapter getService() {
            Log.i("SERVICE", "RETURNING THIS SERVICE");
            return BeaconManagerAdapter.this;
        }
    }

    /**
     * Metodo che ritorna un valore booleano che indica se il BeaconManager è in background
     * @return boolean Valore booleano che indica se il BeaconManager è in background
     */
    @Override
    public boolean isBackground(){
        return isBackground;
    }

    /**
     * Metodo per ottenere uno tra i periodi di scan del BeaconManager in base alla richiesta
     * @param type Tipo di periodo di scan desiderato
     * @return long Periodo di scan desiderato
     */
    @Override
    public long getPeriod(PeriodType type){
        return periods.get(type);
    }
}

