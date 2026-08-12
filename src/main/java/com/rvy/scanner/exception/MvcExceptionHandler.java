package com.rvy.scanner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.rvy.scanner.controller.ScannerController;
import com.rvy.scanner.web.StrategyParameterFactory;

@ControllerAdvice(assignableTypes = ScannerController.class)
public class MvcExceptionHandler {

    private final StrategyParameterFactory parameterFactory;

    public MvcExceptionHandler(StrategyParameterFactory parameterFactory) {
        this.parameterFactory = parameterFactory;
    }

    @ExceptionHandler({
            MissingCredentialsException.class,
            InvalidSymbolException.class,
            MissingDataException.class,
            AlpacaApiException.class
    })
    public ModelAndView handleKnown(RuntimeException ex) {
        return errorView(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnknown(Exception ex) {
        return errorView("Unexpected error: " + ex.getMessage());
    }

    private ModelAndView errorView(String message) {
        ModelAndView view = new ModelAndView("scanner");
        view.setStatus(HttpStatus.OK);
        view.addObject("error", message);
        view.addObject("symbol", "SPY");
        view.addObject("params", parameterFactory.defaults());
        return view;
    }
}
