package com.aequitas.aequitascentralservice.adapter.web.exception;

import org.springframework.http.ProblemDetail;

public interface IErrorMessage {
    String getMessage();
    void setMessage(String message);

    StackTraceElement[] getStackTrace();
    void setStackTrace(StackTraceElement[] stackTrace);
    
    ThrowableInfo getThrowable();
    void setThrowable(ThrowableInfo throwable);
    

    String getError();
    void setError(String errorDetail);
    
    ProblemDetail getProblemDetail();
    void setProblemDetail(ProblemDetail problemDetail);
  
}
