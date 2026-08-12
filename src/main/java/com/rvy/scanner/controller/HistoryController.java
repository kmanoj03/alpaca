package com.rvy.scanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.rvy.scanner.service.HistoryService;

@Controller
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public String history(Model model) {
        model.addAttribute("scans", historyService.loadEvaluated());
        return "history";
    }
}
