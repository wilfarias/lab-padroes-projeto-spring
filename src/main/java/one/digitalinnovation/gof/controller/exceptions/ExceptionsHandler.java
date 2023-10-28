package one.digitalinnovation.gof.controller.exceptions;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import one.digitalinnovation.gof.service.exceptions.ClienteNotfoundException;

/*Permite que a classe intercepte alguma exceção que
 * ocorrer na camada de Controller */

@ControllerAdvice
public class ExceptionsHandler {
	
	@ExceptionHandler(ClienteNotfoundException.class)
	public ResponseEntity<StandardError> entityNotFound(ClienteNotfoundException e, HttpServletRequest request){
		
		HttpStatus status = HttpStatus.NOT_FOUND;
		
		StandardError error = new StandardError();
		error.setError("Resource not found");
		error.setMessage(e.getMessage());
		error.setPath(request.getRequestURI());
		error.setStatus(status.value());
		error.setTimestamp(Instant.now());
		
		return ResponseEntity.status(status).body(error);
		
	}

}
