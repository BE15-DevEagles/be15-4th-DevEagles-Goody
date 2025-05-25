package com.deveagles.be15_deveagles_be.features.worklog.command.application.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestUtil {

  private static final int DEFAULT_PAGE = 0; // 0부터 시작
  private static final int DEFAULT_SIZE = 10;
  private static final String DEFAULT_SORT_BY = "writtenAt"; // 예시 정렬 기준
  private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

  public static Pageable createPageRequest(Integer page, Integer size, String sortOption) {
    int pageNumber = (page == null || page < 1) ? DEFAULT_PAGE : page - 1;
    int pageSize = (size == null || size < 1) ? DEFAULT_SIZE : size;

    String sortBy = "writtenAt";
    Sort.Direction direction = DEFAULT_DIRECTION; // 기본은 내림차순

    if ("created".equalsIgnoreCase(sortOption)) {
      direction = Sort.Direction.ASC; // 방향만 오름차순
    }

    return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
  }
}
