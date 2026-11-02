package com.team.team_work.memberA.mall.entity;

public class CatalogCategory {
    private Long categoryId;
    private Long parentId;
    private String categoryName;
    private Integer categoryLevel;
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { categoryId=value; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long value) { parentId=value; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String value) { categoryName=value; }
    public Integer getCategoryLevel() { return categoryLevel; }
    public void setCategoryLevel(Integer value) { categoryLevel=value; }
}
