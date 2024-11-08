package com.codenge.btcalert.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codenge.btcalert.entity.Prediction;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

public class PredictBtcService  extends AsyncTask<Void, Void, List<Prediction>> {

    private static final String API_URL = "http://164.152.44.23:3000/predict";
    private final Callback callback;
    private Exception error;

    public PredictBtcService(Callback callback) {
        this.callback = callback;
    }


    // Interface de Callback para passar o resultado ou erros
    public interface Callback {
        void onSuccess(List<Prediction> result);
        void onError(Exception e);
    }

    @Override
    protected List<Prediction> doInBackground(Void... voids) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lê a resposta
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            // Converte a resposta para JSON
            JSONObject jsonResponse = new JSONObject(content.toString());
            // Extrai o array "predictions"
            JSONArray predictionsArray = jsonResponse.getJSONArray("predictions");

            // Lista para armazenar os objetos de previsão
            List<Prediction> predictions = new ArrayList<>();

            // Itera sobre o array de previsões e popula a lista
            for (int i = 0; i < predictionsArray.length(); i++) {
                JSONObject predictionObject = predictionsArray.getJSONObject(i);

                // Extraindo os dados de cada previsão
                String predictedDate = predictionObject.getString("predictedDate");
                String predictedHour = predictionObject.getString("predictedHour");
                double predictedPrice = predictionObject.getDouble("predictedPrice");

                // Criando o objeto Prediction e adicionando à lista
                Prediction prediction = new Prediction(predictedDate, predictedHour, predictedPrice);
                predictions.add(prediction);
            }

            return predictions;

        } catch (Exception e) {
            error = e;
            return null;
        }
    }
    @Override
    protected void onPostExecute(List<Prediction> result) {
        if (result != null) {
            callback.onSuccess(result);
        } else {
            callback.onError(error);
        }
    }
}