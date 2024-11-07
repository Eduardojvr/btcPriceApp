package com.codenge.btcalert.entity;

import java.util.Date;

public class HistoricoBTC {

    private double maiorQue;
    private double menorQue;
    private int idRegistro;
    private Date dtRegistro;

    public double getMaiorQue() {
        return maiorQue;
    }

    public void setMaiorQue(double maiorQue) {
        this.maiorQue = maiorQue;
    }

    public double getMenorQue() {
        return menorQue;
    }

    public void setMenorQue(double menorQue) {
        this.menorQue = menorQue;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Date getDtRegistro() {
        return dtRegistro;
    }

    public void setDtRegistro(Date dtRegistro) {
        this.dtRegistro = dtRegistro;
    }
}
