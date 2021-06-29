package com.coasapp.coas.utils;

import java.text.DecimalFormat;

/**
 * Created by User on 27-11-2017.
 */

public class Rounding {

    public static String patchDecimal(double d) {
        String number = String.valueOf(d);

        DecimalFormat twoDForm = new DecimalFormat("#.##");

        return twoDForm.format(d);
    }
}
