package com.coasapp.coas.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AK INFOPARK on 11-09-2017.
 */

public class InputValidator {

    public boolean validatePan(String pan) {
        Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");

        Matcher matcher = pattern.matcher(pan);
// Check if pattern matches
        if (matcher.matches()) {
            Log.i("Matching", "Yes");
            return true;
        } else {
            return false;
        }
    }

    public boolean validateIfsc(String ifsc) {
        Pattern pattern = Pattern.compile("[A-Z]{4}[0-9]{7}");

        Matcher matcher = pattern.matcher(ifsc);
// Check if pattern matches
        if (matcher.matches()) {
            Log.i("Matching", "Yes");
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile.length() >= 8;

    }

    public static boolean isValidPrice(String num) {

        final String regExp = "[0-9]+([,.][0-9]{1,2})?";
        final Pattern pattern = Pattern.compile(regExp);

// This can be repeated in a loop with different inputs:
        Matcher matcher = pattern.matcher(num);
        if (matcher.matches()) {
            return Double.valueOf(num) > 0/* && Double.valueOf(num) <= 50000*/;
        }
        return false;
    }

    public static boolean isValidAge(String num) {
        final String regExp = "[0-9]+([,.][0-9]{1,2})?";
        final Pattern pattern = Pattern.compile(regExp);

// This can be repeated in a loop with different inputs:
        Matcher matcher = pattern.matcher(num);
        if (matcher.matches()) {
            return Integer.valueOf(num) > 17;
        }
        return false;
    }

    public static boolean isValidNumber(String num) {
        final String regExp = "[0-9]+([,.][0-9]{1,2})?";
        final Pattern pattern = Pattern.compile(regExp);

// This can be repeated in a loop with different inputs:
        Matcher matcher = pattern.matcher(num);
        if (matcher.matches()) {
            return Double.valueOf(num) >= 0;
        }
        return false;
    }

    public static boolean isName(String name) {
        boolean isName = true;
        if (name.equals("")) {
            return false;
        }
        Log.i("IsName", String.valueOf(isName));
        for (int i = 0; i < name.length(); i++) {
            char charAt = name.charAt(i);
            if (!Character.isSpaceChar(charAt)) {
                if (!Character.isLetter(charAt)) {
                    isName = false;
                }
            }
        }
        /*if (!Character.isUpperCase(name.charAt(0))) {
            isName = false;
        }*/
        Log.i("IsName", String.valueOf(isName));
        return isName;
    }
}
