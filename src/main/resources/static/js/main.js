// =======================================================================
// 통합 main.js
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤, 카테고리/정렬)
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================

document.addEventListener("DOMContentLoaded", () => {
  initMainPageDynamicLoader();   // 홈(섹션 무한스크롤)일 때만 동작
  initSearchFilterModal();       // 필터 모달 요소 있을 때만 동작
  loadRecommendations();         // 맞춤 추천 목록을 불러오는 함수 호출
});

// =======================================================================
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤 등)
// =======================================================================
function initMainPageDynamicLoader() {
  // 홈이 아니면 종료(섹션 컨테이너 기준이 더 정확)
  if (!document.querySelector('#sections-container')) return;

  let currentCategory = 'cafe';       // 기본 'cafe'
  let currentSort = 'recommend';      // recommend | rating | review
  let currentPage = 0;
  let isLoading = false;
  let noMore = false;
  let observer = null;

  // 태그는 type → mood 순서 고정
  const TAG_CATEGORIES = 'type,mood';

  // 서버가 활성 카테고리에 btn-olive 클래스를 넣어줬다면 우선
  const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
  if (catFromServer) currentCategory = catFromServer;

  // 카테고리 버튼
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

  // 정렬 버튼
  document.querySelectorAll('.sort-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentSort = btn.dataset.sort;
      currentPage = 0; noMore = false;
      document.querySelectorAll('.sort-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      reloadSections();
    });
  });

  // 첫 세팅(SSR 이후 이어붙이기)
  setupInfiniteScroll();

  function reloadSections() {
    // 개인화 + 태그 조각을 함께 불러서 "개인화 → 태그" 순으로 렌더
    const segUrl  = `/segments-fragment?place=${encodeURIComponent(currentCategory)}`;
    const tagsUrl = `/tags-fragment?categories=${encodeURIComponent(TAG_CATEGORIES)}&place=${encodeURIComponent(currentCategory)}&sort=${encodeURIComponent(currentSort)}&page=0`;

    Promise.all([fetch(segUrl), fetch(tagsUrl)])
      .then(async ([resSeg, resTags]) => {
        const htmlSeg  = await resSeg.text();
        const htmlTags = await resTags.text();

        const tempSeg  = document.createElement('div');  tempSeg.innerHTML  = htmlSeg;
        const tempTags = document.createElement('div');  tempTags.innerHTML = htmlTags;

        const container = document.querySelector('#sections-container');
        container.innerHTML = ''; // reset

        // 1) 개인화(연령 → 성별)
        tempSeg.querySelectorAll('section.tag-section').forEach(sec => container.appendChild(sec));
        // 2) 태그(type → mood)
        tempTags.querySelectorAll('section.tag-section').forEach(sec => container.appendChild(sec));

        // 무한스크롤은 태그 조각의 has-more 기준
        noMore = getHasMoreFromEl(tempTags) === false;
        currentPage = 0;

        setupInfiniteScroll(true);
      })
      .catch(console.error);
  }

  function setupInfiniteScroll(reset=false) {
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

        const temp = document.createElement('div');
        temp.innerHTML = html;

        const sections = temp.querySelectorAll('section.tag-section');
        if (sections.length === 0) { noMore = true; return; }

        const container = document.querySelector('#sections-container');
        sections.forEach(sec => container.appendChild(sec));

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
} // ✅ 여기로 initMainPageDynamicLoader 닫힘 (중요)

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
        // 기본값 any 를 옵션에도 노출(상관없음)
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
          { value: 'solo', text: '👤 1인' }, { value: 'friends', text: '🎉 친구' },
          { value: 'couple', text: '💖 커플' }, { value: 'family', text: '👨‍👩‍👧‍👦 가족' },
          { value: 'group', text: '🏢 단체' }
        ]},
        { id: 'mood', title: '분위기', multi: true, options: [
          { value: 'quiet', text: '🤫 조용한' }, { value: 'solo-friendly', text: '🍚 혼밥하기 좋은' },
          { value: 'date', text: '💖 데이트하기 좋은' },  { value: 'feel good', text: '🍷 분위기 좋은' },
          { value: 'photo-spot', text: '📸 사진 맛집' }
        ]},
        { id: 'amenities', title: '편의시설', multi: true, options: [
          { value: 'parking', text: '🚗 주차장' }, { value: 'toilet', text: '🚻 화장실' },
          { value: 'pet-friendly', text: '🐾 반려동물' }, { value: 'waiting room', text: '🛌 대기실' },
          { value: 'takeout', text: '🥡 포장' }
        ]},
        { id: 'type', title: '종류', multi: true, options: [
          { value: 'korean', text: '🍚 한식' }, { value: 'chinese', text: '🍜 중식' },
          { value: 'japanese', text: '🍣 일식' }, { value: 'western', text: '🍝 양식' },
          { value: 'fusion', text: '🥘 퓨전' }, { value: 'asian', text: '🥠 아시안' }
        ]},
        { id: 'reservation', title: '예약 여부', multi: false, default: 'any', options: [
          { value: 'any', text: '상관없음' }, { value: 'possible', text: '✅ 가능' }, { value: 'impossible', text: '❌ 불가능' }
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
         // 다중 선택
         selected.has(value) ? selected.delete(value) : selected.add(value);
     } else {
         // 단일 선택 (수정됨)
         selectedFilters[groupId] = value;
     }

     updateFilterUI(); // UI 업데이트
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

// =======================================================================
// [추가됨] 기능 3: 맞춤 추천 목록 불러오기
// =======================================================================
function loadRecommendations() {
    const recommendationContainer = document.getElementById('recommendation-cards');
    // 추천 섹션이 없으면 (비로그인 상태 등) 함수를 실행하지 않음
    if (!recommendationContainer) {
        return;
    }

    // fetch API를 사용해 백엔드에 데이터를 요청
    fetch('/api/recommend/cafes?topN=4') // 상위 4개만 가져오도록 요청
        .then(response => {
            // 401 Unauthorized 등 에러 응답 처리
            if (!response.ok) {
                // 로그인하지 않았거나 오류가 발생하면 추천 섹션을 숨김
                document.getElementById('recommendation-section').style.display = 'none';
                return;
            }
            return response.json(); // 응답을 JSON으로 변환
        })
        .then(cafes => {
            if (!cafes || cafes.length === 0) {
                recommendationContainer.innerHTML = '<div class="col-12"><p class="text-muted">추천할 만한 카페를 찾지 못했어요. 검색을 더 해보세요!</p></div>';
                return;
            }

            // 받아온 데이터로 HTML 카드 생성
            let cardsHtml = '';
            cafes.forEach(cafe => {
                cardsHtml += `
                    <div class="col">
                        <a class="card h-100 text-decoration-none text-reset" href="/cafes/${cafe.id}">
                            <div class="ratio ratio-16x9 bg-light card-img-top"
                                 style="background-image:url('${cafe.heroImageUrl || '/images/placeholder_shop.jpg'}')"></div>
                            <div class="card-body">
                                <h6 class="card-title text-truncate">${cafe.name}</h6>
                                <div class="small text-muted text-truncate">${cafe.addressRoad}</div>
                                <div class="mt-2 small">⭐ ${cafe.averageRating} · 리뷰 ${cafe.reviewCount}</div>
                            </div>
                        </a>
                    </div>
                `;
            });

            // 생성된 HTML 카드를 컨테이너에 삽입
            recommendationContainer.innerHTML = cardsHtml;
        })
        .catch(error => {
            console.error('추천 목록을 불러오는 중 오류 발생:', error);
            recommendationContainer.innerHTML = '<div class="col-12"><p class="text-danger">추천 목록을 불러오는 데 실패했습니다.</p></div>';
        });
}
