package com.team.team_work.memberA.mall.service;
import com.team.team_work.memberA.mall.dto.*;
import com.team.team_work.memberA.mall.entity.*;
import com.team.team_work.memberA.mall.mapper.CatalogMapper;
import com.team.team_work.memberA.user.service.BusinessException;
import org.springframework.stereotype.Service;
import java.util.*;
import java.io.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.ParserDelegator;

@Service
public class CatalogService {
    private final CatalogMapper mapper;
    public CatalogService(CatalogMapper mapper) {this.mapper=mapper;}
    public CatalogQuery query(String keyword,Long categoryId,String sort,int page,int pageSize) {
        String word=keyword==null?"":keyword.trim();String order=sort==null?"latest":sort;
        if(word.length()>50 || page<1 || page>100000 || pageSize<1 || pageSize>50 || (categoryId!=null&&categoryId<=0) || !Set.of("latest","priceAsc","priceDesc").contains(order)) throw new BusinessException(400,"搜索参数不正确，请检查关键词、分类、排序或页码");
        String pattern="%"+word.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        return new CatalogQuery(word,pattern,categoryId,order,page,pageSize,(long)(page-1)*pageSize);
    }
    public CatalogViews.Page<CatalogViews.Product> search(CatalogQuery q) {
        long total=mapper.count(q);
        return new CatalogViews.Page<>(mapper.search(q).stream().map(this::view).toList(),q.page(),q.pageSize(),total,(total+q.pageSize()-1)/q.pageSize());
    }
    public CatalogViews.Product product(Long id) {
        CatalogProduct product=mapper.product(id);
        if(product==null) throw new BusinessException(404,"商品不存在或已下架");
        return view(product);
    }
    public List<CatalogViews.Category> categories(Long parentId) {return tree(mapper.categories(),parentId,0);}
    private List<CatalogViews.Category> tree(List<CatalogCategory> source,Long parentId,int depth) {
        if(depth>=3) return List.of();
        return source.stream().filter(c->Objects.equals(c.getParentId(),parentId)).map(c->new CatalogViews.Category(c.getCategoryId().toString(),c.getParentId()==null?null:c.getParentId().toString(),c.getCategoryName(),c.getCategoryLevel(),tree(source,c.getCategoryId(),depth+1))).toList();
    }
    public CatalogViews.Home home() {
        return new CatalogViews.Home(categories(null),mapper.carousels().stream().map(c->new CatalogViews.Carousel(c.getCarouselId().toString(),image(c.getImageUrl()),link(c.getRedirectUrl()))).toList(),configured(1),configured(2),configured(3));
    }
    private List<CatalogViews.Product> configured(int type) {return mapper.configured(type).stream().map(this::view).toList();}
    private CatalogViews.Product view(CatalogProduct p) {
        return new CatalogViews.Product(p.getProductId().toString(),p.getCategoryId().toString(),p.getProductName(),p.getSummary(),image(p.getCoverUrl()),plain(p.getDetailHtml()),p.getOriginalPrice().setScale(2).toPlainString(),p.getSellingPrice().setScale(2).toPlainString(),p.getStock());
    }
    private String image(String value) {return value!=null&&value.matches("/(images|uploads)/[a-zA-Z0-9_./-]+")&&!value.contains("..")?value:"/images/01-product.svg";}
    private String link(String value) {return value!=null&&value.matches("/(search|products/[0-9]+)(\\?[a-zA-Z0-9=&%_-]*)?")?value:"/search";}
    private String plain(String html) {
        if(html==null||html.isBlank()) return "商品详细说明待维护，请以商品名称、价格和库存信息为准。";
        StringBuilder text=new StringBuilder();
        try {new ParserDelegator().parse(new StringReader(html),new HTMLEditorKit.ParserCallback(){
            private int skip;
            @Override public void handleStartTag(HTML.Tag t,javax.swing.text.MutableAttributeSet a,int pos){if(t==HTML.Tag.STYLE||t==HTML.Tag.SCRIPT)skip++;}
            @Override public void handleEndTag(HTML.Tag t,int pos){if((t==HTML.Tag.STYLE||t==HTML.Tag.SCRIPT)&&skip>0)skip--;if(t==HTML.Tag.P||t==HTML.Tag.DIV)text.append('\n');}
            @Override public void handleText(char[] data,int pos){if(skip==0)text.append(data).append(' ');}
        },true);} catch(IOException e){throw new IllegalStateException(e);}
        return text.toString().trim();
    }
}
