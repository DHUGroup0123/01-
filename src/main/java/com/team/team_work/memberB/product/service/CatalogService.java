package com.team.team_work.memberB.product.service;

import com.team.team_work.memberB.product.entity.*;
import com.team.team_work.memberB.product.mapper.CatalogMapper;
import com.team.team_work.memberB.admin.service.BusinessException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CatalogService {
    private final CatalogMapper mapper;
    public CatalogService(CatalogMapper mapper){this.mapper=mapper;}
    public List<Category> categories(){return mapper.categories();}
    public Map<String,Object> tree(){
        List<Category> rows=categories();
        return Map.of("rows",rows,"roots",rows.stream().filter(c->c.parentId==0).map(c->node(c,rows)).toList());
    }
    private Map<String,Object> node(Category c,List<Category> rows){
        return Map.of("category",c,"children",rows.stream().filter(x->x.parentId.equals(c.categoryId)).map(x->node(x,rows)).toList());
    }
    public Category category(long id){return require(mapper.category(id),"分类不存在");}
    public Product product(long id){return require(mapper.product(id),"商品不存在");}
    public Map<String,Object> products(String q,Long categoryId,Integer status,int page,int size){
        pagination(page,size);
        if(status!=null) flag(status);
        if(q!=null && q.length()>100)throw BusinessException.bad("关键词过长");
        return Map.of("rows",mapper.products(q,categoryId,status,(page-1)*size,size),"records",mapper.count(q,categoryId,status),"page",page,"size",size);
    }
    public Category saveCategory(Category c){
        c.categoryName=text(c.categoryName,50,"分类名称");rank(c.categoryRank);
        if(c.categoryId==null){
            if(c.parentId==null)throw BusinessException.bad("请选择父分类，顶级分类为0");
            c.categoryLevel=c.parentId==0?1:category(c.parentId).categoryLevel+1;
            if(c.categoryLevel>3)throw BusinessException.bad("最多三级分类");
            boolean duplicate=categories().stream().anyMatch(x->x.parentId.equals(c.parentId) && x.categoryName.equals(c.categoryName));
            if(duplicate)throw new BusinessException(409,"同级分类名称已存在");
            mapper.insertCategory(c);
        }else{
            Category old=category(c.categoryId);
            if(c.parentId!=null && !old.parentId.equals(c.parentId))throw BusinessException.bad("不支持移动分类，请保留原父分类");
            boolean duplicate=categories().stream().anyMatch(x->!x.categoryId.equals(c.categoryId) && x.parentId.equals(old.parentId) && x.categoryName.equals(c.categoryName));
            if(duplicate)throw new BusinessException(409,"同级分类名称已存在");
            mapper.updateCategory(c);
        }
        return category(c.categoryId);
    }
    public Object deleteCategory(long id){
        category(id);
        if(mapper.childCount(id)>0 || mapper.productCountInCategory(id)>0)throw new BusinessException(409,"分类仍有关联子分类或商品，禁止删除");
        mapper.deleteCategory(id);return Map.of("id",id);
    }
    public Product saveProduct(Product p){
        p.goodsName=text(p.goodsName,200,"商品名称");p.goodsIntro=optional(p.goodsIntro,200,"简介");
        p.tag=optional(p.tag,20,"标签");
        Category c=category(p.goodsCategoryId==null?0:p.goodsCategoryId);
        if(c.categoryLevel!=3)throw BusinessException.bad("商品必须关联三级分类");
        price(p.originalPrice);price(p.sellingPrice);
        if(p.sellingPrice.compareTo(p.originalPrice)>0)throw BusinessException.bad("售价不能高于原价");
        if(p.stockNum==null || p.stockNum<0)throw BusinessException.bad("库存必须为非负整数");
        image(p.goodsCoverImg);
        if(p.goodsCarousel==null || p.goodsCarousel.isBlank())p.goodsCarousel=p.goodsCoverImg;
        if(p.goodsCarousel.length()>2000)throw BusinessException.bad("轮播图片地址过长");
        for(String url:p.goodsCarousel.split(","))image(url.trim());
        // Rich text is edited as plain content and rendered with textContent/th:text, never raw HTML.
        p.goodsDetailContent=optional(p.goodsDetailContent,20000,"详情");
        if(p.goodsId==null){p.goodsSellStatus=1;p.version=0;mapper.insertProduct(p);}
        else{
            Product old=product(p.goodsId);
            if(p.version==null)throw BusinessException.bad("缺少商品版本，请刷新后重试");
            if(old.goodsSellStatus==0 && p.stockNum==0 && mapper.configCount(p.goodsId)>0)throw new BusinessException(409,"首页配置中的商品不能将库存改为0，请先停用配置");
            if(mapper.updateProduct(p)!=1)throw new BusinessException(409,"商品已被其他操作更新，请刷新");
        }
        return product(p.goodsId);
    }
    public Product status(long id,int status,int version){
        flag(status);Product p=product(id);
        if(status==0 && p.stockNum<=0)throw BusinessException.bad("无库存商品不能上架");
        if(status==1 && mapper.configCount(id)>0)throw new BusinessException(409,"商品仍有启用的首页配置，请先停用或删除配置");
        if(mapper.sellStatus(id,status,version)!=1)throw new BusinessException(409,"商品已被其他操作更新，请刷新");
        return product(id);
    }
    public static void pagination(int page,int size){if(page<1 || page>100000 || size<1 || size>100)throw BusinessException.bad("页码1至100000，每页1至100条");}
    public static String text(String s,int max,String label){if(s==null || s.isBlank() || s.trim().length()>max)throw BusinessException.bad(label+"不能为空且不得超过"+max+"字");return s.trim();}
    public static String optional(String s,int max,String label){if(s==null)return "";if(s.length()>max)throw BusinessException.bad(label+"过长");return s;}
    public static void flag(Integer flag){if(flag==null || (flag!=0 && flag!=1))throw BusinessException.bad("状态只能为0或1");}
    public static void rank(Integer rank){if(rank==null || rank<0 || rank>999999)throw BusinessException.bad("排序值须为0至999999");}
    public static void image(String url){if(url==null || !url.matches("/member-b/media/[a-f0-9]{64}\\.(png|jpg|gif)"))throw BusinessException.bad("请使用本系统上传的图片");}
    public static String link(String url){if(url==null || url.isBlank())return "/";if(url.length()>500 || !url.startsWith("/") || url.startsWith("//") || url.contains("\\") || url.contains("\r") || url.contains("\n"))throw BusinessException.bad("跳转链接须为站内路径");return url;}
    private void price(BigDecimal p){if(p==null || p.signum()<0 || p.compareTo(new BigDecimal("99999999.99"))>0 || p.stripTrailingZeros().scale()>2)throw BusinessException.bad("价格须为非负金额且最多两位小数");}
    public static <T>T require(T obj,String msg){if(obj==null)throw new BusinessException(404,msg);return obj;}
}
