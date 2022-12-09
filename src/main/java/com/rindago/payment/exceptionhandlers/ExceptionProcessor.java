package com.rindago.payment.exceptionhandlers;

import com.rindago.payment.exceptions.ExceptionResponse;
import com.rindago.payment.exceptions.RequirementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionProcessor {

  private final Logger logger = LoggerFactory.getLogger(ExceptionProcessor.class);

  /**
   * Process an exception and prepares the response to be returned once it's thrown
   * @param requirementException Thrown instance of the custom exception, RequirementException
   * @return Prepared response which is to be returned
   */
  @ExceptionHandler(RequirementException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ExceptionResponse handleRequirementException(RequirementException requirementException) {
    logger.error("REQUIREMENT ERROR!", requirementException);
    var exceptionResponse = new ExceptionResponse();
    exceptionResponse.setErrorCode(requirementException.getExceptionCode().name());
    exceptionResponse.setErrorMessage(requirementException.getExceptionCode().getExplanation());
    return exceptionResponse;
  }

}
