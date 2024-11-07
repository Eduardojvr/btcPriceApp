package com.codenge.btcalert.Util;

import java.text.NumberFormat;
import java.util.Locale;
public class FormatadorMoeda {
    public static String formatToBRL(double value) {
        // Cria uma instância de NumberFormat para formato brasileiro
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return numberFormat.format(value);
    }
}
