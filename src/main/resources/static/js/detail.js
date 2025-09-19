document.addEventListener('DOMContentLoaded', function() {
    // 1. 메인 이미지 캐러셀 로직
    const mainCarouselContainer = document.querySelector('.carousel-container-inner');
    // ✅ 버튼 선택자를 ID에서 클래스로 변경합니다.
    const mainPrevBtn = document.querySelector('.carousel-nav-btn.prev');
    const mainNextBtn = document.querySelector('.carousel-nav-btn.next');
    const mainCarouselItems = document.querySelector('.carousel-items');

    // 요소들이 모두 존재하는지 확인
    if (mainCarouselContainer && mainPrevBtn && mainNextBtn && mainCarouselItems) {
        const firstItem = mainCarouselItems.querySelector('.carousel-item');
        if (!firstItem) return;

        // 아이템의 실제 렌더링된 너비를 가져옵니다.
        const itemWidth = firstItem.offsetWidth;
        // CSS에 설정된 픽셀 단위 간격
        const gap = 10;

        // 3개 아이템의 너비와 그 사이의 간격 2개를 합산하여 스크롤 이동량 계산
        const scrollAmount = (itemWidth + gap) * 3;

        console.log("스크롤 이동량:", scrollAmount);

        mainPrevBtn.addEventListener('click', () => {
            mainCarouselContainer.scrollBy({
                left: -scrollAmount,
                behavior: 'smooth'
            });
        });

        mainNextBtn.addEventListener('click', () => {
            mainCarouselContainer.scrollBy({
                left: scrollAmount,
                behavior: 'smooth'
            });
        });
    }

    // 2. 주요 정보 섹션 버튼 로직
    const viewMapBtn = document.getElementById('view-map-btn');
    const getDirectionsBtn = document.getElementById('get-directions-btn');
    const shareBtn = document.getElementById('share-btn');
    const callBtn = document.getElementById('call-btn');
    const bookmarkBtn = document.getElementById('bookmark-btn');

    if (viewMapBtn) {
        viewMapBtn.addEventListener('click', () => {
            alert('지도보기 버튼이 클릭되었습니다!');
        });
    }
    
    if (getDirectionsBtn) {
        getDirectionsBtn.addEventListener('click', () => {
            console.log('길찾기 버튼 클릭');
        });
    }
    
    if (shareBtn) {
        shareBtn.addEventListener('click', () => {
            console.log('공유하기 버튼 클릭');
        });
    }
    
    if (callBtn) {
        callBtn.addEventListener('click', () => {
            console.log('전화하기 버튼 클릭');
        });
    }

    if (bookmarkBtn) {
        bookmarkBtn.addEventListener('click', () => {
            console.log('북마크 버튼 클릭');
        });
    }
});