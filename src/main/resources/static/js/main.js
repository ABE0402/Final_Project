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
    const mainCategoryButton = document.getElementById('category-dropdown-button');
    const mainCategoryInput = document.getElementById('search-category');
    const mainDropdownItems = document.querySelectorAll('.main-category-item');
    const cafeFilterTrigger = document.querySelector('.filter-trigger-btn[data-category="cafe"]');
    const restaurantFilterTrigger = document.querySelector('.filter-trigger-btn[data-category="restaurant"]');
    const modalTitle = document.getElementById('filterModalLabel');
    const modalBody = document.querySelector('#filterModal .modal-body');

    if (!mainCategoryButton || !modalBody) return;

    const filterData = {
        cafe: {
            title: '☕ 카페 상세 필터',
            groups: [
                { id: 'companion', title: '동반인', multi: true, options: [ { value: 'solo', text: '👤 1인' }, { value: 'friends', text: '🎉 친구' }, { value: 'couple', text: '💖 커플' }, { value: 'family', text: '👨‍👩‍👧‍👦 가족' } ] },
                { id: 'mood', title: '분위기', multi: true, options: [ { value: 'quiet', text: '🤫 조용한' }, { value: 'talk', text: '💬 대화하기 좋은' } ] },
                { id: 'sort', title: '정렬', multi: false, default: 'hits', options: [ { value: 'hits', text: '⭐ 인기순' }, { value: 'reviews', text: '📝 리뷰순' } ] }
            ]
        },
        restaurant: {
            title: '🍽️ 식당 상세 필터',
            groups: [
                { id: 'type', title: '종류', multi: true, options: [ { value: 'korean', text: '🍚 한식' }, { value: 'chinese', text: '🍜 중식' } ] },
                { id: 'sort', title: '정렬', multi: false, default: 'hits', options: [ { value: 'hits', text: '⭐ 인기순' }, { value: 'rating', text: '👍 평점순' } ] }
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
