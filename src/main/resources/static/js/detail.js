/* 기본 스타일 */
body {
    font-family: Arial, sans-serif;
    background-color: #f5f5f5;
    margin: 0;
}

.review-page-container {
    max-width: 800px;
    margin: 20px auto;
    padding: 20px;
}

/* 헤더 & 푸터 */
.site-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background-color: #fff;
    border-bottom: 1px solid #ddd;
}

.site-header .logo {
    font-weight: bold;
    font-size: 24px;
}

.site-header .auth a {
    text-decoration: none;
    color: #333;
    margin-left: 15px;
}

.site-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background-color: #fff;
    border-top: 1px solid #ddd;
    margin-top: 40px;
}

.site-footer .footer-logo {
    font-weight: bold;
}

.site-footer .social-icons a {
    margin: 0 5px;
    color: #333;
}

.site-footer .footer-menu a {
    text-decoration: none;
    color: #666;
    margin: 0 10px;
    font-size: 14px;
}

/* 페이지 헤더 */
.review-page-header {
    text-align: center;
    margin-bottom: 20px;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.review-page-header h2 {
    font-size: 32px;
    margin: 0;
}

.review-page-header p {
    color: #666;
    margin: 5px 0;
}

.review-page-header .rating {
    color: #f7a623;
    font-size: 18px;
}

/* 리뷰 목록 및 폼 컨테이너 */
.review-list-box {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.review-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid #555;
    padding-bottom: 10px;
    margin-bottom: 20px;
}

.review-list-header h3 {
    font-size: 20px;
    margin: 0;
}

.review-write-btn {
    padding: 8px 15px;
    border: 1px solid #ddd;
    background-color: #f8f9fa;
    border-radius: 20px;
    cursor: pointer;
}

/* 리뷰 작성 폼 */
.review-form-wrapper {
    display: none; /* 이 부분이 폼을 숨기는 역할을 합니다. */
}

.review-form {
    padding: 20px;
    background-color: #f9f9f9;
    border-radius: 8px;
    margin-bottom: 20px;
    border: 1px solid #ddd;
}

.review-form h3 {
    text-align: center;
    margin-top: 0;
    font-size: 20px;
}

.review-rating-stars {
    text-align: center;
    margin-bottom: 20px;
    font-size: 24px;
    color: #3A6D1F;
}

.review-rating-stars i {
    cursor: pointer;
}

.form-section h4 {
    font-size: 16px;
    font-weight: normal;
    margin: 0 0 10px;
}

.photo-upload-box {
    width: 100px;
    height: 100px;
    border: 2px dashed #ccc;
    border-radius: 8px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 40px;
    color: #ccc;
    cursor: pointer;
}

.review-text-section textarea {
    width: 100%;
    height: 120px;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    resize: vertical;
    box-sizing: border-box;
}

.form-buttons {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
}

.form-buttons button {
    padding: 10px 30px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
}

.form-buttons .cancel-btn {
    background-color: #ccc;
    color: #555;
}

.form-buttons .submit-btn {
    background-color: #3A6D1F; /* 등록 버튼 배경색 */
    color: #fff;
}

/* 리뷰 목록 */
.review-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.review-item {
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
}

.review-item:last-child {
    border-bottom: none;
}

.review-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
}

.reviewer {
    font-weight: bold;
    font-size: 16px;
}

.review-date {
    font-size: 12px;
    color: #888;
}

.review-content {
    margin: 5px 0 10px;
    color: #555;
    font-size: 14px;
}