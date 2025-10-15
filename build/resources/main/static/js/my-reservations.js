(function(){
  // 탭 전환
  const tabBtns = document.querySelectorAll('.time-period-btn');
  const tabCafe = document.getElementById('tab-cafe');
  const tabRest = document.getElementById('tab-restaurant');

  tabBtns.forEach(b=>{
    b.addEventListener('click', ()=>{
      tabBtns.forEach(x=>x.classList.remove('selected'));
      b.classList.add('selected');
      const t = b.dataset.tab;
      if(t==='cafe'){ tabCafe.classList.remove('hidden'); tabRest.classList.add('hidden'); }
      else { tabRest.classList.remove('hidden'); tabCafe.classList.add('hidden'); }
    });
  });

  // 취소 처리
  document.querySelectorAll('.cancel-btn').forEach(btn=>{
    btn.addEventListener('click', async ()=>{
      const id = btn.dataset.id;
      if(!confirm('이 예약을 취소하시겠습니까?')) return;

      const token = document.querySelector('meta[name="_csrf"]')?.content;
      const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
      const headers = { 'Content-Type':'application/json' }; if(token) headers[header]=token;

      const res = await fetch(`/api/reservations/${id}/cancel`, { method:'POST', headers });
      const data = await res.json().catch(()=>({}));
      if(res.ok){ alert('취소되었습니다.'); location.reload(); }
      else { alert(data.message || '취소에 실패했습니다.'); }
    });
  });
})();
