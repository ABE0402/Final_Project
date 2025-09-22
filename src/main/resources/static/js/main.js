// ==========================
// Global 상태
// ==========================
let currentCategory = 'cafe';   // 기본 카테고리
let currentSort = 'recommend';   // 기본 정렬
let currentPage = 0;
let isLoading = false;
let noMoreData = false;
let observer = null;

document.addEventListener("DOMContentLoaded", () => {
    initAllSections();
    setupCategoryButtons();
    setupInfiniteScroll();  // 초기 무한스크롤
});

// ==========================
// 카테고리 버튼
// ==========================
function setupCategoryButtons() {
    document.querySelectorAll(".category-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const category = btn.dataset.category;
            currentCategory = category;
            currentSort = 'recommend';
            currentPage = 0;
            noMoreData = false;

            // 버튼 active 처리
            document.querySelectorAll(".category-btn").forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            // 기존 섹션 제거
            const mainContent = document.querySelector("#main-content");
            mainContent.innerHTML = "";

            // 새 데이터 가져오기
            try {
                const res = await fetch(`/tags-fragment?page=0&category=${category}&sort=${currentSort}`);
                if(!res.ok) throw new Error("서버 오류");
                const html = await res.text();
                if(!html.trim()) return;

                const temp = document.createElement("div");
                temp.innerHTML = html;
                const sections = [...temp.children];
                mainContent.append(...sections);

                // 새 섹션 초기화
                sections.forEach(sec => {
                    if(sec.classList.contains("tag-section")){
                        setupCarousel(sec);
                        setupSortDropdown(sec);
                    }
                });

                // 무한스크롤 재설정
                setupInfiniteScroll();

            } catch(err) {
                console.error(err);
            }
        });
    });
}

// ==========================
// 모든 섹션 초기화
// ==========================
function initAllSections() {
    document.querySelectorAll(".tag-section").forEach(section => {
        setupCarousel(section);
        setupSortDropdown(section);
    });
}

// ==========================
// 캐러셀
// ==========================
function setupCarousel(section){
    const container = section.querySelector(".carousel-items");
    const prevBtn = section.querySelector(".carousel-btn.prev");
    const nextBtn = section.querySelector(".carousel-btn.next");
    if(!container || !prevBtn || !nextBtn) return;

    const cardWidth = 220 + 20;
    const visibleCount = 4;
    const totalCards = container.children.length;
    const totalPages = Math.ceil(totalCards / visibleCount);
    let pageIndex = 0;

    const updateScroll = () => {
        const scrollX = pageIndex * cardWidth * visibleCount;
        container.style.transform = `translateX(-${scrollX}px)`;
        prevBtn.disabled = pageIndex === 0;
        nextBtn.disabled = pageIndex >= totalPages - 1;
    };

    prevBtn.onclick = () => { if(pageIndex>0){ pageIndex--; updateScroll(); }};
    nextBtn.onclick = () => { if(pageIndex<totalPages-1){ pageIndex++; updateScroll(); }};

    updateScroll();
}

// ==========================
// 정렬 드롭다운
// ==========================
function setupSortDropdown(section){
    const btn = section.querySelector(".sort-btn");
    const dropdown = section.querySelector(".sort-options");
    const tag = section.dataset.tag;
    if(!btn || !dropdown) return;

    document.addEventListener("click", () => dropdown.classList.remove("show"));

    btn.onclick = e => {
        e.stopPropagation();
        document.querySelectorAll(".sort-options.show").forEach(dd=>{
            if(dd!==dropdown) dd.classList.remove("show");
        });
        dropdown.classList.toggle("show");
    };

    dropdown.querySelectorAll("li").forEach(option => {
        option.onclick = async e => {
            e.stopPropagation();
            const sortKey = option.dataset.sort;
            currentSort = sortKey;
            currentPage = 0;
            noMoreData = false;

            try {
                const res = await fetch(`/tags-fragment?tag=${tag}&page=0&category=${currentCategory}&sort=${sortKey}`);
                if(!res.ok) throw new Error("서버 오류");
                const html = await res.text();
                if(!html.trim()) return;

                const temp = document.createElement("div");
                temp.innerHTML = html;
                const newSection = temp.querySelector(`.tag-section[data-tag="${tag}"]`);
                if(newSection){
                    // 기존 섹션 교체
                    section.replaceWith(newSection);
                    setupCarousel(newSection);
                    setupSortDropdown(newSection);

                    // 버튼 텍스트 & 옵션 selected
                    const newBtn = newSection.querySelector(".sort-btn");
                    const newDropdown = newSection.querySelector(".sort-options");
                    newBtn.textContent = option.textContent + " ▼";
                    newDropdown.querySelectorAll("li").forEach(li => li.classList.remove("selected"));
                    newDropdown.querySelector(`li[data-sort="${sortKey}"]`)?.classList.add("selected");
                }
            } catch(err) {
                console.error(err);
            }
        };
    });
}

// ==========================
// 무한스크롤
// ==========================
function setupInfiniteScroll() {
    if(observer) observer.disconnect(); // 기존 observer 제거

    observer = new IntersectionObserver(async entries => {
        const entry = entries[0];
        if(entry.isIntersecting && !isLoading && !noMoreData){
            isLoading = true;
            currentPage++;

            try {
                const res = await fetch(`/tags-fragment?page=${currentPage}&category=${currentCategory}&sort=${currentSort}`);
                if(!res.ok) throw new Error("서버 응답 오류");

                const html = await res.text();
                if(html.trim() === "" || html.includes('empty-message')){
                    noMoreData = true;
                    showEndMessage();
                    observer.disconnect();
                } else {
                    const tempDiv = document.createElement("div");
                    tempDiv.innerHTML = html;
                    const newSections = [...tempDiv.children];
                    document.querySelector("#main-content").append(...newSections);

                    newSections.forEach(sec => {
                        if(sec.classList.contains("tag-section")){
                            setupCarousel(sec);
                            setupSortDropdown(sec);
                        }
                    });
                }
            } catch(err) {
                console.error("무한 스크롤 오류:", err);
            } finally {
                isLoading = false;
            }
        }
    }, { rootMargin: "100px" });

    const scrollEnd = document.querySelector("#scroll-end");
    if(scrollEnd) observer.observe(scrollEnd);

    function showEndMessage(){
        const endMsg = document.createElement("div");
        endMsg.className = "end-message";
        endMsg.style.textAlign = "center";
        endMsg.style.padding = "20px";
        endMsg.style.color = "#777";
        endMsg.textContent = "더 이상 불러올 게시물이 없습니다.";
        document.querySelector("#main-content").appendChild(endMsg);
    }
}
