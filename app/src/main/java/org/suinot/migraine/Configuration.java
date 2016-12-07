package org.suinot.migraine;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.R.string.cancel;
import static android.R.string.ok;
import static org.suinot.migraine.R.layout.configuration;

/**
 * Created by remi on 27/08/16.
 * Activity pour la configuration de la base de données du médicament (visu, ajout, suppression)
 */

public class Configuration extends Activity {

    GestionBaseMedicament medicBdd;
    CustomAdapter_medic monAdapter = null;
    private ArrayList<Item_Medicament> data;
    private long derniere_donnees_initiale;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        final Context context = this;

        setContentView (configuration);

        medicBdd = new GestionBaseMedicament (this);
        // On ouvre la base de données pour écrire dedans
        medicBdd.open ();

        // copieBase = medicBdd;
        derniere_donnees_initiale = medicBdd.NombreMedicament ();

        // setup the data source
        data = new ArrayList<> ();
        data = medicBdd.getAllMedicaments ();

        ListView listViewMedicaments = (ListView) findViewById (R.id.C_affiche_bdd);
        monAdapter = new CustomAdapter_medic (this, R.layout.template_item, this.data);  //instantiation de l'adapter une seule fois

        listViewMedicaments.setAdapter (monAdapter);
        final LayoutInflater inflater = LayoutInflater.from (this);
        // final View dialogView = inflater.inflate (R.layout.nouveau_medic, null);
        // ajout d'un mémdicament
        Button ajouter = (Button) findViewById (R.id.C_ajout);
        ajouter.setOnClickListener (new View.OnClickListener () { // Notre classe anonyme
            public void onClick(View view) { // et sa méthode !
                // ici, on ouvre une boite avec deux champs: medicament et dose

                AlertDialog.Builder dialog = new AlertDialog.Builder (context);
                dialog.setView (inflater.inflate (R.layout.nouveau_medic, null));
                dialog.setTitle (R.string.nouveau_medicament);
                final AlertDialog alertDialog = dialog.create ();
                final AlertDialog.Builder builder = dialog.setPositiveButton (ok, new DialogInterface.OnClickListener () {
                    public void onClick(DialogInterface dialog, int which) {
                        Medicament medic = new Medicament ();
                        EditText et1 = (EditText) ((AlertDialog) dialog).findViewById (R.id.Nouveau_Medic);
                        EditText et2 = (EditText) ((AlertDialog) dialog).findViewById (R.id.Nouveau_Dose);
                        String s1 = et1 != null ? et1.getText ().toString () : null;
                        String s2 = et2 != null ? et2.getText ().toString () : null;
                        medic.setMedicament (s1);
                        medic.setDose (s2);
                        if (medicBdd.insertMedicament (medic) >= 1) {
                            //Enregistrement réussi, ajouter le nouveau médicament dans la liste
                            Item_Medicament item1 = new Item_Medicament (s1, s2, 0);
                            data.add (item1);
                            //Ensuite rafraîchir l'adaptateur
                            monAdapter.notifyDataSetChanged ();
                        }
                    }
                });

                dialog.setNegativeButton (cancel, new DialogInterface.OnClickListener () {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss ();
                    }
                });
                dialog.show ();
            }
        });

        listViewMedicaments.setOnItemClickListener (new AdapterView.OnItemClickListener () {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                clique court
                    pour l'instant, juste un toast
                    Todo: stat sur le nombre d'utilisation du médicament
                 */
                Toast.makeText (getApplicationContext (), "Click court: " + parent.getItemAtPosition (position).toString () + "(" + position + ")",
                        Toast.LENGTH_SHORT).show ();
            }
        });
        listViewMedicaments.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener () {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                demande de suppression dans la base de données
                  1/ confirmation avec une boite d'alerte
                  2/ si oui
                     3/ appel GestionBaseMedicament -> removeMedicamentWithID (position)
                     4/ update de l'affichage
                 */
                Item_Medicament obj = (Item_Medicament) parent.getItemAtPosition (position);
                Suppression (position, obj, view);
                return true;
            }
        });

        Button sauver = (Button) findViewById (R.id.C_sauver);
        sauver.setOnClickListener (new View.OnClickListener () { // Notre classe anonyme
            public void onClick(View view) { // et sa méthode !

                Toast.makeText (getApplicationContext (), "Sauvegarde de la base de données Médicaments ", Toast.LENGTH_SHORT).show ();
                medicBdd.close ();
                System.exit (0);
            }
        });

        // Annulation de nos ajouts de médicament
        Button annuler = (Button) findViewById (R.id.C_annuler);
        annuler.setOnClickListener (new View.OnClickListener () { // Notre classe anonyme
            public void onClick(View view) { // et sa méthode !
                // on a dans derniere_donnees_initiale la dfin des données de la basse, on supprime toutes les autres
                long last;
                last = medicBdd.NombreMedicament ();
                while (last > derniere_donnees_initiale) {
                    medicBdd.removeMedicamentWithID (last);
                    last = medicBdd.NombreMedicament ();
                }

                Toast.makeText (getApplicationContext (), "Annulation des modifications", Toast.LENGTH_SHORT).show ();
                medicBdd.close ();
                System.exit (0);
            }
        });


        Button exporter = (Button) findViewById (R.id.C_export);
        exporter.setOnClickListener (new View.OnClickListener () { // Notre classe anonyme
            public void onClick(View view) { // et sa méthode !

                Toast.makeText (getApplicationContext (), "(A venir) Sauvergarde des données patients sur le téléphone",
                        Toast.LENGTH_LONG).show ();
                Export_de_la_base_patient();
            }
        });


    }

    private void Export_de_la_base_patient() {
        finish ();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void Suppression(final int listeitemId, Item_Medicament liste, View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder (this);
        Medicament medic = new Medicament ();
        medic = medicBdd.getMedicamentWithNom (liste.get ("medicament"), liste.get ("dosage"));
        final int id = medic.getId ();
        alertDialogBuilder.setMessage ("Voulez vous vraiment supprimer " + medic.getMedicament ());

        final Medicament finalMedic = medic;
        alertDialogBuilder.setPositiveButton ("yes", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // Ici suppression du médicament et demande de réaffichage
                finalMedic.setInvalide (1);
                if (medicBdd.updateMedicament (id, finalMedic) != 0) {
                    monAdapter.notifyDataSetChanged ();
                } else {
                    Toast.makeText (getApplicationContext (), "Erreur lors de l'invalidation du médicament. \nNe sera pas exécuté ! ", Toast.LENGTH_LONG).show ();
                }
            }
        }).setNegativeButton ("No", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create ();
        alertDialog.show ();
    }
}
