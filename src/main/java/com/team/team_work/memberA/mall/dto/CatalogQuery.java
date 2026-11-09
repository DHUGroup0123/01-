package com.team.team_work.memberA.mall.dto;
public record CatalogQuery(String keyword,String pattern,Long categoryId,String sort,int page,int pageSize,long offset) { }
