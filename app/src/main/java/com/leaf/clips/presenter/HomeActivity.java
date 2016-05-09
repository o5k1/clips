package com.leaf.clips.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.leaf.clips.R;
import com.leaf.clips.model.InformationManager;
import com.leaf.clips.model.NoBeaconSeenException;
import com.leaf.clips.view.HomeView;
import com.leaf.clips.view.HomeViewImp;

import java.util.List;

import javax.inject.Inject;

public class HomeActivity extends AppCompatActivity {
    /**
     *  TODO
        Enable Suggestion
     */

    /**
     * Riferimento utilizzato per accedere alle informazioni trattate dal model
     */
    @Inject
    InformationManager informationManager;

    /**
     * View associata a tale Activity
     */
    private HomeView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new HomeViewImp(this);

        ((MyApplication)getApplication()).getInfoComponent().inject(this);

        updateBuildingAddress();
        updateBuildingName();
        updateBuildingDescription();
        updateBuildingOpeningHours();
        updatePoiCategoryList();
    }

    /**
     * Chiude il Drawer se utente esegue tap sul tasto Back.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_home);
        if(drawer != null){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di visualizzazione della modalità esplora
     * @return  void
     */
    public void showExplorer(){
        Intent intent = new Intent(this, NearbyPoiActivity.class);
        startActivity(intent);
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di visualizzazione della guida
     * @return  void
     */
    public void showHelp(){
        // TODO: 5/3/16  codify
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di visualizzazione della mappe salvate nel database locale
     * @return  void
     */
    public void showLocalMaps(){
        // TODO: 5/3/16  
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di visualizzazione di tutti i POI appartenenti ad un certa categoria
     * @param categoryName Nome della categoria di cui visualizzare l'insieme di POI appartenente
     * @return  void
     */
    public void showPoisCategory(String categoryName){
        Intent intent = new Intent(this, PoiCategoryActivity.class);
        intent.putExtra("category_name",categoryName);
        startActivity(intent);
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di visualizzazione delle preferenze dell'utente
     * @return  void
     */
    public void showPreferences(){
        // TODO: 5/3/16  
    }

    /**
     * Metodo che viene invocato a seguito della richiesta di inizio della navigazione
     * @param poiPosition Identificativo del POI verso il quale si vuole effettuare una navigazione
     * @return  void
     */
    public void startNavigation(int poiPosition){
        //TODO: 5/3/16
    }

    /**
     * Metodo che recupera l'indirizzo dell'edificio e lo passa alla View corrispondente
     * @return  void
     */
    public void updateBuildingAddress(){
        try {
            String address = informationManager.getBuildingMap().getAddress();
            view.setBuildingAddress(address);
        } catch (NoBeaconSeenException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che recupera la descrizione dell'edificio e lo passa alla View corrispondente
     * @return  void
     */
    public void updateBuildingDescription(){
        try {
            String desc = informationManager.getBuildingMap().getDescription()+"%%%%";
            Log.d("DESC",desc);
            view.setBuildingDescription(desc);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo che recupera il nome dell'indirizzo dell'edificio e lo passa alla View corrispondente
     * @return  void
     */
    public void updateBuildingName(){
        try {
            String name = informationManager.getBuildingMap().getName();
            view.setBuildingName(name);
        } catch (NoBeaconSeenException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che recupera l'orario di apertura dell'edificio e lo passa alla View corrispondente
     * @return  void
     */
    public void updateBuildingOpeningHours(){
        try {
            String hours = informationManager.getBuildingMap().getOpeningHours();
            view.setBuildingOpeningHours(hours);
        } catch (NoBeaconSeenException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che recupera la lista di categorie di POI nell'edificio e lo passa alla View corrispondente
     * @return  void
     */
    public void updatePoiCategoryList(){
        List<String> categories = (List<String>) informationManager.getAllCategories();
        view.setPoiCategoryListAdapter(categories);
    }

    /**
     * Metodo che permette di attivare la lista dei possibili POI raggiungibili a partire da una stringa
     * @return  void
     */
    public void enableSuggestions(){
        // TODO: 5/3/16  
    }

}