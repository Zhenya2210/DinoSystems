package org.evgen.dinosystems.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ExceptionHandler
    public ResponseEntity<String> handleConflict(Exception exception) {
        return ResponseEntity.badRequest().body("Invalid query");
    }
}
