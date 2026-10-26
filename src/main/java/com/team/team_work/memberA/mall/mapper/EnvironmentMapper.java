package com.team.team_work.memberA.mall.mapper;

import com.team.team_work.memberA.mall.dto.ProductPreview;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface EnvironmentMapper {
    String schemaVersion();
    int countProducts();
    int countCategories();
    List<ProductPreview> findPreviewProducts();
}
