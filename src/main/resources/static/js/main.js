// =======================================================================
// 통합 main.js
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤, 카테고리/정렬)
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================

document.addEventListener("DOMContentLoaded", () => {
    // 기능 1 초기화: 메인 페이지의 동적 컨텐츠 로더 실행
    initMainPageDynamicLoader();

    // 기능 2 초기화: 검색창의 상세 필터 모달 기능 실행
    initSearchFilterModal();
});


// =======================================================================
// 기능 1: 메인 페이지 동적 컨텐츠 로딩 (무한 스크롤 등)
// =======================================================================
function initMainPageDynamicLoader() {
    // 이 기능에 필요한 요소들이 없으면 실행하지 않음
    if (!document.querySelector('.cat-btn')) return;

    let currentCategory = 'all';
    let currentSort = 'recommend';
    let currentPage = 0;
    let pageSize = 12;
    let isLoading = false;
    let noMore = false;
    let observer = null;

    const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
    if (catFromServer) currentCategory = catFromServer;

    document.querySelectorAll('.cat-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentCategory = btn.dataset.category;
            document.querySelectorAll('.cat-btn').forEach(b => {
                b.classList.remove('btn-olive');
                b.classList.add('btn-outline-olive');
            });
            btn.classList.add('btn-olive');
            btn.classList.remove('btn-outline-olive');
            reloadCards();
        });
    });

    document.querySelectorAll('.sort-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentSort = btn.dataset.sort;
            document.querySelectorAll('.sort-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            reloadCards();
        });
    });

    function reloadCards() {
        currentPage = 0;
        noMore = false;
        isLoading = true;
        fetch(`/cards-fragment?category=${currentCategory}&sort=${currentSort}&page=0&size=${pageSize}`)
            .then(res => res.text())
            .then(html => {
                document.querySelector('#card-container').innerHTML = html;
                setupInfiniteScroll();
            })
            .catch(console.error)
            .finally(() => isLoading = false);
    }

    function setupInfiniteScroll() {
        if (observer) observer.disconnect();
        const target = document.querySelector('#scroll-end');
        if (!target) return;

        observer = new IntersectionObserver(async (entries) => {
            if (entries[0].isIntersecting && !isLoading && !noMore) {
                isLoading = true;
                currentPage++;
                try {
                    const res = await fetch(`/cards-fragment?category=${currentCategory}&sort=${currentSort}&page=${currentPage}&size=${pageSize}`);
                    const html = await res.text();
                    if (!html.trim() || html.includes('empty-message')) {
                        noMore = true;
                        if(observer) observer.disconnect();
                    } else {
                        document.querySelector('#card-container').insertAdjacentHTML('beforeend', html);
                    }
                } catch (e) {
                    console.error(e);
                } finally {
                    isLoading = false;
                }
            }
        }, { rootMargin: '200px' });
        observer.observe(target);
    }

    setupInfiniteScroll();
}


// =======================================================================
// 기능 2: 검색창 상세 필터 모달 관리
// =======================================================================
function initSearchFilterModal() {
    const mainCategoryButton = document.getElementById('category-dropdown-button');
    const mainCategoryInput = document.getElementById('search-category');
    const mainDropdownItems = document.querySelectorAll('.main-category-item');
    const cafeFilterTrigger = document.querySelector('.filter-trigger-btn[data-category="cafe"]');
    const restaurantFilterTrigger = document.querySelector('.filter-trigger-btn[data-category="restaurant"]');
    const modalTitle = document.getElementById('filterModalLabel');
    const modalBody = document.querySelector('#filterModal .modal-body');

    if (!mainCategoryButton || !modalBody) return; // 상세 필터 검색창이 없으면 실행 중단

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
                    // ... (days, type 등 나머지 그룹들도 동일한 방식으로 value를 DB의 name과 일치시켜 주세요)
                    { id: 'reservation', title: '예약 여부', multi: false, default: 'any', options: [

                        { value: '가능', text: '✅ 가능' },
                        { value: '불가능', text: '❌ 불가능' }
                    ]},
                    { id: 'sort', title: '우선순위 (정렬)', multi: false, default: 'hits', options: [
                        { value: 'hits', text: '⭐ 많이 찾는 순' },
                        { value: 'reviews', text: '📝 리뷰 많은 순' },
                        { value: 'rating', text: '👍 평점 높은 순' },
                        { value: 'like', text: '⭐️ 즐겨찾기 많은 순' }
                    ]},
                ]
            },
        restaurant: {
            title: '🍽️ 식당 상세 필터',
            groups: [
                { id: 'companion', title: '동반인', multi: true, options: [ { value: 'solo', text: '👤 1인' }, { value: 'friends', text: '🎉 친구' }, { value: 'couple', text: '💖 커플' }, { value: 'family', text: '👨‍👩‍👧‍👦 가족' }, { value: 'group', text: '🏢 단체' } ] },
                { id: 'mood', title: '분위기', multi: true, options: [ { value: 'quiet', text: '🤫 조용한' }, { value: 'solo-friendly', text: '🍚 혼밥하기 좋은' }, { value: 'date', text: '💖 데이트하기 좋은' },  { value: 'feel good', text: '🍷 분위기 좋은' }, { value: 'photo-spot', text: '📸 사진 맛집' } ] },
                { id: 'amenities', title: '편의시설', multi: true, options: [ { value: 'parking', text: '🚗 주차장' }, { value: 'toilet', text: '🚻 화장실' }, { value: 'pet-friendly', text: '🐾 반려동물' }, { value: 'waiting room', text: '🛌 대기실' }, { value: 'takeout', text: '🥡 포장' } ] },
                { id: 'type', title: '종류', multi: true, options: [ { value: 'korean', text: '🍚 한식' }, { value: 'chinese', text: '🍜 중식' }, { value: 'japanese', text: '🍣 일식' }, { value: 'western', text: '🍝 양식' }, { value: 'fusion', text: '🥘 퓨전' }, { value: 'asian', text: '🥠 아시안' } ] },
                { id: 'reservation', title: '예약 여부', multi: false, default: 'any', options: [ { value: 'any', text: '상관없음' }, { value: 'possible', text: '✅ 가능' }, { value: 'impossible', text: '❌ 불가능' } ] },
                { id: 'sort', title: '우선순위 (정렬)', multi: false, default: 'hits', options: [ { value: 'hits', text: '⭐ 많이 찾는 순' }, { value: 'reviews', text: '📝 리뷰 많은 순' }, { value: 'rating', text: '👍 평점 높은 순' }, { value: 'like', text: '⭐️ 즐겨찾기 많은 순' } ] },
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
            const multiText = group.multi ? '<small class="text-muted">(다중 선택)</small>' : '';
            const optionsHtml = group.options.map(opt => `<button type="button" class="btn btn-outline-secondary" data-value="${opt.value}">${opt.text}</button>`).join('');
            modalBody.innerHTML += `<div class="filter-group mb-4"><h6>${group.title} ${multiText}</h6><div class="btn-group flex-wrap gap-2" id="filter-${group.id}">${optionsHtml}</div></div>`;
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

    mainDropdownItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const value = this.dataset.value;
            mainCategoryButton.innerHTML = this.innerHTML;
            mainCategoryInput.value = value;
            cafeFilterTrigger.style.display = (value === 'cafe') ? 'inline-block' : 'none';
            restaurantFilterTrigger.style.display = (value === 'restaurant') ? 'inline-block' : 'none';
        });
    });

    [cafeFilterTrigger, restaurantFilterTrigger].forEach(trigger => {
        trigger.addEventListener('click', function() {
            buildModalBody(this.dataset.category);
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
}

