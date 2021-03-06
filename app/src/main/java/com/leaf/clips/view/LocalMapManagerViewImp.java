package com.leaf.clips.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ListView;

import com.leaf.clips.R;
import com.leaf.clips.model.dataaccess.dao.BuildingTable;
import com.leaf.clips.presenter.LocalMapActivity;
import com.leaf.clips.presenter.LocalMapAdapter;
import com.leaf.clips.presenter.NoInternetAlert;

import java.util.Collection;

/**
 * @author Oscar Elia Conti
 * @version 0.01
 * @since 0.00
 */

/**
 *Classe che si occupa di mostrare le mappe degli edifici salvate nel database locale. La UI legata a questa classe permette all'utente di accedere alle funzionalità di aggiornamento e rimozione di una certa mappa
 */
public class LocalMapManagerViewImp implements LocalMapManagerView {

    /**
     * Presenter della View
     */
    private LocalMapActivity presenter;

    /**
     * Costruttore della classe LocalMapManagerViewImp
     * @param presenter Presenter della View che viene creata
     */
    public LocalMapManagerViewImp(final LocalMapActivity presenter){
        this.presenter = presenter;
        this.presenter.setContentView(R.layout.activity_local_map);

        FloatingActionButton btnAddNewMap = (FloatingActionButton) presenter.findViewById(R.id.fab_add_new_map);
        if (btnAddNewMap != null) {
            btnAddNewMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager)presenter.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (!(networkInfo != null && networkInfo.isConnected())){
                        new NoInternetAlert().show(presenter);
                    } else
                        presenter.DownloadNewMap();
                }
            });
        }
    }
    
    /**
     * Metodo utilizzato per visualizzare la lista delle mappe salvate nel database locale
     * @param buildingTables Collegamento tra la lista delle mappe salvate nel database locale e la view in cui esse devono essere mostrate
     * @param mapVersionStatus Array contenente lo stato di ogni mappa installata. Se vero allora la mappa è da aggiornare, se falso non lo è
     */
    @Override
    public void setAdapter(Collection<BuildingTable> buildingTables, boolean [] mapVersionStatus){
        ListView listView = (ListView) presenter.findViewById(R.id.listViewLocalMaps);
        if (listView != null) {
            listView.setAdapter(new LocalMapAdapter(presenter,buildingTables, mapVersionStatus));
        }
    }
}
