package com.team.team_work.memberB;
import com.team.team_work.memberB.admin.service.CommandService;
import com.team.team_work.memberB.admin.service.BusinessException;
import com.team.team_work.memberB.product.mapper.CatalogMapper;
import com.team.team_work.memberB.product.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ConcurrentCommandTests {
 @Autowired CommandService commands;
 @Autowired CatalogMapper catalog;
 @Autowired InventoryService inventory;
 @Test void concurrentReservationsCannotOversell()throws Exception{
  var p=catalog.products("",null,null,0,1).get(0);int before=p.stockNum;
  String event=UUID.randomUUID().toString();ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
  List<String> reserved=Collections.synchronizedList(new ArrayList<>());
  try{List<Future<?>> jobs=new ArrayList<>();for(int i=0;i<2;i++){String id=event+"-"+i;jobs.add(pool.submit(()->{try{start.await();inventory.reserve(id,p.goodsId,before);reserved.add(id);}catch(BusinessException expected){assertEquals(409,expected.status);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}}));}start.countDown();for(var job:jobs)job.get(20,TimeUnit.SECONDS);assertEquals(1,reserved.size());assertEquals(0,catalog.product(p.goodsId).stockNum);}
  finally{for(String id:reserved)inventory.release(id,p.goodsId,before);pool.shutdownNow();}
 }
 @Test void simultaneousRetriesExecuteEffectOnce()throws Exception{
  String key=UUID.randomUUID().toString();AtomicInteger count=new AtomicInteger();CountDownLatch start=new CountDownLatch(1);ExecutorService pool=Executors.newFixedThreadPool(4);
  try{List<Future<?>> jobs=new ArrayList<>();for(int i=0;i<4;i++)jobs.add(pool.submit(()->{try{start.await();return commands.run(1,key,"same-intent",()->Map.of("count",count.incrementAndGet()));}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}}));start.countDown();for(Future<?> job:jobs)assertNotNull(job.get(20,TimeUnit.SECONDS));assertEquals(1,count.get());}finally{pool.shutdownNow();}
 }
}
