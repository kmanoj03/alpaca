package com.rvy.scanner.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.rvy.scanner.controller.HistoryController;
import com.rvy.scanner.controller.ScannerController;

@ControllerAdvice(assignableTypes = {ScannerController.class, HistoryController.class})
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
