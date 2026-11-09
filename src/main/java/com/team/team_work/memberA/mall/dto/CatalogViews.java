package com.team.team_work.memberA.mall.dto;
import java.util.List;

public final class CatalogViews {
    private CatalogViews() { }
    public record Product(String productId,String categoryId,String productName,String summary,String coverUrl,String detailText,String originalPrice,String sellingPrice,int stock) { }
    public record Category(String categoryId,String parentId,String categoryName,int level,List<Category> children) { }
    public record Carousel(String carouselId,String imageUrl,String redirectUrl) { }
    public record Page<T>(List<T> list,int page,int pageSize,long total,long totalPages) { }
    public record Home(List<Category> categories,List<Carousel> carousels,List<Product> hot,List<Product> latest,List<Product> recommended) { }
}
