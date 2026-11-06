package com.team.team_work.memberB.product.service;
import com.team.team_work.memberB.product.mapper.CatalogMapper;
import com.team.team_work.memberB.admin.mapper.AdminMapper;
import com.team.team_work.memberB.admin.service.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
/** Member C calls these methods inside its order transaction. No public stock-mutation HTTP API. */
@Service
public class InventoryService {
 private final CatalogMapper mapper;private final AdminMapper admin;
 public InventoryService(CatalogMapper mapper,AdminMapper admin){this.mapper=mapper;this.admin=admin;}
 @Transactional public void reserve(String orderLineId,long goodsId,int quantity){change(orderLineId,goodsId,quantity,false);}
 @Transactional public void release(String orderLineId,long goodsId,int quantity){change(orderLineId,goodsId,quantity,true);}
 private void change(String id,long goodsId,int quantity,boolean release){
  if(id==null || !id.matches("[a-zA-Z0-9_-]{1,90}") || quantity<1)throw BusinessException.bad("库存业务编号或数量不合法");
  admin.guard();String event=(release?"release:":"reserve:")+id;
  Map<String,Object> old=mapper.event(event);
  if(old!=null){check(old,goodsId,quantity);return;}
  if(release){Map<String,Object> reserved=mapper.event("reserve:"+id);if(reserved==null)throw new BusinessException(409,"没有对应的库存预占记录");check(reserved,goodsId,quantity);}
  int changed=release?mapper.release(goodsId,quantity):mapper.reserve(goodsId,quantity);
  if(changed!=1)throw new BusinessException(409,release?"库存归还失败":"商品下架、不存在或库存不足");
  mapper.stockEvent(event,goodsId,quantity,release?"release":"reserve");
 }
 private void check(Map<String,Object> row,long id,int quantity){
  Object gid=row.entrySet().stream().filter(e->e.getKey().equalsIgnoreCase("goodsId")).findFirst().orElseThrow().getValue();
  Object qty=row.entrySet().stream().filter(e->e.getKey().equalsIgnoreCase("quantity")).findFirst().orElseThrow().getValue();
  if(((Number)gid).longValue()!=id || ((Number)qty).intValue()!=quantity)throw new BusinessException(409,"业务编号已用于不同库存操作");
 }
}
