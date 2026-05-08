package com.hotel.gestion.utils;

import java.util.Locale;

public final class MoneyUtils {
    private MoneyUtils() {
    }

    public static String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "%,.0f FBU", amount);
    }

    public static String formatMoneyPerNight(double amount) {
        return String.format(Locale.getDefault(), "%,.0f FBU / nuit", amount);
    }
}
