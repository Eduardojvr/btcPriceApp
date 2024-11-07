package com.codenge.btcalert.scheduled;

import android.os.Handler;

public class Agendador {

    private final Handler handler;
    private final Runnable task;
    private final int interval; // Intervalo em milissegundos
    private boolean isRunning = false;

    public Agendador(Runnable task, int interval) {
        this.handler = new Handler();
        this.task = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    task.run(); // Executa a tarefa fornecida
                    handler.postDelayed(this, interval); // Reagenda a próxima execução
                }
            }
        };
        this.interval = interval;
    }

    // Inicia a execução periódica
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(task);
        }
    }

    // Para a execução periódica
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(task);
    }
}
