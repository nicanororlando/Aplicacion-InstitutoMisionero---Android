/*
 * Created by Nicanor Orlando.
 * Copyright (c) 7/12/21 09:33.
 * All rights reserved.
 */

package com.institutomisionero.IM.ui.gallery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.HashMap;

import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.institutomisionero.IM.R;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

public class GalleryFragment extends Fragment {
    //private final String data = "http://localhost:5000/data/";
    private final String data = "https://nubecolectiva.com/blog/tutos/demos/leer_json_android_java/datos/postres.json";
    private final int codigodatos = 1;

    private ProgressBar progressBar;
    protected ImageCarousel carousel;
    List<CarouselItem> carouselList = new ArrayList<>();
    ArrayList<GalleryModel> galleryModelArrayList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        carousel = view.findViewById(R.id.carousel);

        // Register lifecycle. For activity this will be lifecycle/getLifecycle() and for fragments it will be viewLifecycleOwner/getViewLifecycleOwner().
        carousel.registerLifecycle(getLifecycle());

        // Llamo al método para leer el archivo JSON.
        leerJSON();

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    public void leerJSON(){

        // Inicio una tare Asíncrona
        new AsyncTask<Void, Void, String>(){

            // La tarea se llevará acabo de fondo
            protected String doInBackground(Void[] params) {

                String response = "";

                // Declaro un HashMap
                HashMap<String, String> map = new HashMap<>();

                // Hago la petición de los datos
                try {
                    HttpRequest req = new HttpRequest(data);
                    response = req.prepare(HttpRequest.Method.POST).reciboDatos(map).enviaryRecibirString();
                    Log.d("JSON", response);
                } catch (Exception e) {
                    response = e.getMessage();
                }
                return response;
            }

            // Después de realizar la petición de los datos, llamo al método tareaCompletada()
            // El método tareaCompletada() lo crearé a continuación
            protected void onPostExecute(String resultado) {
                tareaCompletada(resultado, codigodatos);
            }

        }.execute();
    }

    public void tareaCompletada(String response, int serviceCode) {

        // Uso un case y le paso la variable 'codigodatos'
        if (serviceCode == codigodatos) {

            // Verifico si los datos se recibieron.
            if (siCorrecto(response)) {

                // A mi modelo le paso el método obtenerInformacion(), este método lo crearé más adelante.
                galleryModelArrayList = obtenerInformacion(response);

                progressBar.setVisibility(View.GONE);

                llenarCarousel();

            } else {
                // Si hubo error, lo muestro en un Toast
                Toast.makeText(this.getContext(), obtenerCodigoError(response), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<GalleryModel> obtenerInformacion(String response) {

        // Creo un array con los datos JSON que he obtenido
        ArrayList<GalleryModel> listJSON = new ArrayList<>();

        // Solicito los datos al archivo JSON
        try {
            JSONObject jsonObject = new JSONObject(response);

            // En los datos que recibo verifico si obtengo el estado o 'status' con el valor 'true'
            // El dato 'status' con el valor 'true' se encuentra dentro del archivo JSON
            if (jsonObject.getString("status").equals("true")) {

                // Accedo a la fila 'gallery' del archivo JSON
                JSONArray dataArray = jsonObject.getJSONArray("postres");

                // Recorro los datos que hay en la fila 'gallery' del archivo JSON
                for (int i = 0; i < dataArray.length(); i++) {

                    // Creo la variable 'datosModelo' y le paso mi modelo 'MyAppModel'
                    GalleryModel dataModel = new GalleryModel();

                    // Creo la  variable 'objetos' y recupero los valores
                    JSONObject objetos = dataArray.getJSONObject(i);

                    // Selecciono dato por dato
                    dataModel.setId(objetos.getInt("id"));
                    dataModel.setTitle(objetos.getString("stock"));
                    dataModel.setUrl(objetos.getString("img"));
                    dataModel.setImage(objetos.getString("img"));
                    dataModel.setDescription(objetos.getString("nombre"));

                    // Meto los datos en el array que definí más arriba 'listaArray'
                    listJSON.add(dataModel);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listJSON;
    }

    public void llenarCarousel() {
        for(int i = 0; i < galleryModelArrayList.size(); i++){
            carouselList.add(new CarouselItem(
                    galleryModelArrayList.get(i).getImage(),
                    galleryModelArrayList.get(i).getTitle()
            ));
        }

        carousel.setData(carouselList);

        carousel.setCarouselListener(new CarouselListener() {
            @Nullable
            @Override
            public ViewBinding onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
                return null;
            }

            @Override
            public void onBindViewHolder(@NonNull ViewBinding viewBinding, @NonNull CarouselItem carouselItem, int i) {

            }

            @Override
            public void onClick(int i, @NonNull CarouselItem carouselItem) {
                createCustomDialog(i).show();
            }

            @Override
            public void onLongClick(int i, @NonNull CarouselItem carouselItem) {

            }
        });
    }

    public AlertDialog createCustomDialog(int i) {
        final AlertDialog alertDialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext(), R.style.DialogTheme);

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Pasamos null como vista principal porque va en el diseño del diálogo
        View v = inflater.inflate(R.layout.dialog_link, null);

        // builder.setView(inflater.inflate(R.layout.dialog_signin, null))
        Button openLink = (Button) v.findViewById(R.id.openLink);

        builder.setView(v);
        alertDialog = builder.create();

        // Add action buttons
        openLink.setOnClickListener(
                v1 -> {
                    Uri uri = Uri.parse(galleryModelArrayList.get(i).getUrl());
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(browserIntent);
                }
        );
        return alertDialog;
    }

    public boolean siCorrecto(String response) {

        // Verificamos si la petición de los datos ha sido correcta
        try {

            // Creo la variable 'objetoJson' de tipo JSONObjetc (Objeto JSON) y le
            // paso los datos que he recibido (response)
            JSONObject objetoJson = new JSONObject(response);

            // En los datos que he recibido verifico si obtengo el estado o 'status' con el valor 'true'
            // El dato 'status' con el valor 'true' se encuentra dentro del archivo JSON
            // Retorno 'true' si es correcto y 'false' si es incorrecto
            return objetoJson.optString("status").equals("true");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Si nada se lleva acabo retorno 'false'
        return false;
    }

    public String obtenerCodigoError(String response) {

        // Solicitamos el código de error que se encuentra en el archivo JSON
        try {
            // El archivo JSON contiene el dato 'message'
            JSONObject jsonObject = new JSONObject(response);

            return jsonObject.getString("message");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Si no hay datos en el archiv JSON, muestro un mensaje
        return "No hay datos";
    }
}