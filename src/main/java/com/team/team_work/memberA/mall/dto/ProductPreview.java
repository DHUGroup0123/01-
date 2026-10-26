package com.team.team_work.memberA.mall.dto;

import java.math.BigDecimal;

/** Read-only display contract; product writes belong to member B. */
public class ProductPreview {
    private Long productId;
    private String productName;
    private String summary;
    private BigDecimal sellingPrice;
    private Integer stock;
    public Long getProductId() { return productId; }
    public void setProductId(Long value) { productId = value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { productName = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal value) { sellingPrice = value; }
    public Integer getStock() { return stock; }
    public void setStock(Integer value) { stock = value; }
}
