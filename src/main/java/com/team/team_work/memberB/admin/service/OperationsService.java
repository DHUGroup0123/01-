package com.team.team_work.memberB.admin.service;
import com.team.team_work.memberB.admin.entity.*;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import com.team.team_work.memberB.product.entity.Product;
import com.team.team_work.memberB.product.service.CatalogService;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.team.team_work.memberB.product.service.CatalogService.*;
@Service
public class OperationsService {
 private final AdminMapper mapper; private final CatalogService catalog;
 public OperationsService(AdminMapper mapper,CatalogService catalog){this.mapper=mapper;this.catalog=catalog;}
 public Map<String,Object> members(String q,int page,int size){pagination(page,size);optional(q,100,"关键词");return Map.of("rows",mapper.members(q,(page-1)*size,size),"records",mapper.memberCount(q),"page",page,"size",size);}
 public Member memberStatus(long id,int locked){flag(locked);require(mapper.member(id),"会员不存在");mapper.lockMember(id,locked);return mapper.member(id);}
 public List<Carousel> carousels(){return mapper.carousels();}
 public Carousel saveCarousel(Carousel c){
  image(c.carouselUrl);c.redirectUrl=link(c.redirectUrl);rank(c.carouselRank);flag(c.isDeleted);
  if(c.carouselId==null)mapper.insertCarousel(c);else{require(mapper.carousel(c.carouselId),"轮播不存在");mapper.updateCarousel(c);}return mapper.carousel(c.carouselId);
 }
 public Object deleteCarousel(long id){require(mapper.carousel(id),"轮播不存在");mapper.deleteCarousel(id);return Map.of("id",id);}
 public List<HomeConfig> configs(){return mapper.configs();}
 public HomeConfig saveConfig(HomeConfig c){
  c.configName=text(c.configName,50,"配置名称");rank(c.configRank);flag(c.isDeleted);c.redirectUrl=link(c.redirectUrl);
  if(c.configType==null || c.configType<3 || c.configType>5)throw BusinessException.bad("类型为3热销、4新品、5推荐");
  Product p=catalog.product(c.goodsId==null?0:c.goodsId);
  if(c.isDeleted==0 && (p.goodsSellStatus!=0 || p.stockNum<=0))throw BusinessException.bad("仅有库存的上架商品可配置首页");
  boolean duplicate=configs().stream().anyMatch(x->!Objects.equals(x.configId,c.configId) && x.goodsId.equals(c.goodsId) && x.configType.equals(c.configType));
  if(duplicate)throw new BusinessException(409,"同一类型已配置此商品");
  if(c.configId==null)mapper.insertConfig(c);else{require(mapper.config(c.configId),"配置不存在");mapper.updateConfig(c);}return mapper.config(c.configId);
 }
 public Object deleteConfig(long id){require(mapper.config(id),"配置不存在");mapper.deleteConfig(id);return Map.of("id",id);}
}
