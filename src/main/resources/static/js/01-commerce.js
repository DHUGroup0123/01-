'use strict';
(()=>{
 const csrf=document.querySelector('meta[name="csrf-token"]')?.content;
 const show=text=>{const n=document.getElementById('commerce-message');if(n)n.textContent=text;};
 async function write(url,method,body){const response=await fetch(url,{method,credentials:'same-origin',headers:{'Content-Type':'application/json','X-CSRF-TOKEN':csrf},body:body===undefined?undefined:JSON.stringify(body)});if(response.redirected){window.location.assign(response.url);return;}const result=await response.json();if(!response.ok||(result.resultCode??result.code)!==200)throw new Error(result.message||'操作失败');return result;}
 function bind(selector,action){document.querySelectorAll(selector).forEach(button=>button.addEventListener('click',async()=>{button.disabled=true;try{await action(button);}catch(error){show(error.message);button.disabled=false;}}));}
 bind('[data-cart-update]',async b=>{const input=b.closest('tr').querySelector('.cart-quantity');const count=Number(input.value);if(!Number.isInteger(count)||count<1||count>5)throw new Error('数量需为1至5');await write('/shop-cart','PUT',{cartItemId:b.dataset.cartId,goodsCount:count});location.reload();});
 bind('[data-cart-delete]',async b=>{await write('/shop-cart/'+encodeURIComponent(b.dataset.cartId),'DELETE');location.reload();});
 bind('[data-order-action]',async b=>{await write('/orders/'+encodeURIComponent(b.dataset.orderNo)+'/'+b.dataset.orderAction,'PUT');location.reload();});
 bind('#demo-pay',async b=>{await write('/paySuccess?orderNo='+encodeURIComponent(b.dataset.orderNo)+'&payType='+b.dataset.payType,'POST');location.assign('/orders/'+encodeURIComponent(b.dataset.orderNo));});
})();
