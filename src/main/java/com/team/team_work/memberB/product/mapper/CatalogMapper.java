package com.team.team_work.memberB.product.mapper;
import com.team.team_work.memberB.product.entity.*;
import org.apache.ibatis.annotations.*;
import java.util.*;
@Mapper
public interface CatalogMapper {
 List<Category> categories();
 Category category(long id);
 int insertCategory(Category category);
 int updateCategory(Category category);
 int deleteCategory(long id);
 int childCount(long id);
 int productCountInCategory(long id);
 List<Product> products(@Param("q")String q,@Param("categoryId")Long categoryId,@Param("status")Integer status,@Param("offset")int offset,@Param("size")int size);
 long count(@Param("q")String q,@Param("categoryId")Long categoryId,@Param("status")Integer status);
 Product product(long id);
 int insertProduct(Product product);
 int updateProduct(Product product);
 int sellStatus(@Param("id")long id,@Param("status")int status,@Param("version")int version);
 int configCount(long id);
 int reserve(@Param("id")long id,@Param("quantity")int quantity);
 int release(@Param("id")long id,@Param("quantity")int quantity);
 int stockEvent(@Param("event")String event,@Param("id")long id,@Param("quantity")int quantity,@Param("kind")String kind);
 Map<String,Object> event(String event);
}
