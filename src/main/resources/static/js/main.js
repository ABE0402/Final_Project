// /static/js/main.js

let currentCategory = 'all';
let currentSort = 'recommend';
let currentPage = 0;
let pageSize = 12;
let isLoading = false;
let noMore = false;
let observer = null;

document.addEventListener('DOMContentLoaded', () => {
  // 서버에서 렌더된 초기 상태 감지
  const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
  if (catFromServer) currentCategory = catFromServer;

  // 버튼 핸들
  document.querySelectorAll('.cat-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentCategory = btn.dataset.category;
      currentPage = 0;
      noMore = false;
      // 버튼 표시
      document.querySelectorAll('.cat-btn').forEach(b => {
        b.classList.remove('btn-olive'); b.classList.add('btn-outline-olive');
      });
      btn.classList.remove('btn-outline-olive'); btn.classList.add('btn-olive');
      reloadCards();
    });
  });

  document.querySelectorAll('.sort-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentSort = btn.dataset.sort;
      currentPage = 0;
      noMore = false;
      document.querySelectorAll('.sort-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      reloadCards();
    });
  });

  setupInfiniteScroll();
});

function reloadCards() {
  fetch(`/cards-fragment?category=${currentCategory}&sort=${currentSort}&page=0&size=${pageSize}`)
    .then(res => res.text())
    .then(html => {
      document.querySelector('#card-container').innerHTML = html;
      currentPage = 0;
      noMore = false;
      setupInfiniteScroll(true);
    })
    .catch(console.error);
}

function setupInfiniteScroll(reset=false) {
  if (observer) observer.disconnect();

  const target = document.querySelector('#scroll-end');
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

      // 카드가 없거나 empty-message가 있으면 종료
      if (!html.trim() || temp.querySelector('.empty-message')) {
        noMore = true;
        return;
      }
      // 새로운 섹션 안의 카드를 현재 컨테이너에 append
      const newSection = temp.querySelector('section');
      if (newSection) {
        document.querySelector('#card-container').appendChild(newSection);
        currentPage = next;
      } else {
        noMore = true;
      }
    } catch (e) {
      console.error(e);
    } finally {
      isLoading = false;
    }
  }, { rootMargin: '200px' });

  if (target) observer.observe(target);
}
