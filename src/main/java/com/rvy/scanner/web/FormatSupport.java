package com.rvy.scanner.web;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component("fmt")
public class FormatSupport {

    public String px(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    public String money(Double value) {
        if (value == null) {
            return "--";
        }
        return "$" + String.format(Locale.US, "%,.2f", value);
    }

    public String strike(double value) {
        if (value == Math.rint(value)) {
            return "$" + String.format(Locale.US, "%,.0f", value);
        }
        return "$" + String.format(Locale.US, "%,.2f", value);
    }

    public String theta(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.US, "%.4f", value);
    }

    public String delta(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.US, "%.2f", value);
    }

    public String iv(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.US, "%.1f%%", value * 100.0);
    }

    public String pct(double value) {
        return String.format(Locale.US, "%.1f%%", value * 100.0);
    }

    public String score(Double value) {
        if (value == null) {
            return "--";
        }
        return String.format(Locale.US, "%.1f", value);
    }
}
