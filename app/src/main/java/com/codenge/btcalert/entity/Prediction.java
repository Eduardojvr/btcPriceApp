package com.codenge.btcalert.entity;
public class Prediction {
    private String predictedDate;
    private String predictedHour;
    private double predictedPrice;

    // Construtor
    public Prediction(String predictedDate, String predictedHour, double predictedPrice) {
        this.predictedDate = predictedDate;
        this.predictedHour = predictedHour;
        this.predictedPrice = predictedPrice;
    }

    // Getters e Setters
    public String getPredictedDate() {
        return predictedDate;
    }

    public void setPredictedDate(String predictedDate) {
        this.predictedDate = predictedDate;
    }

    public String getPredictedHour() {
        return predictedHour;
    }

    public void setPredictedHour(String predictedHour) {
        this.predictedHour = predictedHour;
    }

    public double getPredictedPrice() {
        return predictedPrice;
    }

    public void setPredictedPrice(double predictedPrice) {
        this.predictedPrice = predictedPrice;
    }
}
