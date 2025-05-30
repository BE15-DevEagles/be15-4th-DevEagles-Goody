package com.deveagles.be15_deveagles_be.features.todolist.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum TodoErrorCode implements ErrorCodeType {
  INVALID_TODO_DATE("50001", "시작일은 마감일보다 빠를 수 없습니다.", HttpStatus.BAD_REQUEST),
  TODO_NOT_FOUND("50002", "할 일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  TODO_ALREADY_COMPLETED("50003", "이미 완료된 할 일입니다.", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED_TODO_ACCESS("50004", "해당 할 일에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
