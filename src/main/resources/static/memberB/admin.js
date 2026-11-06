'use strict';
(() => {
 const $=s=>document.querySelector(s),csrf=$('meta[name=csrf-token]').content;
 const names={products:'商品管理',categories:'分类管理',carousels:'轮播图',configs:'首页配置',members:'会员管理',profile:'个人设置'};
 const descriptions={products:'维护商品信息，让每一次上架都有据可查。',categories:'维护三级分类，关联商品的分类受到删除保护。',carousels:'管理首页轮播、排序与上线状态。',configs:'为热销、新品与推荐选择合适的商品。',members:'查询会员账号，管理禁用与解禁状态。',profile:'更新管理员资料与登录密码。'};
 let section='products',page=1,total=0,rows=[],categories=[],products=[],busy=false,editing=null,rich=null,loadId=0;
 const escape=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 const money=v=>'¥ '+Number(v).toFixed(2),pill=(ok,on,off)=>`<span class="status-pill ${ok?'':'off'}">${ok?on:off}</span>`;
 async function api(path,options={}) {
  const controller=new AbortController(),timer=setTimeout(()=>controller.abort(),20000);
  try{const response=await fetch('/admin/b/api/'+path,{...options,signal:controller.signal,headers:{'X-CSRF-Token':csrf,...options.headers}});
   const data=await response.json();if(!response.ok){if(response.status===401)location.href='/admin/b/login';throw Error(data.message||'操作失败');}return data.data;
  }catch(e){if(e.name==='AbortError')throw Error('请求超时，结果可能已保存。请使用相同内容重试，不要重复新建。');throw e;}finally{clearTimeout(timer);}
 }
 async function command(path,payload,method='POST') {
  const source=JSON.stringify([csrf,path,method,payload]);const hash=Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(source)))).map(b=>b.toString(16).padStart(2,'0')).join('');
  const storageKey='memberb-request:'+hash;let key=sessionStorage.getItem(storageKey);if(!key){key=crypto.randomUUID();sessionStorage.setItem(storageKey,key);}
  const result=await api(path,{method,headers:{'Content-Type':'application/json','Idempotency-Key':key},...(method==='DELETE'?{}:{body:JSON.stringify(payload)})});sessionStorage.removeItem(storageKey);return result;
 }
 function feedback(message,error=false){$('#feedback').textContent=message;$('#feedback').style.color=error?'#b94343':'#54805d';}
 async function withBusy(button,fn){if(busy)return;busy=true;const label=button.textContent;button.disabled=true;button.textContent='处理中…';try{await fn();}catch(e){feedback(e.message,true);}finally{busy=false;button.disabled=false;button.textContent=label;}}
 const action=(name,id,label)=>`<button class="plain-button" data-action="${name}" data-id="${id}">${label}</button>`;
 function render() {
  let heads=[],body=[];
  if(section==='products'){
   heads=['商品信息','分类','售价 / 原价','库存','状态','操作'];body=rows.map(r=>`<tr><td><div class="product-cell"><img src="${escape(r.goodsCoverImg)}" alt=""><div><strong>${escape(r.goodsName)}</strong><small>#${r.goodsId} · ${escape(r.goodsIntro)}</small></div></div></td><td>${escape(categories.find(c=>c.categoryId===r.goodsCategoryId)?.categoryName||r.goodsCategoryId)}</td><td><span class="price">${money(r.sellingPrice)}</span><br><small class="muted">${money(r.originalPrice)}</small></td><td>${r.stockNum}</td><td>${pill(r.goodsSellStatus===0,'已上架','已下架')}</td><td>${action('edit',r.goodsId,'编辑')}${action('status',r.goodsId,r.goodsSellStatus===0?'下架':'上架')}</td></tr>`);
  }else if(section==='categories'){
   heads=['分类名称','ID','层级 / 父分类','排序','操作'];const ordered=[];const walk=id=>categories.filter(c=>c.parentId===id).forEach(c=>{ordered.push(c);walk(c.categoryId);});walk(0);rows=ordered;body=rows.map(r=>`<tr><td style="padding-left:${12+(r.categoryLevel-1)*28}px">${r.categoryLevel>1?'└　':''}${escape(r.categoryName)}</td><td>#${r.categoryId}</td><td>${r.categoryLevel}级 / ${r.parentId||'顶级'}</td><td>${r.categoryRank}</td><td>${action('edit',r.categoryId,'编辑')}${action('delete',r.categoryId,'删除')}</td></tr>`);
  }else if(section==='carousels'){
   heads=['轮播图','跳转地址','排序','状态','操作'];body=rows.map(r=>`<tr><td><div class="product-cell"><img src="${escape(r.carouselUrl)}" alt="轮播缩略图"><span>#${r.carouselId}</span></div></td><td>${escape(r.redirectUrl)}</td><td>${r.carouselRank}</td><td>${pill(r.isDeleted===0,'已上线','已下线')}</td><td>${action('edit',r.carouselId,'编辑')}${action('delete',r.carouselId,'删除')}</td></tr>`);
  }else if(section==='configs'){
   heads=['配置名称','类型','关联商品','排序','状态','操作'];body=rows.map(r=>`<tr><td>${escape(r.configName)}</td><td>${({3:'热销',4:'新品',5:'推荐'})[r.configType]}</td><td>#${r.goodsId} ${escape(products.find(p=>p.goodsId===r.goodsId)?.goodsName||'')}</td><td>${r.configRank}</td><td>${pill(r.isDeleted===0,'启用','停用')}</td><td>${action('edit',r.configId,'编辑')}${action('delete',r.configId,'删除')}</td></tr>`);
  }else if(section==='members'){
   heads=['会员ID','账号','昵称','状态','操作'];body=rows.map(r=>`<tr><td>#${r.userId}</td><td>${escape(r.loginName)}</td><td>${escape(r.nickName)}</td><td>${pill(r.lockedFlag===0,'正常','已禁用')}</td><td>${action('member',r.userId,r.lockedFlag===0?'禁用':'解禁')}</td></tr>`);
  }
  $('#table-head').innerHTML='<tr>'+heads.map(h=>`<th>${h}</th>`).join('')+'</tr>';
  $('#table-body').innerHTML=body.length?body.join(''):`<tr><td colspan="${heads.length}" class="text-center muted py-5">暂无数据</td></tr>`;
  $('#row-count').textContent='共 '+total+' 项';$('#page-info').textContent='第 '+page+' 页 / 共 '+Math.max(1,Math.ceil(total/10))+' 页';$('#previous').disabled=page<=1;$('#next').disabled=page*10>=total;
 }
 async function stats(){const [all,on,c,slides]=await Promise.all([api('products?size=1'),api('products?status=0&size=1'),api('categories'),api('carousels')]);$('#stat-products').textContent=all.records;$('#stat-online').textContent=on.records;$('#stat-categories').textContent=c.rows.length;$('#stat-carousels').textContent=slides.filter(x=>x.isDeleted===0).length;}
 async function load(){const generation=++loadId;feedback('正在加载…');try{
  if(section==='profile'){const data=await api('profile');$('#nick-name').value=data.nickName;feedback('');return;}
  let result;if(section==='categories'){result=await api('categories');categories=result.rows;rows=categories;total=rows.length;}
  else if(section==='products'||section==='members'){
   const query=new URLSearchParams({q:$('#query').value,page,size:10});if(section==='products'){if($('#category-filter').value)query.set('categoryId',$('#category-filter').value);if($('#status-filter').value)query.set('status',$('#status-filter').value);}
   result=await api(section+'?'+query);if(generation!==loadId)return;rows=result.rows;total=result.records;
  }else {result=await api(section);if(generation!==loadId)return;rows=result;total=rows.length;}
  if(generation!==loadId)return;render();feedback('');
 }catch(e){if(generation===loadId)feedback(e.message,true);}}
 function changeSection(next){if(busy)return;section=next;page=1;$('#query').value='';$('#category-filter').value='';$('#status-filter').value='';$('#page-title').textContent=names[next];$('#breadcrumb').textContent=names[next];$('#page-description').textContent=descriptions[next];$('#panel-title').textContent=next==='profile'?'账号设置':names[next].replace('管理','')+'列表';$('#add-item').hidden=['members','profile'].includes(next);$('#add-item').textContent='＋ 新增'+({products:'商品',categories:'分类',carousels:'轮播',configs:'配置'})[next];$('#search-form').hidden=!['products','members'].includes(next);$('#table-container').hidden=next==='profile';$('#profile-section').hidden=next!=='profile';$('#pager').hidden=!['products','members'].includes(next);$('#category-filter').hidden=next!=='products';$('#status-filter').hidden=next!=='products';$('#query').placeholder=next==='members'?'搜索账号或昵称':'搜索商品名称';document.querySelectorAll('[data-section]').forEach(b=>b.classList.toggle('active',b.dataset.section===next));load();}
 const options=(items,value,label)=>items.map(x=>({value:x[value],label:x[label]}));
 function schemas(){return {
 products:[['goodsName','商品名称','text',true],['goodsCategoryId','三级分类','select',true,options(categories.filter(c=>c.categoryLevel===3),'categoryId','categoryName')],['goodsIntro','商品简介','text'],['tag','标签','text'],['originalPrice','原价（元）','number',true],['sellingPrice','售价（元）','number',true],['stockNum','库存数量','number',true],['goodsCoverImg','商品封面（PNG/JPEG/GIF，≤5MB）','image',true],['goodsCarousel','轮播图片（逗号分隔的已上传地址）','textarea'],['goodsDetailContent','商品详情（按纯文本保存，防止脚本注入）','rich']],
 categories:[['categoryName','分类名称','text',true],['parentId','父分类（最多三级）','select',true,[{value:0,label:'顶级分类'},...categories.filter(c=>c.categoryLevel<3).map(c=>({value:c.categoryId,label:'　'.repeat(c.categoryLevel-1)+c.categoryName+' / '+c.categoryLevel+'级'}))]],['categoryRank','排序（越大越靠前）','number',true]],
 carousels:[['carouselUrl','轮播图（PNG/JPEG/GIF，≤5MB）','image',true],['redirectUrl','站内跳转路径','text',true],['carouselRank','排序（越大越靠前）','number',true],['isDeleted','状态','select',true,[{value:0,label:'上线'},{value:1,label:'下线'}]]],
 configs:[['configName','配置名称','text',true],['configType','类型','select',true,[{value:3,label:'热销'},{value:4,label:'新品'},{value:5,label:'推荐'}]],['goodsId','关联商品（仅可启用有库存的上架商品）','select',true,products.map(p=>({value:p.goodsId,label:'#'+p.goodsId+' '+p.goodsName+(p.goodsSellStatus===0?' / 上架':' / 下架')}))],['configRank','排序（越大越靠前）','number',true],['redirectUrl','站内跳转路径','text',true],['isDeleted','状态','select',true,[{value:0,label:'启用'},{value:1,label:'停用'}]]]
 };}
 async function openEditor(item=null){if(busy)return;if(section==='configs'){products=[];let current=1;while(true){const result=await api('products?page='+current+'&size=100');products.push(...result.rows);if(products.length>=result.records)break;current++;}}
  editing=item;$('#editor-error').textContent='';$('#editor-title').textContent=(item?'编辑':'新增')+names[section].replace('管理','');if(rich){rich.destroy();rich=null;}
  const defaults={originalPrice:0,sellingPrice:0,stockNum:1,parentId:0,categoryRank:0,carouselRank:0,configRank:0,configType:3,isDeleted:0,redirectUrl:'/'};
  $('#editor-fields').innerHTML=schemas()[section].map(([key,label,type,required,choices])=>{const value=item?.[key]??defaults[key]??'';let field='';
   if(type==='select')field=`<select id="field-${key}" name="${key}" class="form-control" ${required?'required':''} ${(item&&key==='parentId')?'disabled':''}>${choices.map(o=>`<option value="${escape(o.value)}" ${String(o.value)===String(value)?'selected':''}>${escape(o.label)}</option>`).join('')}</select>`;
   else if(type==='textarea')field=`<textarea id="field-${key}" name="${key}" class="form-control">${escape(value)}</textarea>`;
   else if(type==='rich')field='<div id="rich-toolbar"></div><div id="rich-editor"></div>';
   else if(type==='image')field=`<input id="field-${key}" name="${key}" class="form-control" value="${escape(value)}" readonly ${required?'required':''}><div class="upload-field mt-2"><input type="file" accept="image/png,image/jpeg,image/gif" data-upload="${key}" aria-label="上传${label}"><img id="preview-${key}" ${value?'src="'+escape(value)+'"':''} alt="图片预览" ${value?'':'hidden'}></div>`;
   else field=`<input id="field-${key}" name="${key}" class="form-control" type="${type}" value="${escape(value)}" ${type==='number'?'min="0" max="99999999.99" step="'+(['originalPrice','sellingPrice'].includes(key)?'.01':'1')+'"':'maxlength="200"'} ${required?'required':''}>`;
   return `<div class="${['image','textarea','rich'].includes(type)?'wide':''}"><label for="field-${key}">${escape(label)}${required?' *':''}</label>${field}</div>`;
  }).join('');$('#editor-dialog').showModal();
  if(section==='products'){rich=window.wangEditor.createEditor({selector:'#rich-editor',html:'<p>'+escape(item?.goodsDetailContent||'').replace(/\n/g,'</p><p>')+'</p>',config:{placeholder:'输入商品详情…',MENU_CONF:{}},mode:'simple'});window.wangEditor.createToolbar({editor:rich,selector:'#rich-toolbar',config:{toolbarKeys:['undo','redo']},mode:'simple'});}
 }
 function closeEditor(){if(!busy)$('#editor-dialog').close();}
 $('#editor-fields').onchange=async event=>{const input=event.target,key=input.dataset.upload;if(!key||!input.files[0])return;
  if(input.files[0].size>5*1024*1024){$('#editor-error').textContent='图片不能超过5MB';input.value='';return;}busy=true;$('#save-item').disabled=true;input.disabled=true;$('#editor-error').textContent='正在上传…';
  try{const form=new FormData();form.append('file',input.files[0]);const result=await api('upload',{method:'POST',body:form});$('#field-'+key).value=result.url;$('#preview-'+key).src=result.url;$('#preview-'+key).hidden=false;$('#editor-error').textContent='';}catch(e){$('#editor-error').textContent=e.message;}finally{busy=false;$('#save-item').disabled=false;input.disabled=false;}
 };
 $('#editor-form').onsubmit=async event=>{event.preventDefault();if(busy)return;busy=true;const button=$('#save-item');button.disabled=true;button.textContent='保存中…';$('#editor-error').textContent='';
  try{const payload=editing?{...editing}:{};for(const [key,,type]of schemas()[section]){if(type==='rich'){payload[key]=rich.getText();continue;}const value=$('#field-'+key).value;payload[key]=['select','number'].includes(type)?Number(value):value;}
   await command(section,payload);$('#editor-dialog').close();await load();await stats();feedback('已保存');
  }catch(e){$('#editor-error').textContent=e.message;}finally{busy=false;button.disabled=false;button.textContent='保存';}
 };
 $('#table-body').onclick=async event=>{const button=event.target.closest('button[data-action]');if(!button||busy)return;const id=Number(button.dataset.id),key=({products:'goodsId',categories:'categoryId',carousels:'carouselId',configs:'configId',members:'userId'})[section];const item=rows.find(x=>x[key]===id);if(!item)return;
  if(button.dataset.action==='edit'){try{await openEditor(item);}catch(e){feedback(e.message,true);}return;}
  const confirmation=await Swal.fire({title:'确认'+button.textContent+'？',text:section==='members'?'会员状态会立即更新；成员A需在登录和请求处理中检查该状态。':'操作完成后页面将刷新。',icon:'question',showCancelButton:true,confirmButtonText:'确认',cancelButtonText:'取消',confirmButtonColor:'#176c59'});if(!confirmation.isConfirmed)return;
  await withBusy(button,async()=>{if(button.dataset.action==='delete')await command(section+'/'+id,null,'DELETE');else if(section==='products')await command('products/'+id+'/status',{status:item.goodsSellStatus===0?1:0,version:item.version});else await command('members/'+id+'/status',{locked:item.lockedFlag===0?1:0});await load();await stats();feedback('操作成功');});
 };
 $('#navigation').onclick=e=>{const button=e.target.closest('[data-section]');if(button)changeSection(button.dataset.section);};
 $('#add-item').onclick=()=>openEditor().catch(e=>feedback(e.message,true));$('#close-editor').onclick=closeEditor;$('#cancel-editor').onclick=closeEditor;
 $('#editor-dialog').addEventListener('cancel',event=>{if(busy)event.preventDefault();});
 $('#search-form').onsubmit=e=>{e.preventDefault();page=1;load();};$('#reset-filter').onclick=()=>{$('#query').value='';$('#category-filter').value='';$('#status-filter').value='';page=1;load();};
 $('#previous').onclick=()=>{if(page>1){page--;load();}};$('#next').onclick=()=>{if(page*10<total){page++;load();}};
 $('#logout').onclick=e=>withBusy(e.target,async()=>{await api('logout',{method:'POST'});location.href='/admin/b/login';});
 $('#profile-form').onsubmit=e=>{e.preventDefault();withBusy(e.target.querySelector('button'),async()=>{await command('profile',{nickName:$('#nick-name').value});feedback('昵称已保存，刷新页面后显示新昵称');});};
 $('#password-form').onsubmit=e=>{e.preventDefault();if($('#new-password').value!==$('#confirm-password').value){feedback('两次输入的新密码不一致',true);return;}withBusy(e.target.querySelector('button'),async()=>{await command('password',{oldPassword:$('#old-password').value,newPassword:$('#new-password').value});location.href='/admin/b/login';});};
 async function initialize(){try{categories=(await api('categories')).rows;$('#category-filter').innerHTML='<option value="">全部三级分类</option>'+categories.filter(c=>c.categoryLevel===3).map(c=>`<option value="${c.categoryId}">${escape(c.categoryName)}</option>`).join('');await Promise.all([load(),stats()]);}catch(e){feedback(e.message,true);}}
 initialize();
})();
