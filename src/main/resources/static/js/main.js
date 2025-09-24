// =======================================================================
// 통합 main.js
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤, 캐러셀, 정렬)
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================

document.addEventListener("DOMContentLoaded", () => {
    // 기능 1 초기화
    initMainPageDynamicLoader();
    // 기능 2 초기화
    initSearchFilterModal();
});


// =======================================================================
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤 등)
// =======================================================================
function initMainPageDynamicLoader() {
    let currentCategory = 'all';
    let currentSort = 'recommend';
    let currentPage = 0;
    let pageSize = 12;
    let isLoading = false;
    let noMore = false;
    let observer = null;

    // 초기 상태 감지 및 버튼 핸들러 설정
    const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
    if (catFromServer) currentCategory = catFromServer;

    document.querySelectorAll('.cat-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentCategory = btn.dataset.category;
            resetAndReloadCards();
        });
    });

    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentSort = btn.dataset.sort;
            resetAndReloadCards();
        });
    });

    function resetAndReloadCards() {
        currentPage = 0;
        noMore = false;
        // 버튼 UI 업데이트
        // ... (필요 시 .active 클래스 제어 로직 추가)
        reloadCards();
    }

    function reloadCards() {
        fetch(`/cards-fragment?category=${currentCategory}&sort=${currentSort}&page=0&size=${pageSize}`)
            .then(res => res.text())
            .then(html => {
                document.querySelector('#card-container').innerHTML = html;
                currentPage = 0;
                noMore = false;
                setupInfiniteScroll();
            })
            .catch(console.error);
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
                const res = await fetch(`/cards-fragment?category=${currentCategory}&sort=${currentSort}&page=${next}&size=${pageSize}`);
                const html = await res.text();
                const temp = document.createElement('div');
                temp.innerHTML = html;

                if (!html.trim() || temp.querySelector('.empty-message')) {
                    noMore = true;
                    if (observer) observer.disconnect();
                } else {
                    const newCards = temp.querySelector('#card-container').innerHTML;
                    document.querySelector('#card-container').insertAdjacentHTML('beforeend', newCards);
                    currentPage = next;
                }
            } catch (e) {
                console.error(e);
            } finally {
                isLoading = false;
            }
        }, { rootMargin: '200px' });

        observer.observe(target);
    }

    // 최초 실행
    setupInfiniteScroll();
}


// =======================================================================
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================
function initSearchFilterModal() {
    const mainCategoryInput = document.getElementById('search-category');
        // [수정됨] HTML의 클래스 이름과 일치시킴
    const categoryButtons = document.querySelectorAll('.filter-trigger-btn');
    const modalTitle = document.getElementById('filterModalLabel');
    const modalBody = document.querySelector('#filterModal .modal-body');


    if (!modalBody) return; // 상세 필터 모달이 없는 페이지면 실행 중단

    const filterData = {
               cafe: {
                       title: '☕ 카페 상세 필터',
                       groups: [
                           // 1. 동반인 (요청사항과 동일하여 유지)
                           { id: 'companion', title: '동반인', multi: true, options: [
                               { value: 'solo', text: '👤 1인' }, { value: 'friends', text: '🎉 친구' }, { value: 'couple', text: '💖 커플' }, { value: 'family', text: '👨‍👩‍👧‍👦 가족' }, { value: 'group', text: '🏢 단체' }
                           ]},
                           // 2. 분위기 (옵션 수정)
                           { id: 'mood', title: '분위기', multi: true, options: [
                               { value: 'quiet', text: '🤫 조용한' }, { value: 'talk', text: '💬 대화하기 좋은' },
                               { value: 'exciting', text: '🎉 신나는' }, // '신나는' 추가
                               { value: 'study', text: '📚 카공하기 좋은' }, { value: 'feel good', text: '🍷 분위기 좋은' }, { value: 'date', text: '💖 데이트하기 좋은' }
                           ]},
                           // 3. 편의 및 서비스 (요청사항과 동일하여 유지)
                           { id: 'amenities', title: '편의 및 서비스', multi: true, options: [
                               { value: 'parking', text: '🚗 주차장' }, { value: 'toilet', text: '🚻 화장실' }, { value: 'pet-friendly', text: '🐾 반려동물' }, { value: 'waiting room', text: '🛌 대기실' }, { value: 'takeout', text: '🥡 포장' }
                           ]},
                           // 4. 예약 여부 (옵션 수정)
                           { id: 'reservation', title: '예약 여부', multi: false, default: 'possible', options: [
                               // '상관없음' 제거
                               { value: 'possible', text: '✅ 가능' }, { value: 'impossible', text: '❌ 불가능' }
                           ]},
                           // 5. 우선순위 (요청사항과 동일하여 유지)
                           { id: 'sort', title: '우선순위 (정렬)', multi: false, default: 'hits', options: [
                               { value: 'hits', text: '⭐ 많이 찾는 순' }, { value: 'reviews', text: '📝 리뷰 많은 순' }, { value: 'rating', text: '👍 평점 높은 순' }, { value: 'like', text: '⭐️ 즐겨찾기 많은 순' }
                           ]},
                           // 6. 종류 (옵션 수정)
                           { id: 'type', title: '종류', multi: true, options: [
                               { value: 'dessert', text: '🍰 디저트 전문' }, { value: 'coffee', text: '☕ 커피 전문' },
                               { value: 'interior', text: '🛋️ 인테리어 맛집' } // '사진 맛집' 제거
                           ]}
                       ]
                   },
                restaurant: {
                    title: '🍽️ 식당 상세 필터',
                    groups: [
                        { id: 'companion', title: '동반인', multi: true, options: [
                            { value: 'solo', text: '👤 1인' }, { value: 'friends', text: '🎉 친구' }, { value: 'couple', text: '💖 커플' }, { value: 'family', text: '👨‍👩‍👧‍👦 가족' }, { value: 'group', text: '🏢 단체' }
                        ]},
                         { id: 'mood', title: '분위기', multi: true, options: [
                            { value: 'quiet', text: '🤫 조용한' }, { value: 'solo-friendly', text: '🍚 혼밥하기 좋은' }, { value: 'date', text: '💖 데이트하기 좋은' },  { value: 'feel good', text: '🍷 분위기 좋은' }, { value: 'photo-spot', text: '📸 사진 맛집' }
                        ]},
                        { id: 'amenities', title: '편의시설', multi: true, options: [
                            { value: 'parking', text: '🚗 주차장' }, { value: 'toilet', text: '🚻 화장실' }, { value: 'pet-friendly', text: '🐾 반려동물' }, { value: 'waiting room', text: '🛌 대기실' }, { value: 'takeout', text: '🥡 포장' }
                        ]},
                        { id: 'days', title: '영업 요일', multi: true, options: [
                            { value: 'mon', text: '월' }, { value: 'tue', text: '화' }, { value: 'wed', text: '수' }, { value: 'thu', text: '목' }, { value: 'fri', text: '금' }, { value: 'sat', text: '토' }, { value: 'sun', text: '일' }
                        ]},
                        { id: 'type', title: '종류', multi: true, options: [
                            { value: 'korean', text: '🍚 한식' }, { value: 'chinese', text: '🍜 중식' }, { value: 'japanese', text: '🍣 일식' }, { value: 'western', text: '🍝 양식' }, { value: 'fusion', text: '🥘 퓨전' }, { value: 'asian', text: '🥠 아시안' }
                        ]},
                        { id: 'reservation', title: '예약 여부', multi: false, default: 'any', options: [
                            { value: 'any', text: '상관없음' }, { value: 'possible', text: '✅ 가능' }, { value: 'impossible', text: '❌ 불가능' }
                        ]},
                         { id: 'sort', title: '우선순위 (정렬)', multi: false, default: 'hits', options: [
                            { value: 'hits', text: '⭐ 많이 찾는 순' }, { value: 'reviews', text: '📝 리뷰 많은 순' }, { value: 'rating', text: '👍 평점 높은 순' }, { value: 'like', text: '⭐️ 즐겨찾기 많은 순' }
                        ]},
                    ]
                }
            };

    let activeFilterCategory = 'cafe';
    let selectedFilters = {};

    function buildModalBody(category) {
        activeFilterCategory = category;
        const data = filterData[category];
        modalTitle.textContent = data.title;
        modalBody.innerHTML = '';

        data.groups.forEach(group => {
            const multiText = group.multi ? '<small class="text-muted">(다중 선택)</small>' : '';
            const optionsHtml = group.options.map(opt => `<button type="button" class="btn btn-outline-secondary" data-value="${opt.value}">${opt.text}</button>`).join('');
            modalBody.innerHTML += `
                <div class="filter-group mb-4">
                    <h6>${group.title} ${multiText}</h6>
                    <div class="btn-group flex-wrap gap-2" id="filter-${group.id}">${optionsHtml}</div>
                </div>`;
        });
        initializeFilters();
    }

    function initializeFilters() {
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

    categoryButtons.forEach(button => {
        button.addEventListener('click', function() {
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            const category = this.dataset.category;
            mainCategoryInput.value = category;
            buildModalBody(category);
        });
    });

    modalBody.addEventListener('click', function(event) {
        const button = event.target.closest('button');
        if (!button) return;
        const groupId = button.parentElement.id.replace('filter-', '');
        const value = button.dataset.value;
        const selected = selectedFilters[groupId];
        if (selected instanceof Set) {
            selected.has(value) ? selected.delete(value) : selected.add(value);
        } else {
            selectedFilters[groupId] = value;
        }
        updateFilterUI();
    });

    document.getElementById('apply-filters').addEventListener('click', function() {
        for (const key in selectedFilters) {
            const input = document.getElementById(`search-${key}`);
            if (input) {
                const value = selectedFilters[key];
                input.value = (value instanceof Set) ? [...value].join(',') : value;
            }
        }
    });

    document.getElementById('reset-filters').addEventListener('click', initializeFilters);

    buildModalBody(activeFilterCategory);
}