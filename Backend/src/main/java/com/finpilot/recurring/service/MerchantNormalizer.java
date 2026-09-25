package com.finpilot.recurring.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class MerchantNormalizer {

    private static final Pattern URL_PREFIX_PATTERN = Pattern.compile("^(https?://)?(www\\.)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMAIN_SUFFIX_PATTERN = Pattern.compile("(\\.(com|in|org|net|co|io|app|ai|me|cc|info))(/.*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CORP_SUFFIX_PATTERN = Pattern.compile("\\b(pvt\\.?\\s*ltd\\.?|private\\s+limited|ltd\\.?|limited|inc\\.?|llc\\.?|corp\\.?|corporation|co\\.?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[*#,_\\-'\"@!&+/\\\\]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public String normalize(String merchantName) {
        if (merchantName == null) {
            return "";
        }

        String normalized = merchantName.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return "";
        }

        // 1. Remove URL prefix like https:// or www.
        normalized = URL_PREFIX_PATTERN.matcher(normalized).replaceAll("");

        // 2. Remove common domain suffixes like .com, .in, etc.
        normalized = DOMAIN_SUFFIX_PATTERN.matcher(normalized).replaceAll("");

        // 3. Remove common corporate suffixes
        normalized = CORP_SUFFIX_PATTERN.matcher(normalized).replaceAll("");

        // 4. Remove punctuation
        normalized = PUNCTUATION_PATTERN.matcher(normalized).replaceAll(" ");

        // 5. Collapse multiple spaces and trim
        normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ").trim();

        return normalized;
    }
}
