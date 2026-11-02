package com.team.team_work.memberA.mall.entity;

public class CatalogProduct {
    private Long productId;
    private Long categoryId;
    private String productName;
    private String summary;
    private String coverUrl;
    private String detailHtml;
    private java.math.BigDecimal originalPrice;
    private java.math.BigDecimal sellingPrice;
    private Integer stock;
    public Long getProductId() { return productId; }
    public void setProductId(Long value) { productId=value; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long value) { categoryId=value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { productName=value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary=value; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String value) { coverUrl=value; }
    public String getDetailHtml() { return detailHtml; }
    public void setDetailHtml(String value) { detailHtml=value; }
    public java.math.BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(java.math.BigDecimal value) { originalPrice=value; }
    public java.math.BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(java.math.BigDecimal value) { sellingPrice=value; }
    public Integer getStock() { return stock; }
    public void setStock(Integer value) { stock=value; }
}
