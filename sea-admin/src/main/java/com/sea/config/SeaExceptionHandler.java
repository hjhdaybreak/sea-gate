package com.sea.config;

import com.sea.exception.SeaException;
import com.sea.pojo.vo.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class SeaExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<Void> handlerBusinessException(Exception exception) {
        return Result.error(transferToSeaException(exception));
    }

    private SeaException transferToSeaException(Exception exception) {
        SeaException seaException;
        if (exception instanceof SeaException) {
            seaException = (SeaException) exception;

        } else if (exception instanceof BindException) {
            BindException bindException = (BindException) exception;
            BindingResult bindingResult = bindException.getBindingResult();
            seaException = new SeaException(getErrorMsg(bindingResult));

        } else if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) exception;
            BindingResult bindingResult = validException.getBindingResult();
            seaException = new SeaException(getErrorMsg(bindingResult));

        } else {
            seaException = new SeaException(exception.getMessage());
        }
        return seaException;
    }

    private String getErrorMsg(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder();
        fieldErrors.forEach(fieldError -> {
            sb.append(fieldError.getDefaultMessage());
            sb.append("-");
        });
        return sb.toString();
    }


}
