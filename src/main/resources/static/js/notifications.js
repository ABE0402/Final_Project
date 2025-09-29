// /js/notifications.js
(function(){
  const $ = (sel, ctx=document) => ctx.querySelector(sel);
  const $$ = (sel, ctx=document) => Array.from(ctx.querySelectorAll(sel));

  const bell = $('#notif-bell');
  const badge = $('#notif-count');
  const panel = $('#notif-panel');
  const list = $('#notif-list');
  const readAllBtn = $('#notif-read-all');

  if(!bell || !badge || !panel || !list) return;

  function getCsrf() {
    const t = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const h = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';
    return { header: h, token: t };
  }

  async function fetchJSON(url, opt={}) {
    const {header, token} = getCsrf();
    const headers = Object.assign({'Accept':'application/json'}, opt.headers||{});
    if (token) headers[header] = token;
    const res = await fetch(url, Object.assign({credentials:'same-origin'}, opt, {headers}));
    if (!res.ok) throw new Error('HTTP '+res.status);
    return res.json().catch(()=> ({}));
  }

  async function updateCount(){
    try {
      const data = await fetchJSON('/api/notifications/unread-count');
      const c = Number(data.count || 0);
      if (c > 0) {
        badge.textContent = String(c);
        badge.classList.remove('hidden');
      } else {
        badge.textContent = '0';
        badge.classList.add('hidden');
      }
    } catch(e) { /* ignore */ }
  }

  function renderItems(items){
    if (!items || items.length === 0) {
      list.innerHTML = '<div class="p-3 text-muted small">새 알림이 없습니다.</div>';
      return;
    }
    list.innerHTML = items.map(it => {
      const readClass = it.read ? 'notif-read' : 'notif-unread';
      const time = new Date(it.createdAt).toLocaleString();
      const link = it.link || '#';
      return `
        <div class="notif-item ${readClass}" data-id="${it.id}">
          <a href="${link}" class="notif-link">${it.message}</a>
          <div class="notif-time">${time}</div>
        </div>
      `;
    }).join('');
    // 개별 읽음 처리
    $$('.notif-item .notif-link', list).forEach(a => {
      a.addEventListener('click', async (e) => {
        const item = e.currentTarget.closest('.notif-item');
        const id = item?.dataset?.id;
        if (!id) return; // 방어
        try { await fetchJSON(`/api/notifications/${id}/read`, {method:'POST'}); }
        catch(e){}
        // 배지 감소(대략적으로 1 줄이기)
        const current = Number(badge.textContent || '0');
        if (current > 0) badge.textContent = String(current - 1);
        if (Number(badge.textContent) <= 0) badge.classList.add('hidden');
        // 그대로 이동
      }, {capture:true});
    });
  }

  async function openPanel(){
    try {
      const items = await fetchJSON('/api/notifications/recent');
      renderItems(items);
    } catch(e) {
      list.innerHTML = '<div class="p-3 text-danger small">알림을 불러오지 못했습니다.</div>';
    }
    panel.classList.toggle('hidden', false);
    document.addEventListener('click', onDocClick);
  }

  function closePanel(){
    panel.classList.add('hidden');
    document.removeEventListener('click', onDocClick);
  }

  function onDocClick(e){
    if (!panel.contains(e.target) && e.target !== bell) closePanel();
  }

  bell.addEventListener('click', (e) => {
    e.stopPropagation();
    if (panel.classList.contains('hidden')) openPanel();
    else closePanel();
  });

  readAllBtn?.addEventListener('click', async () => {
    try {
      await fetchJSON('/api/notifications/mark-all-read', {method:'POST'});
      badge.textContent = '0';
      badge.classList.add('hidden');
      $$('.notif-item', list).forEach(n => {
        n.classList.remove('notif-unread'); n.classList.add('notif-read');
      });
    } catch(e){}
  });

  // 최초 1회 + 주기적 갱신(선택)
  updateCount();
  setInterval(updateCount, 60000);
})();
