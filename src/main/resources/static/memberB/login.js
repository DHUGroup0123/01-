'use strict';
(() => {
  const form=document.querySelector('#login-form'),button=form.querySelector('[type=submit]');let pending=false;
  const refresh=()=>{document.querySelector('#captcha-image').src='/admin/b/captcha?t='+Date.now();};
  document.querySelector('#refresh-captcha').onclick=refresh;
  form.onsubmit=async event=>{event.preventDefault();if(pending)return;pending=true;button.disabled=true;button.textContent='正在登录…';document.querySelector('#login-error').textContent='';
    try{const response=await fetch('/admin/b/login',{method:'POST',headers:{'Content-Type':'application/json','X-CSRF-Token':document.querySelector('meta[name=csrf-token]').content},body:JSON.stringify(Object.fromEntries(new FormData(form)))});const result=await response.json();if(!response.ok)throw Error(result.message||'登录失败');location.href=result.data.redirect;}
    catch(error){document.querySelector('#login-error').textContent=error.message;refresh();form.elements.captcha.value='';}
    finally{pending=false;button.disabled=false;button.textContent='登录工作台';}
  };
})();
