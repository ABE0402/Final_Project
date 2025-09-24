document.addEventListener('DOMContentLoaded', () => {
  // --- 별점 기능 ---
  const stars = document.querySelectorAll('.review-rating-stars i');
  let currentRating = 0;

  function updateStars(rating) {
    stars.forEach(star => {
      const starValue = parseInt(star.getAttribute('data-value'));
      if (starValue <= rating) {
        star.classList.remove('fa-regular');
        star.classList.add('fa-solid');
        star.style.color = '#3A6D1F';
      } else {
        star.classList.remove('fa-solid');
        star.classList.add('fa-regular');
        star.style.color = '#3A6D1F';
      }
    });
  }

  if (stars.length > 0) {
    stars.forEach(star => {
      star.addEventListener('mouseover', () => {
        updateStars(parseInt(star.getAttribute('data-value')));
      });

      star.addEventListener('mouseout', () => {
        updateStars(currentRating);
      });

      star.addEventListener('click', () => {
        currentRating = parseInt(star.getAttribute('data-value'));
        updateStars(currentRating);
        console.log('선택된 별점:', currentRating);
      });
    });
  }

  // --- 리뷰 작성 폼 열기/닫기 ---
  const reviewWriteBtn = document.querySelector('.review-write-btn');
  const reviewFormWrapper = document.querySelector('.review-form-wrapper');

  // ✅ 모든 cancel-btn을 선택하도록 수정
  const cancelBtns = document.querySelectorAll('.cancel-btn');

  if (reviewWriteBtn && reviewFormWrapper) {
    reviewWriteBtn.addEventListener('click', () => {
      reviewFormWrapper.style.display = 'block';
    });
  }

  if (cancelBtns.length > 0) {
    // ✅ 각 버튼에 이벤트 리스너를 추가
    cancelBtns.forEach(cancelBtn => {
      cancelBtn.addEventListener('click', (e) => {
        e.preventDefault();

        const pageType = cancelBtn.dataset.pageType;

        if (pageType === 'write') {
          // 'write' 페이지일 경우: 이전 페이지로 이동
          window.history.back();
        } else if (pageType === 'list') {
          // 'list' 페이지일 경우: 작성 폼만 닫기
          if (reviewFormWrapper) {
            reviewFormWrapper.style.display = 'none';
          }
        }
      });
    });
  }

  // --- 이미지 업로드 및 미리보기 ---
  const photoUploadContainer = document.querySelector('.photo-upload-container');
  const photoUploadBox = document.querySelector('.photo-upload-box');
  const maxPhotos = 3;

  if (photoUploadBox && photoUploadContainer) {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = 'image/*';
    fileInput.multiple = true;
    fileInput.style.display = 'none';
    photoUploadBox.appendChild(fileInput);

    photoUploadBox.addEventListener('click', () => {
      fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
      const files = e.target.files;
      if (!files.length) return;

      const currentCount = photoUploadContainer.querySelectorAll('.photo-preview-box').length;
      const remainingSlots = maxPhotos - currentCount;

      Array.from(files).slice(0, remainingSlots).forEach(file => {
        const reader = new FileReader();

        reader.onload = (event) => {
          const previewBox = document.createElement('div');
          previewBox.classList.add('photo-preview-box');

          const previewImg = document.createElement('img');
          previewImg.src = event.target.result;
          previewImg.classList.add('photo-preview');

          const deleteBtn = document.createElement('span');
          deleteBtn.classList.add('delete-photo-btn');

          const deleteIcon = document.createElement('i');
          deleteIcon.classList.add('fa-solid', 'fa-xmark');
          deleteBtn.appendChild(deleteIcon);

          deleteBtn.addEventListener('click', () => {
            previewBox.remove();
            const updatedCount = photoUploadContainer.querySelectorAll('.photo-preview-box').length;
            if (updatedCount < maxPhotos) {
              photoUploadBox.style.display = 'flex';
            }
          });

          previewBox.appendChild(previewImg);
          previewBox.appendChild(deleteBtn);
          photoUploadContainer.insertBefore(previewBox, photoUploadBox);

          const updatedCount = photoUploadContainer.querySelectorAll('.photo-preview-box').length;
          if (updatedCount >= maxPhotos) {
            photoUploadBox.style.display = 'none';
          }
        };

        reader.readAsDataURL(file);
      });

      fileInput.value = '';
    });
  }

  // --- 글자 수 카운트 ---
  const reviewTextarea = document.querySelector('.review-text-section textarea');
  const charCount = document.querySelector('.char-count');
  const maxChars = 1000;

  if (reviewTextarea && charCount) {
    reviewTextarea.addEventListener('input', () => {
      const currentLength = reviewTextarea.value.length;
      charCount.textContent = `${currentLength} / ${maxChars}`;
    });
  }
});