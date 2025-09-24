document.addEventListener('DOMContentLoaded', function() {
    // =========================================
    // 1. 메인 이미지 캐러셀 로직
    // =========================================
    const mainCarouselContainer = document.querySelector('.carousel-container-inner');
    const mainPrevBtn = document.querySelector('.carousel-nav-btn.prev');
    const mainNextBtn = document.querySelector('.carousel-nav-btn.next');
    const mainCarouselItems = document.querySelector('.carousel-items');

    if (mainCarouselContainer && mainPrevBtn && mainNextBtn && mainCarouselItems) {
        const firstItem = mainCarouselItems.querySelector('.carousel-item');
        if (firstItem) {
            const itemWidth = firstItem.offsetWidth;
            const gap = 10;
            const scrollAmount = (itemWidth + gap) * 3;

            mainPrevBtn.addEventListener('click', () => {
                mainCarouselContainer.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
            });

            mainNextBtn.addEventListener('click', () => {
                mainCarouselContainer.scrollBy({ left: scrollAmount, behavior: 'smooth' });
            });
        }
    }


    // =========================================
    // 2. 영업 상태 및 시간 정보 로직 (API 연동)
    // =========================================

    // 서버에서 영업시간 데이터를 가져와서 UI를 업데이트하는 메인 함수
    function fetchAndDisplayHours() {
        // 서버 API 호출
        fetch('/api/store/hours')
            .then(response => {
                if (!response.ok) {
                    throw new Error('네트워크 응답이 올바르지 않습니다.');
                }
                return response.json();
            })
            .then(data => {
            console.log(data);
                // 데이터 수신 성공 시, UI 업데이트 함수 호출
                updateStatusBadge(data);
                displayAllHours(data);
            })
            .catch(error => {
                console.error('영업시간 정보를 가져오는 중 오류 발생:', error);
                const badge = document.getElementById("statusBadge");
                if (badge) {
                    badge.textContent = "정보 없음";
                    badge.className = "badge status-unavailable";
                }
            });
    }

    // 영업 상태(뱃지) 업데이트 함수
    function updateStatusBadge(openingHoursData) {
        const badge = document.getElementById("statusBadge");
        if (!badge) return;

        const now = new Date();
        const dayOfWeek = now.getDay(); // 0:일요일, 1:월요일 ... 6:토요일
        const days = ["sun", "mon", "tue", "wed", "thu", "fri", "sat"];
        const todayData = openingHoursData[days[dayOfWeek]];

        // holiday 또는 isHoliday 속성 확인
        if (todayData.holiday || todayData.isHoliday) {
            badge.textContent = "휴무";
            badge.className = "badge status-holiday";
            return;
        }

        const nowMinutes = now.getHours() * 60 + now.getMinutes();
        const openMinutes = todayData.open.split(":").map(Number).reduce((h, m) => h * 60 + m);
        const closeMinutes = todayData.close.split(":").map(Number).reduce((h, m) => h * 60 + m);

        let isOpen = false;

        // 자정을 넘는 경우 (예: 22:00 ~ 02:00)
        if (closeMinutes < openMinutes) {
            if (nowMinutes >= openMinutes || nowMinutes < closeMinutes) {
                isOpen = true;
            }
        }
        // 같은 날에 종료되는 경우 (예: 10:00 ~ 20:00)
        else {
            if (nowMinutes >= openMinutes && nowMinutes < closeMinutes) {
                isOpen = true;
            }
        }

        if (isOpen) {
            badge.textContent = "영업중";
            badge.className = "badge status-open";
        } else {
            badge.textContent = "영업종료";
            badge.className = "badge status-closed";
        }
    }

    // 모든 요일의 영업시간을 표시하는 함수
    function displayAllHours(openingHoursData) {
        const scheduleContainer = document.getElementById('business-hours-container');
        if (!scheduleContainer) return;

        scheduleContainer.innerHTML = ''; // 기존 내용 초기화

        const dayNames = {
            "mon": "월", "tue": "화", "wed": "수", "thu": "목", "fri": "금", "sat": "토", "sun": "일"
        };

        const dayOrder = ["mon", "tue", "wed", "thu", "fri", "sat", "sun"];

        dayOrder.forEach(dayKey => {
            const dayData = openingHoursData[dayKey];
            const p = document.createElement('p');
            let timeString;

            // `holiday` 또는 `isHoliday` 속성 확인
            if (dayData && (dayData.holiday || dayData.isHoliday)) {
                timeString = "휴무";
            } else {
                // open/close가 null이면 '-' 표시
                const open = dayData?.open ?? '-';
                const close = dayData?.close ?? '-';
                timeString = `${open}-${close}`;
            }

            p.textContent = `${dayNames[dayKey]}: ${timeString}`;
            scheduleContainer.appendChild(p);
        });
    }

    // 별점 렌더링 함수
    function renderRatingStars() {
        const starContainers = document.querySelectorAll('.rating-stars');

        starContainers.forEach(container => {
            const rating = parseFloat(container.dataset.rating);
            if (isNaN(rating)) {
                // 평점 정보가 없을 경우, 빈 별 5개를 표시
                for (let i = 0; i < 5; i++) {
                    const star = document.createElement('i');
                    star.className = 'fa-regular fa-star';
                    container.appendChild(star);
                }
                return; // 함수 종료
            }

            const fullStars = Math.floor(rating);
            const halfStar = rating - fullStars >= 0.5;

            // 기존 별점 초기화
            container.innerHTML = '';

            // 채워진 별 추가
            for (let i = 0; i < fullStars; i++) {
                const star = document.createElement('i');
                star.className = 'fa-solid fa-star';
                container.appendChild(star);
            }

            // 반쪽 별 추가
            if (halfStar) {
                const halfStarIcon = document.createElement('i');
                halfStarIcon.className = 'fa-solid fa-star-half-stroke';
                container.appendChild(halfStarIcon);
            }

            // 빈 별 추가
            const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
            for (let i = 0; i < emptyStars; i++) {
                const star = document.createElement('i');
                star.className = 'fa-regular fa-star';
                container.appendChild(star);
            }
        });
    }

    // =========================================
    // 3. 즐겨찾기(북마크) 기능 로직
    // =========================================
    function initBookmark() {
        const bookmarkBtn = document.getElementById('bookmarkBtn');
        console.log("bookmarkBtn:", bookmarkBtn); // 추가
        if (!bookmarkBtn) return;

        bookmarkBtn.addEventListener('click', function() {
            console.log("클릭됨!"); // 추가
            const heartIcon = this.querySelector('i');

            if (heartIcon.classList.contains('fa-regular')) {
                heartIcon.classList.remove('fa-regular');
                heartIcon.classList.add('fa-solid');
                console.log('즐겨찾기에 추가되었습니다.');
            } else {
                heartIcon.classList.remove('fa-solid');
                heartIcon.classList.add('fa-regular');
                console.log('즐겨찾기에서 해제되었습니다.');
            }
        });
    }



    // =========================================
    // 초기 함수 호출 및 이벤트 리스너 등록
    // =========================================
    fetchAndDisplayHours();
    renderRatingStars();
    initBookmark();
    setInterval(fetchAndDisplayHours, 60 * 1000);
});