package com.coasapp.coas.utils;

import java.util.Comparator;
import java.util.Map;

public class SortContacts implements Comparator<Map<String, String>> {
    @Override
    public int compare(Map<String, String> o1, Map<String, String> o2) {

        return o1.get("contact_name").compareTo(o2.get("contact_name"));

    }
}