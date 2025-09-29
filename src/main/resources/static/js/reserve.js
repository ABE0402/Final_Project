(function () {
  function csrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return { token, header };
  }

  const state = { partySize:null, selectedDate:null, selectedTime:null, period:'am' };

  // 인원
  const peopleBtns = document.querySelectorAll('.people-btn');
  const peopleInput = document.getElementById('people-input');
  peopleBtns.forEach(btn=>{
    btn.addEventListener('click', ()=>{
      peopleBtns.forEach(b=>b.classList.remove('selected'));
      btn.classList.add('selected');
      state.partySize = parseInt(btn.dataset.value,10);
      peopleInput.value = '';
    });
  });
  peopleInput.addEventListener('input', ()=>{
    peopleBtns.forEach(b=>b.classList.remove('selected'));
    const v = parseInt(peopleInput.value||'0',10);
    state.partySize = Number.isFinite(v)&&v>0 ? v : null;
  });

  // 달력
  const monthYear = document.getElementById('month-year-display');
  const grid = document.getElementById('calendar-grid');
  const prevBtn = document.querySelector('.calendar-header .prev');
  const nextBtn = document.querySelector('.calendar-header .next');
  let current = new Date(); current.setHours(0,0,0,0);

  function renderCalendar(date){
    grid.innerHTML='';
    const y=date.getFullYear(), m=date.getMonth();
    monthYear.textContent = `${y}. ${m+1}`;
    const first=new Date(y,m,1), start=first.getDay(), last=new Date(y,m+1,0).getDate();
    const days=['일','월','화','수','목','금','토'];
    days.forEach(d=>{ const s=document.createElement('span'); s.className='day-of-week'; s.textContent=d; grid.appendChild(s); });
    for(let i=0;i<start;i++){ const s=document.createElement('span'); s.className='day inactive'; grid.appendChild(s); }
    for(let d=1; d<=last; d++){
      const dt=new Date(y,m,d);
      const s=document.createElement('span');
      s.className='day'; s.textContent=String(d);
      const iso=`${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
      s.dataset.date=iso;
      if (dt < new Date().setHours(0,0,0,0)) s.classList.add('inactive');
      else s.addEventListener('click', ()=>{
        document.querySelectorAll('.day').forEach(x=>x.classList.remove('selected'));
        s.classList.add('selected'); state.selectedDate=iso;
      });
      if (d===new Date().getDate() && m===new Date().getMonth() && y===new Date().getFullYear()) s.classList.add('today');
      grid.appendChild(s);
    }
  }
  prevBtn.addEventListener('click', ()=>{ current=new Date(current.getFullYear(), current.getMonth()-1, 1); renderCalendar(current); });
  nextBtn.addEventListener('click', ()=>{ current=new Date(current.getFullYear(), current.getMonth()+1, 1); renderCalendar(current); });
  renderCalendar(current);

  // 시간
  const amSlots=document.getElementById('am-slots'), pmSlots=document.getElementById('pm-slots');
  const periodBtns = document.querySelectorAll('.time-period-btn');
  const AM=['10:00','11:00'], PM=['12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00'];
  function makeBtn(t){ const b=document.createElement('button'); b.className='time-btn'; b.dataset.time=t; b.textContent=t;
    b.addEventListener('click',()=>{ document.querySelectorAll('.time-btn').forEach(x=>x.classList.remove('selected')); b.classList.add('selected'); state.selectedTime=t; }); return b; }
  function renderSlots(){ amSlots.innerHTML=''; pmSlots.innerHTML=''; AM.forEach(t=>amSlots.appendChild(makeBtn(t))); PM.forEach(t=>pmSlots.appendChild(makeBtn(t))); updatePeriod(); }
  function updatePeriod(){ if(state.period==='am'){ amSlots.classList.remove('hidden'); pmSlots.classList.add('hidden'); } else { pmSlots.classList.remove('hidden'); amSlots.classList.add('hidden'); } }
  periodBtns.forEach(btn=>btn.addEventListener('click',()=>{ periodBtns.forEach(b=>b.classList.remove('selected')); btn.classList.add('selected'); state.period=btn.dataset.period; updatePeriod(); }));
  renderSlots();

  // 초기화
  document.getElementById('reset-btn').addEventListener('click', ()=>{
    peopleBtns.forEach(b=>b.classList.remove('selected')); peopleInput.value=''; state.partySize=null;
    document.querySelectorAll('.day').forEach(x=>x.classList.remove('selected')); state.selectedDate=null;
    document.querySelectorAll('.time-btn').forEach(x=>x.classList.remove('selected')); state.selectedTime=null;
    document.querySelector('.time-period-btn[data-period="am"]').click();
  });

  // 예약하기
  document.getElementById('reserve-btn').addEventListener('click', async ()=>{
    const type = document.body.dataset.type; // CAFE/RESTAURANT
    const targetId = document.body.dataset.targetId;
    if(!state.partySize) return alert('인원수를 선택/입력하세요.');
    if(!state.selectedDate) return alert('날짜를 선택하세요.');
    if(!state.selectedTime) return alert('시간을 선택하세요.');
    const reservationAt = `${state.selectedDate}T${state.selectedTime}`;
    const { token, header } = csrf();
    const headers = { 'Content-Type':'application/json' }; if(token) headers[header]=token;

    try {
      const res = await fetch('/api/reservations', {
        method:'POST', headers,
        body: JSON.stringify({
          targetType: type,
          targetId: Number(targetId),
          reservationAt,
          partySize: state.partySize
        })
      });
      const data = await res.json().catch(()=>({}));
      if(!res.ok) return alert(data.message || '예약에 실패했습니다.');
      alert('예약이 완료되었습니다.');
      location.href = '/mypage/reservations';
    } catch(e){ console.error(e); alert('네트워크 오류가 발생했습니다.'); }
  });
})();
