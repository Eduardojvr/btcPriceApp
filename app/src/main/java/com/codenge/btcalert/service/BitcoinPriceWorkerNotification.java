package com.codenge.btcalert.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.codenge.btcalert.R;
import com.codenge.btcalert.Util.FormatadorMoeda;

import java.util.concurrent.TimeUnit;

public class BitcoinPriceWorkerNotification extends Worker {
    public static final String CHANNEL_ID = "simplified_coding";
    public static final String CHANNEL_NAME = "Simplified Coding";
    public static final String CHANNEL_DESC = "Simplified Coding";
    private double valorBtc;
    private double valorReal;
    public BitcoinPriceWorkerNotification(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {
        notifica();
        scheduleNextExecution();

        // Retorna o resultado como sucesso
        return Result.success();
    }

    private void scheduleNextExecution() {
        OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(BitcoinPriceWorkerNotification.class)
                .setInitialDelay(5, TimeUnit.SECONDS) // Atraso inicial de 5 segundos
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(nextRequest); // Envia o próximo trabalho
    }

    public void notifica(){

        // Executa a `AsyncTask` para buscar o preço do Bitcoin
        new FetchBitcoinPriceService(new FetchBitcoinPriceService.Callback() {
            @Override
            public void onSuccess(double price) {
                 setValorBtc(price);
                // Executa a `AsyncTask` para buscar o preço do dolar BRL
                new DolarRealService(new DolarRealService.Callback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(double price) {
                        setValorReal(price);
                        if ((getValorReal()*getValorBtc()) >= 400000) {
                            // Criação do canal de notificação para Android 8.0+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                                channel.setDescription(CHANNEL_DESC);
                                channel.setSound(null, null); // Remove o som
                                channel.setVibrationPattern(new long[]{0}); // Remove a vibração

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                notificationManager.createNotificationChannel(channel);
                            }
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle("Atualização Bitcoin")
                                    .setContentText("Novo valor: " + FormatadorMoeda.formatToBRL(getValorReal()*getValorBtc()))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            notificationManager.notify(1, builder.build());
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }


                }).execute();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }).execute();




    }
    public double getValorBtc() {
        return valorBtc;
    }

    public void setValorBtc(double valorBtc) {
        this.valorBtc = valorBtc;
    }

    public double getValorReal() {
        return valorReal;
    }

    public void setValorReal(double valorReal) {
        this.valorReal = valorReal;
    }
}
