package com.codenge.btcalert.service;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchBitcoinPriceService extends AsyncTask<Void, Void, Double> {

    private static final String API_URL = "https://api.coindesk.com/v1/bpi/currentprice/BTC.json";
    private final Callback callback;
    private Exception error;

    // Interface de Callback para passar o resultado ou erros
    public interface Callback {
        void onSuccess(double price);
        void onError(Exception e);
    }

    public FetchBitcoinPriceService(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Double doInBackground(Void... voids) {
        try {
            // Cria a conexão HTTP
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
            // Extrai o valor do preço do Bitcoin em USD
            return jsonResponse.getJSONObject("bpi").getJSONObject("USD").getDouble("rate_float");

        } catch (Exception e) {
            error = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Double result) {
        if (result != null) {
            callback.onSuccess(result);
        } else {
            callback.onError(error);
        }
    }

}
