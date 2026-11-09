package com.team.team_work.memberA.mall.mapper;
import com.team.team_work.memberA.mall.dto.CatalogQuery;
import com.team.team_work.memberA.mall.entity.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CatalogMapper {
    List<CatalogCategory> categories();
    List<CatalogCarousel> carousels();
    List<CatalogProduct> configured(@Param("type") int type);
    long count(@Param("q") CatalogQuery query);
    List<CatalogProduct> search(@Param("q") CatalogQuery query);
    CatalogProduct product(@Param("id") Long id);
}
