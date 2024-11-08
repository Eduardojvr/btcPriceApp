package com.codenge.btcalert;

import android.annotation.SuppressLint;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.work.WorkManager;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.codenge.btcalert.Util.FormatadorMoeda;
import com.codenge.btcalert.entity.Prediction;
import com.codenge.btcalert.scheduled.Agendador;
import com.codenge.btcalert.service.BitcoinPriceWorkerNotification;
import com.codenge.btcalert.service.DolarRealService;
import com.codenge.btcalert.service.FetchBitcoinPriceService;
import com.codenge.btcalert.service.PredictBtcService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {



    public double[] valorDolar = new double[1];
    public double[] valorBtc = new double[1];
    private TextView bitcoinPriceTextView;
    private Agendador periodicTaskScheduler;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable predictionRunnable;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bitcoinPriceTextView = findViewById(R.id.valorAtual);

        bitcoinPriceTextView.setText("Iniciando!");


        // Define a tarefa que será executada periodicamente
        Runnable fetchBitcoinPriceTask = new Runnable() {
            @Override
            public void run() {
                // Lógica ou método que será chamado a cada intervalo definido
                retornaValor();
            }
        };

        // Cria e inicia o agendador para executar a cada 5 segundos (5000 ms) - Worker da notificação
        periodicTaskScheduler = new Agendador(fetchBitcoinPriceTask, 500);
        periodicTaskScheduler.start();

        // Cria a tarefa inicial para ser executada imediatamente
        OneTimeWorkRequest initialRequest = new OneTimeWorkRequest.Builder(BitcoinPriceWorkerNotification.class)
                .setInitialDelay(0, TimeUnit.SECONDS) // Começa imediatamente
                .build();

        // Envia o primeiro trabalho para o WorkManager
        WorkManager.getInstance(this).enqueue(initialRequest);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para a execução do agendador para evitar vazamentos de memória
        if (periodicTaskScheduler != null) {
            periodicTaskScheduler.stop();
        }
    }


//    @SuppressLint("MissingPermission")
//    public void notificar() {
//        if((valorDolar[0] * valorBtc[0]) > 0 && (valorDolar[0] * valorBtc[0]) >= 400000){
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
//            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
//            builder.setContentTitle("Atualização Bitcoin");
//            builder.setContentText("Novo valor: "+ valorDolar[0] * valorBtc[0]);
//            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            builder.setAutoCancel(true);
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                NotificationChannel channel = null;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    channel.setDescription(CHANNEL_DESC);
//                }
//                NotificationManager manager= null;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    manager = getSystemService(NotificationManager.class);
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    manager.createNotificationChannel(channel);
//                }
//            }
//            notificationManager.notify(1, builder.build());
//        }
//
//
//    }


    public void retornaValor(){

        // Executa a `AsyncTask` para buscar o preço do Bitcoin
        new FetchBitcoinPriceService(new FetchBitcoinPriceService.Callback() {
            @Override
            public void onSuccess(double price) {
                valorBtc[0] = price;
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }).execute();


        // Executa a `AsyncTask` para buscar o preço do dolar BRL
        new DolarRealService(new DolarRealService.Callback() {
            @Override
            public void onSuccess(double price) {
                valorDolar[0] = price;

                bitcoinPriceTextView.setText(FormatadorMoeda.formatToBRL(1 * valorBtc[0]));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }


        }).execute();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Cria o Runnable que chama PredictBtcService a cada 10 segundos
        predictionRunnable = new Runnable() {
            @Override
            public void run() {
                // Inicia a PredictBtcService
                new PredictBtcService(new PredictBtcService.Callback() {
                    @Override
                    public void onSuccess(List<Prediction> result) {
                        // Trate o sucesso da previsão, por exemplo, atualizando a UI
                        updateUIWithPredictions(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Trate o erro, como mostrar um log ou mensagem de erro
                        e.printStackTrace();
                    }
                }).execute();

                // Reagenda o Runnable após 10 segundos (10000 ms)
                handler.postDelayed(this, 10000);
            }
        };

        // Inicia o primeiro chamado
        handler.post(predictionRunnable); // Executa imediatamente pela primeira vez
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove callbacks quando a atividade é parada para evitar vazamentos de memória
        handler.removeCallbacks(predictionRunnable);
    }

    private void updateUIWithPredictions(List<Prediction> predictions) {
        // Atualize a UI com a lista de previsões, por exemplo, exiba em um TextView
        TextView textPredict = findViewById(R.id.textPredict);
        StringBuilder builder = new StringBuilder();
        for (Prediction prediction : predictions) {
            builder.append(prediction.getPredictedDate())
                    .append(" | ")
                    .append(prediction.getPredictedHour())
                    .append(" | ")
                    .append(prediction.getPredictedPrice())
                    .append("\n");
        }
        textPredict.setText(builder);
    }

}