package com.club.securitygateway.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PrivacyDetector {

    private static final Pattern RRN_PATTERN =
            Pattern.compile("\\d{6}-[1-4]\\d{6}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("01[016789]-?\\d{3,4}-?\\d{4}");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    public List<String> detect(String text) {

        List<String> result = new ArrayList<>();

        if (text == null) {
            return result;
        }

        if (RRN_PATTERN.matcher(text).find()) {
            result.add("RRN");
        }

        if (PHONE_PATTERN.matcher(text).find()) {
            result.add("PHONE");
        }

        if (EMAIL_PATTERN.matcher(text).find()) {
            result.add("EMAIL");
        }

        return result;
    }
}