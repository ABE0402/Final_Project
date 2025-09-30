// =======================================================================
// 통합 main.js
// - 기능 1: 메인 페이지 동적 컨텐츠 로딩 (추천 → 세그먼트 → 태그, 무한 스크롤)
// - 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================

document.addEventListener("DOMContentLoaded", () => {
  initMainPageDynamicLoader();   // 홈(섹션 무한스크롤 + 추천/세그먼트/태그)일 때만 동작
  initSearchFilterModal();       // 상세 필터 모달
});

// =======================================================================
// 기능 1: 메인 페이지 동적 컨텐츠 로딩
// =======================================================================
function initMainPageDynamicLoader() {
  const cardRoot = document.querySelector('#card-container');
  if (!cardRoot) return;

  // 섹션 컨테이너(SPA 안전장치)
  let sectionsRoot = document.querySelector('#sections-container');
  if (!sectionsRoot) {
    sectionsRoot = document.createElement('div');
    sectionsRoot.id = 'sections-container';
    cardRoot.innerHTML = '';
    cardRoot.appendChild(sectionsRoot);
  }

  let currentCategory = 'cafe';       // 기본 카테고리
  let currentSort = 'recommend';      // recommend | rating | review | favorite (백엔드 기준)
  let currentPage = 0;
  let isLoading = false;
  let noMore = false;
  let observer = null;

  // 태그는 mood → type 순서 (요청사항)
  const TAG_CATEGORIES = 'mood,type';

  // 서버가 활성 카테고리에 btn-olive를 붙여줬으면 우선 반영
  const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
  if (catFromServer) currentCategory = catFromServer;

  // 카테고리 버튼(카페/식당)
  document.querySelectorAll('.cat-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentCategory = btn.dataset.category;
      currentPage = 0; noMore = false;
      document.querySelectorAll('.cat-btn').forEach(b => {
        b.classList.remove('btn-olive');
        b.classList.add('btn-outline-olive');
      });
      btn.classList.remove('btn-outline-olive');
      btn.classList.add('btn-olive');
      reloadSections();
    });
  });

  // 섹션 내부 정렬 드롭다운(이벤트 위임)
  const SORT_LABEL = { recommend:'추천순', rating:'평점순', review:'리뷰순', favorite:'즐겨찾기순' };
  sectionsRoot.addEventListener('click', (e) => {
    const btn = e.target.closest('.sort-btn');
    if (btn) {
      const box = btn.parentElement.querySelector('.sort-options');
      document.querySelectorAll('.sort-options').forEach(el => { if (el !== box) el.style.display = 'none'; });
      box.style.display = (box.style.display === 'block') ? 'none' : 'block';
      return;
    }
    const li = e.target.closest('.sort-options li');
    if (li) {
      currentSort = li.dataset.sort;
      document.querySelectorAll('.sort-options').forEach(el => el.style.display = 'none');
      // 즉시 버튼 라벨도 업데이트(UX 지연 방지)
      const parentBox = li.closest('.position-relative');
      const btn2 = parentBox?.querySelector('.sort-btn');
      if (btn2) btn2.innerHTML = `${SORT_LABEL[currentSort] || '정렬'} ▼`;
      // 실제 데이터는 reloadSections로 재요청
      currentPage = 0; noMore = false;
      reloadSections();
    }
  });
  // 외부 클릭 시 옵션 박스 닫기
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.position-relative')) {
      document.querySelectorAll('.sort-options').forEach(el => el.style.display = 'none');
    }
  });

  // ✅ 초기 로드
  reloadSections();

  // ---------------- 내부 구현 ----------------

  // 추천 섹션(캐러셀/220px 카드, fragment와 동일 규격)
  async function buildRecommendationSectionIfAny(place) {
    if (place !== 'cafe') return null; // 현재는 카페 카테고리에서만 노출

    try {
      const res = await fetch('/api/recommend/cafes?topN=12', { credentials: 'include' });
      if (!res.ok) return null; // 401 등: 비로그인 → 추천 스킵
      const cafes = await res.json();
      if (!cafes || cafes.length === 0) return null;

      const section = document.createElement('section');
      section.className = 'tag-section mb-5';
      section.dataset.tag = 'recommendations';
      section.innerHTML = `
        <div class="container">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <h5 class="m-0">맞춤 추천</h5>
          </div>
          <div class="position-relative">
            <button class="carousel-btn prev btn btn-light border position-absolute top-50 start-0 translate-middle-y">‹</button>
            <div class="overflow-hidden px-4">
              <div class="d-flex gap-3 carousel-items" style="transition:transform .25s;">
                ${cafes.map(cafe => `
                  <a class="card text-decoration-none text-reset" style="width:220px" href="/cafes/${encodeURIComponent(cafe.id)}">
                    <div class="ratio ratio-16x9 card-img-top"
                         style="background-image:url('${escapeAttr(cafe.heroImageUrl || '/images/placeholder.jpg')}'); background-size:cover; background-position:center; border-top-left-radius:.5rem; border-top-right-radius:.5rem;"></div>
                    <div class="card-body p-2">
                      <div class="d-flex justify-content-between align-items-start">
                        <h6 class="card-title text-truncate m-0">${escapeHtml(cafe.name || '')}</h6>
                        <span class="badge text-bg-success">카페</span>
                      </div>
                      <div class="small text-muted text-truncate">${escapeHtml(cafe.addressRoad || '')}</div>
                      <div class="mt-1 small">⭐ ${safeNumber(cafe.averageRating)} · 리뷰 ${safeNumber(cafe.reviewCount)}</div>
                    </div>
                  </a>
                `).join('')}
              </div>
            </div>
            <button class="carousel-btn next btn btn-light border position-absolute top-50 end-0 translate-middle-y">›</button>
          </div>
        </div>
      `;
      initLocalCarousel(section); // 추천 캐러셀 활성화
      return section;
    } catch (e) {
      console.error('추천 섹션 생성 오류:', e);
      return null;
    }
  }

  // 캐러셀 좌우 스크롤(섹션 로컬)
  function initLocalCarousel(sectionEl) {
    const track = sectionEl.querySelector('.carousel-items');
    if (!track) return;
    let offset = 0;
    const step = 240; // 카드 220 + gap(대략)
    const prev = sectionEl.querySelector('.carousel-btn.prev');
    const next = sectionEl.querySelector('.carousel-btn.next');
    const maxScroll = () => Math.max(0, track.scrollWidth - track.parentElement.clientWidth);

    function apply() {
      const max = maxScroll();
      if (offset < 0) offset = 0;
      if (offset > max) offset = max;
      track.style.transform = `translate3d(${-offset}px,0,0)`;
    }
    prev?.addEventListener('click', () => { offset -= step; apply(); });
    next?.addEventListener('click', () => { offset += step; apply(); });
    window.addEventListener('resize', apply);
    apply();
  }

  // 세그먼트/태그 0페이지 로딩(부분 실패 무시: 비로그인에서도 태그는 보여야 함!)
  async function reloadSections() {
    if (observer) observer.disconnect();
    isLoading = false; noMore = false; currentPage = 0;
    sectionsRoot.innerHTML = '';

    const segUrl  = `/segments-fragment?place=${encodeURIComponent(currentCategory)}`;
    const tagsUrl = `/tags-fragment?categories=${encodeURIComponent(TAG_CATEGORIES)}&place=${encodeURIComponent(currentCategory)}&sort=${encodeURIComponent(currentSort)}&page=0`;

    try {
      const [recSection, segHtml, tagsHtml] = await Promise.all([
        buildRecommendationSectionIfAny(currentCategory),                        // (옵션)
        fetch(segUrl).then(r => r.ok ? r.text() : '').catch(() => ''),          // 실패해도 ''
        fetch(tagsUrl).then(r => r.ok ? r.text() : '').catch(() => ''),         // 실패해도 ''
      ]);

      // 1) 추천(있다면 최상단)
      if (recSection) sectionsRoot.appendChild(recSection);

      // 2) 세그먼트(연령→성별) — 실패해도 태그로 계속
      if (segHtml) {
        const t = document.createElement('div'); t.innerHTML = segHtml;
        t.querySelectorAll('section.tag-section').forEach(sec => {
          sectionsRoot.appendChild(sec);
          initLocalCarousel(sec);
        });
      }

      // 3) 태그(mood→type) 0페이지 — 비로그인에서도 반드시 붙도록
      if (tagsHtml) {
        const t = document.createElement('div'); t.innerHTML = tagsHtml;
        t.querySelectorAll('section.tag-section').forEach(sec => {
          sectionsRoot.appendChild(sec);
          initLocalCarousel(sec);
        });
        noMore = getHasMoreFromEl(t) === false;
      } else {
        // 태그도 실패하면 안내
        sectionsRoot.insertAdjacentHTML('beforeend',
          `<div class="alert alert-light border text-center my-4">지금은 매장을 불러올 수 없어요. 잠시 후 다시 시도해 주세요.</div>`);
        noMore = true;
      }

      setupInfiniteScroll();
    } catch (e) {
      console.error('섹션 재로딩 오류:', e);
      sectionsRoot.insertAdjacentHTML('beforeend',
        `<div class="alert alert-light border text-center my-4">로딩 중 오류가 발생했어요.</div>`);
      noMore = true;
    }
  }

  function setupInfiniteScroll() {
    if (observer) observer.disconnect();
    const target = document.querySelector('#scroll-end');
    if (!target) return;

    observer = new IntersectionObserver(async (entries) => {
      const entry = entries[0];
      if (!entry.isIntersecting || isLoading || noMore) return;

      isLoading = true;
      try {
        const next = currentPage + 1;
        const res  = await fetch(`/tags-fragment?categories=${encodeURIComponent(TAG_CATEGORIES)}&place=${encodeURIComponent(currentCategory)}&sort=${encodeURIComponent(currentSort)}&page=${next}`);
        const html = await res.text();

        const temp = document.createElement('div'); temp.innerHTML = html;
        const sections = temp.querySelectorAll('section.tag-section');
        if (sections.length === 0) { noMore = true; return; }

        sections.forEach(sec => {
          sectionsRoot.appendChild(sec);
          initLocalCarousel(sec);
        });

        noMore = getHasMoreFromEl(temp) === false;
        currentPage = next;
      } catch (e) {
        console.error(e);
      } finally {
        isLoading = false;
      }
    }, { rootMargin: '400px' });

    observer.observe(target);
  }

  function getHasMoreFromEl(node) {
    const hv = node.querySelector('#has-more')?.dataset.value;
    return hv ? hv === 'true' : false;
  }

  // 안전 유틸
  function escapeHtml(str) {
    return String(str)
      .replaceAll('&','&amp;').replaceAll('<','&lt;')
      .replaceAll('>','&gt;').replaceAll('"','&quot;')
      .replaceAll("'",'&#39;');
  }
  function escapeAttr(str) { return escapeHtml(String(str || '')); }
  function safeNumber(v) { return (v==null || Number.isNaN(v)) ? 0 : v; }
}

// =======================================================================
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================
function initSearchFilterModal() {
  const mainCategoryButton = document.getElementById('category-dropdown-button');
  const mainCategoryInput  = document.getElementById('search-category');
  const mainDropdownItems  = document.querySelectorAll('.main-category-item');
  const cafeFilterTrigger       = document.querySelector('.filter-trigger-btn[data-category="cafe"]');
  const restaurantFilterTrigger = document.querySelector('.filter-trigger-btn[data-category="restaurant"]');
  const modalTitle = document.getElementById('filterModalLabel');
  const modalBody  = document.querySelector('#filterModal .modal-body');

  // 상세 필터 요소가 없으면 종료
  if (!mainCategoryButton || !modalBody) return;

  const filterData = {
    cafe: {
      title: '☕ 카페 상세 필터',
      groups: [
        { id: 'companion', title: '동반인', multi: true, options: [
          { value: '1인', text: '👤 1인' },
          { value: '친구', text: '🎉 친구' },
          { value: '커플', text: '💖 커플' },
          { value: '가족', text: '👨‍👩‍👧‍👦 가족' },
          { value: '단체', text: '🏢 단체' }
        ]},
        { id: 'mood', title: '분위기', multi: true, options: [
          { value: '조용한', text: '🤫 조용한' },
          { value: '대화하기 좋은', text: '💬 대화하기 좋은' },
          { value: '신나는', text: '🎉 신나는' },
          { value: '카공하기 좋은', text: '📚 카공하기 좋은' },
          { value: '분위기 좋은', text: '🍷 분위기 좋은' },
          { value: '데이트하기 좋은', text: '💖 데이트하기 좋은' },
          { value: '사진 맛집', text: '📸 사진 맛집' }
        ]},
        { id: 'amenities', title: '편의 및 서비스', multi: true, options: [
          { value: '주차장', text: '🚗 주차장' },
          { value: '화장실', text: '🚻 화장실' },
          { value: '반려동물 가능', text: '🐾 반려동물' },
          { value: '대기실', text: '🛌 대기실' },
          { value: '포장', text: '🥡 포장' }
        ]},
        { id: 'reservation', title: '예약 여부', multi: false, default: '가능', options: [
          { value: '가능', text: '✅ 가능' },
          { value: '불가능', text: '❌ 불가능' }
        ]},
        { id: 'sort', title: '우선순위 (정렬)', multi: false, default: '많이 찾는', options: [
          { value: '많이 찾는', text: '⭐ 많이 찾는 순' },
          { value: '리뷰 많은', text: '📝 리뷰 많은 순' },
          { value: '평점 높은', text: '👍 평점 높은 순' },
          { value: '즐겨찾기 많은', text: '⭐️ 즐겨찾기 많은 순' }
        ]},
        { id: 'type', title: '종류', multi: true, options: [
          { value: '디저트 전문', text: '디저트 전문' },
          { value: '커피 전문', text: '커피 전문' },
          { value: '인테리어 맛집', text: '인테리어 맛집' }
        ]},
      ]
    },
    restaurant: {
      title: '🍽️ 식당 상세 필터',
      groups: [
        { id: 'companion', title: '동반인', multi: true, options: [
          { value: 'solo', text: '👤 1인' },
          { value: 'friends', text: '🎉 친구' },
          { value: 'couple', text: '💖 커플' },
          { value: 'family', text: '👨‍👩‍👧‍👦 가족' },
          { value: 'group', text: '🏢 단체' }
        ]},
        { id: 'mood', title: '분위기', multi: true, options: [
          { value: 'quiet', text: '🤫 조용한' },
          { value: 'solo-friendly', text: '🍚 혼밥하기 좋은' },
          { value: 'date', text: '💖 데이트하기 좋은' },
          { value: 'feel good', text: '🍷 분위기 좋은' },
          { value: 'photo-spot', text: '📸 사진 맛집' }
        ]},
        { id: 'amenities', title: '편의시설', multi: true, options: [
          { value: 'parking', text: '🚗 주차장' },
          { value: 'toilet', text: '🚻 화장실' },
          { value: 'pet-friendly', text: '🐾 반려동물' },
          { value: 'waiting room', text: '🛌 대기실' },
          { value: 'takeout', text: '🥡 포장' }
        ]},
        { id: 'type', title: '종류', multi: true, options: [
          { value: 'korean', text: '🍚 한식' },
          { value: 'chinese', text: '🍜 중식' },
          { value: 'japanese', text: '🍣 일식' },
          { value: 'western', text: '🍝 양식' },
          { value: 'fusion', text: '🥘 퓨전' },
          { value: 'asian', text: '🥠 아시안' }
        ]},
        { id: 'reservation', title: '예약 여부', multi: false, default: 'any', options: [
          { value: 'any', text: '상관없음' },
          { value: 'possible', text: '✅ 가능' },
          { value: 'impossible', text: '❌ 불가능' }
        ]},
        { id: 'sort', title: '우선순위 (정렬)', multi: false, default: 'hits', options: [
          { value: 'hits', text: '⭐ 많이 찾는 순' },
          { value: 'reviews', text: '📝 리뷰 많은 순' },
          { value: 'rating', text: '👍 평점 높은 순' },
          { value: 'like', text: '⭐️ 즐겨찾기 많은 순' }
        ]},
      ]
    }
  };

  let activeFilterCategory = null;
  let selectedFilters = {};

  function buildModalBody(category) {
    activeFilterCategory = category;
    const data = filterData[category];
    modalTitle.textContent = data.title;
    modalBody.innerHTML = '';
    data.groups.forEach(group => {
      const multiText   = group.multi ? '<small class="text-muted">(다중 선택)</small>' : '';
      const optionsHtml = group.options.map(opt =>
        `<button type="button" class="btn btn-outline-secondary" data-value="${opt.value}">${opt.text}</button>`
      ).join('');
      modalBody.innerHTML += `
        <div class="filter-group mb-4">
          <h6>${group.title} ${multiText}</h6>
          <div class="btn-group flex-wrap gap-2" id="filter-${group.id}">
            ${optionsHtml}
          </div>
        </div>`;
    });
    initializeFilters();
  }

  function initializeFilters() {
    if (!activeFilterCategory) return;
    const data = filterData[activeFilterCategory];
    selectedFilters = {};
    data.groups.forEach(group => {
      selectedFilters[group.id] = group.multi ? new Set() : group.default;
    });
    updateFilterUI();
  }

  function updateFilterUI() {
    modalBody.querySelectorAll('.filter-group').forEach(groupDiv => {
      const groupId = groupDiv.querySelector('.btn-group').id.replace('filter-', '');
      const selected = selectedFilters[groupId];
      groupDiv.querySelectorAll('button').forEach(button => {
        const value = button.dataset.value;
        button.classList.toggle('active', selected instanceof Set ? selected.has(value) : selected === value);
      });
    });
  }

  // 카테고리 드롭다운
  if (mainDropdownItems.length) {
    mainDropdownItems.forEach(item => {
      item.addEventListener('click', function(e) {
        e.preventDefault();
        const value = this.dataset.value;
        mainCategoryButton.innerHTML = this.innerHTML;
        if (mainCategoryInput) mainCategoryInput.value = value;
        if (cafeFilterTrigger)       cafeFilterTrigger.style.display       = (value === 'cafe') ? 'inline-block' : 'none';
        if (restaurantFilterTrigger) restaurantFilterTrigger.style.display = (value === 'restaurant') ? 'inline-block' : 'none';
      });
    });
  }

  // 모달 열기 트리거
  [cafeFilterTrigger, restaurantFilterTrigger].forEach(trigger => {
    if (!trigger) return;
    trigger.addEventListener('click', function() { buildModalBody(this.dataset.category); });
  });

  // 모달 내부 버튼 토글
  modalBody.addEventListener('click', function(event) {
    const button = event.target.closest('button');
    if (!button) return;

    const groupId = button.parentElement.id.replace('filter-', '');
    const value = button.dataset.value;
    const selected = selectedFilters[groupId];

    if (selected instanceof Set) {
      selected.has(value) ? selected.delete(value) : selected.add(value); // 다중 선택
    } else {
      selectedFilters[groupId] = value; // 단일 선택
    }
    updateFilterUI();
  });

  // 적용 → 숨은 input에 값 주입
  const applyBtn = document.getElementById('apply-filters');
  if (applyBtn) {
    applyBtn.addEventListener('click', function() {
      for (const key in selectedFilters) {
        const input = document.getElementById(`search-${key}`);
        if (input) {
          const value = selectedFilters[key];
          input.value = (value instanceof Set) ? [...value].join(',') : value;
        }
      }
    });
  }

  // 초기화
  const resetBtn = document.getElementById('reset-filters');
  if (resetBtn) resetBtn.addEventListener('click', initializeFilters);
}
