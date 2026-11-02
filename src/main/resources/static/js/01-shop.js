'use strict';
(() => {
  let csrfToken = null;
  const message = (text, ok = false) => {
    const node = document.getElementById('form-message') || document.getElementById('global-message');
    node.textContent = text; node.classList.toggle('success', ok);
  };
  async function token() {
    const response = await fetch('/api/csrf', {credentials: 'same-origin', cache: 'no-store'});
    const result = await response.json();
    if (!response.ok || result.code !== 200) throw new Error(result.message || '无法获取页面凭证');
    csrfToken = result.data.token;
  }
  async function api(url, method = 'GET', body) {
    const headers = {'Accept': 'application/json'};
    if (method !== 'GET') { if (!csrfToken) await token(); headers['X-CSRF-TOKEN'] = csrfToken; }
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    const response = await fetch(url, {method, headers, credentials: 'same-origin', cache: 'no-store', body: body === undefined ? undefined : JSON.stringify(body)});
    let result;
    try { result = await response.json(); } catch (_) { throw new Error('服务暂不可用，请稍后再试'); }
    if (!response.ok || result.code !== 200) {
      if (response.status === 403) csrfToken = null;
      if (response.status === 401 && !url.endsWith('/login')) { window.location.assign('/login'); }
      throw new Error(result.message || '操作失败，请稍后重试');
    }
    return result.data;
  }
  function bindForm(id, action) {
    const form = document.getElementById(id); if (!form) return;
    form.addEventListener('submit', async (event) => {
      event.preventDefault(); const button = form.querySelector('[type=submit]');
      if (button.disabled) return; button.disabled = true; message('正在处理…');
      try { await action(form); } catch (error) { message(error.message); } finally { button.disabled = false; }
    });
  }
  const formValue = (form, name) => form.elements.namedItem(name).value;
  document.querySelectorAll('[data-logout]').forEach(button => button.addEventListener('click', async () => {
    button.disabled = true;
    try { await api('/api/users/logout', 'POST'); window.location.assign('/login'); }
    catch (error) {message(error.message);button.disabled=false;}
  }));
  const captcha = document.getElementById('captcha-image');
  function refreshCaptcha() { if (captcha) captcha.src = '/api/captcha?t=' + Date.now(); }
  document.querySelector('.captcha-refresh')?.addEventListener('click', refreshCaptcha);
  const page = document.body.dataset.page;
  if (page === 'login' || page === 'register') {
    if (new URLSearchParams(window.location.search).get('registered') === '1') message('注册成功，请使用新账号登录。', true);
    bindForm('auth-form', async form => {
      if (page === 'register' && formValue(form, 'password') !== formValue(form, 'confirmPassword')) throw new Error('两次输入的密码不一致');
      try {
        await api('/api/users/' + (page === 'register' ? 'register' : 'login'), 'POST', {
          loginName: formValue(form, 'loginName'), password: formValue(form, 'password'), captcha: formValue(form, 'captcha')
        });
        csrfToken = null;
        window.location.assign(page === 'register' ? '/login?registered=1' : '/personal');
      } catch (error) {refreshCaptcha();form.elements.namedItem('captcha').value='';throw error;}
    });
  }
  if (page === 'personal') {
    api('/api/users/me').then(user => {const f = document.getElementById('profile-form'); ['loginName','nickname','phone'].forEach(k => f.elements.namedItem(k).value=user[k] || '');}).catch(error => message(error.message));
    bindForm('profile-form', async form => {await api('/api/users/me', 'PUT', {nickname: formValue(form,'nickname'), phone: formValue(form,'phone')});message('个人资料已保存。',true);});
  }
  if (page === 'addresses') {
    const form = document.getElementById('address-form');
    const reset = () => {form.reset();form.elements.namedItem('addressId').value='';document.getElementById('editor-title').textContent='新增地址';};
    document.getElementById('cancel-edit').addEventListener('click', () => {reset();message('');});
    const text = (tag, value, className) => {const n=document.createElement(tag);n.textContent=value;if(className)n.className=className;return n;};
    async function load() {
      const rows=await api('/api/addresses');const list=document.getElementById('address-list');list.replaceChildren();
      if(!rows.length) list.append(text('p','还没有收货地址，在下方添加第一条吧。','empty'));
      rows.forEach(address => {
        const box=document.createElement('article');box.className='address-card';
        box.append(text('h3',address.receiverName+' · '+address.receiverPhone));
        if(address.isDefault) box.append(text('span','默认地址','badge'));
        box.append(text('p',address.province+' '+address.city+' '+address.district+' '+address.detailAddress));
        const actions=document.createElement('div');actions.className='actions';
        const edit=text('button','编辑','secondary');edit.type='button';edit.addEventListener('click',()=>{
          Object.entries(address).forEach(([key,value]) => {const field=form.elements.namedItem(key);if(field){if(field.type==='checkbox')field.checked=value;else field.value=value;}});
          document.getElementById('editor-title').textContent='编辑地址';form.scrollIntoView({behavior:'smooth',block:'center'});form.elements.namedItem('receiverName').focus();message('');
        });
        const remove=text('button','删除','danger-link');remove.type='button';remove.addEventListener('click',async()=>{
          if(!window.confirm('确定删除这条收货地址吗？'))return;
          remove.disabled=true;
          try{await api('/api/addresses/'+encodeURIComponent(address.addressId),'DELETE');reset();await load();message('地址已删除。',true);}catch(error){message(error.message);}finally{remove.disabled=false;}
        });
        actions.append(edit,remove);box.append(actions);list.append(box);
      });
    }
    load().catch(error=>message(error.message));
    bindForm('address-form',async f=>{
      const addressId=formValue(f,'addressId');const data={};
      ['receiverName','receiverPhone','province','city','district','detailAddress'].forEach(k=>data[k]=formValue(f,k));data.isDefault=f.elements.namedItem('isDefault').checked;
      await api('/api/addresses'+(addressId?'/'+encodeURIComponent(addressId):''),addressId?'PUT':'POST',data);reset();await load();message('收货地址已保存。',true);
    });
  }
  const slides=Array.from(document.querySelectorAll('.slide'));let slideIndex=0,paused=window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const pause=document.getElementById('pause-carousel');
  function showSlide(step){slideIndex=(slideIndex+step+slides.length)%slides.length;slides.forEach((s,i)=>{s.classList.toggle('active',i===slideIndex);s.setAttribute('aria-hidden',i===slideIndex?'false':'true');s.tabIndex=i===slideIndex?0:-1;});const p=document.getElementById('slide-position');if(p)p.textContent=(slideIndex+1)+' / '+slides.length;}
  if(slides.length>1){showSlide(0);document.querySelectorAll('[data-slide]').forEach(b=>b.addEventListener('click',()=>showSlide(Number(b.dataset.slide))));if(pause){pause.textContent=paused?'播放':'暂停';pause.addEventListener('click',()=>{paused=!paused;pause.textContent=paused?'播放':'暂停';pause.setAttribute('aria-label',paused?'播放轮播':'暂停轮播');});}window.setInterval(()=>{if(!paused&&!document.hidden)showSlide(1);},6000);}
  document.addEventListener('error',event=>{const image=event.target;if(image instanceof HTMLImageElement&&!image.src.includes('01-product.svg')&&image.id!=='captcha-image')image.src='/images/01-product.svg';},true);
})();
