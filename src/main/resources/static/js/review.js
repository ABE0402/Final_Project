document.addEventListener('DOMContentLoaded', function() {
    const reviewWriteBtn = document.querySelector('.review-write-btn');
    const reviewFormWrapper = document.querySelector('.review-form-wrapper');
    const cancelBtn = document.querySelector('.cancel-btn');

    // '리뷰 작성' 버튼 클릭 시 폼을 보이게 함
    reviewWriteBtn.addEventListener('click', () => {
        reviewFormWrapper.style.display = 'block';
    });

    // '취소' 버튼 클릭 시 폼을 숨기게 함
    cancelBtn.addEventListener('click', () => {
        reviewFormWrapper.style.display = 'none';
    });

    // 별점 기능
    const stars = document.querySelectorAll('.review-rating-stars i');
    let currentRating = 0; // 현재 선택된 별점

    stars.forEach(star => {
        star.addEventListener('mouseover', () => {
            const value = parseInt(star.getAttribute('data-value'));
            updateStars(value);
        });

        star.addEventListener('mouseout', () => {
            // 마우스가 별 영역을 벗어나면, 선택된 별점으로 돌아갑니다.
            updateStars(currentRating);
        });

        star.addEventListener('click', () => {
            const value = parseInt(star.getAttribute('data-value'));
            currentRating = value; // 현재 별점 업데이트
            updateStars(currentRating); // 선택된 별점 색상으로 고정
            console.log('선택된 별점:', currentRating);
        });
    });

    // 별의 색상을 업데이트하는 함수 (모든 별점 로직을 여기서 처리)
    function updateStars(rating) {
            stars.forEach(star => {
                const starValue = parseInt(star.getAttribute('data-value'));
                if (starValue <= rating) {
                    // 선택된 별까지는 채워진 별로 변경
                    star.classList.remove('fa-regular');
                    star.classList.add('fa-solid');
                    star.style.color = '#3A6D1F';
                } else {
                    // 선택되지 않은 별은 빈 별로 유지
                    star.classList.remove('fa-solid');
                    star.classList.add('fa-regular');
                    star.style.color = '#3A6D1F';
                }
            });
        }
    });