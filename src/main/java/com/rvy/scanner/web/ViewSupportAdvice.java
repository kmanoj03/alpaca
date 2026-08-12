package com.rvy.scanner.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewSupportAdvice {

    private final FormatSupport formatSupport;

    public ViewSupportAdvice(FormatSupport formatSupport) {
        this.formatSupport = formatSupport;
    }

    @ModelAttribute("fmt")
    public FormatSupport fmt() {
        return formatSupport;
    }
}
