let currentCategory = 'cafe';       // 기본을 'cafe'로
let currentSort = 'recommend';      // recommend | rating | review
let currentPage = 0;
let isLoading = false;
let noMore = false;
let observer = null;

// 태그는 type → mood 순서 고정
const TAG_CATEGORIES = 'type,mood';

document.addEventListener('DOMContentLoaded', () => {
  // 서버가 active 클래스 올려준 버튼에서 현재 카테고리 읽기 (없으면 'cafe' 유지)
  const catFromServer = document.querySelector('.cat-btn.btn-olive')?.dataset.category;
  if (catFromServer) currentCategory = catFromServer;

  // 카테고리 버튼
  document.querySelectorAll('.cat-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentCategory = btn.dataset.category;
      currentPage = 0; noMore = false;
      document.querySelectorAll('.cat-btn').forEach(b => { b.classList.remove('btn-olive'); b.classList.add('btn-outline-olive'); });
      btn.classList.remove('btn-outline-olive'); btn.classList.add('btn-olive');
      reloadSections();
    });
  });

  // 정렬 버튼
  document.querySelectorAll('.sort-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      currentSort = btn.dataset.sort;
      currentPage = 0; noMore = false;
      document.querySelectorAll('.sort-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      reloadSections();
    });
  });

  setupInfiniteScroll();
});

function reloadSections() {
  // 개인화 + 태그 조각을 함께 불러서 "개인화 → 태그" 순으로 렌더
  const segUrl  = `/segments-fragment?place=${encodeURIComponent(currentCategory)}`;
  const tagsUrl = `/tags-fragment?categories=${encodeURIComponent(TAG_CATEGORIES)}&place=${encodeURIComponent(currentCategory)}&sort=${encodeURIComponent(currentSort)}&page=0`;

  Promise.all([fetch(segUrl), fetch(tagsUrl)])
    .then(async ([resSeg, resTags]) => {
      const htmlSeg  = await resSeg.text();
      const htmlTags = await resTags.text();

      const tempSeg  = document.createElement('div');  tempSeg.innerHTML  = htmlSeg;
      const tempTags = document.createElement('div');  tempTags.innerHTML = htmlTags;

      const container = document.querySelector('#sections-container');
      container.innerHTML = ''; // reset

      // 1) 개인화(연령 → 성별)
      tempSeg.querySelectorAll('section.tag-section').forEach(sec => container.appendChild(sec));
      // 2) 태그(type → mood)
      tempTags.querySelectorAll('section.tag-section').forEach(sec => container.appendChild(sec));

      // 무한스크롤은 태그 조각의 has-more 기준
      noMore = getHasMoreFromEl(tempTags) === false;
      currentPage = 0;

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
      const res  = await fetch(`/tags-fragment?categories=${encodeURIComponent(TAG_CATEGORIES)}&place=${encodeURIComponent(currentCategory)}&sort=${encodeURIComponent(currentSort)}&page=${next}`);
      const html = await res.text();

      const temp = document.createElement('div');
      temp.innerHTML = html;

      const sections = temp.querySelectorAll('section.tag-section');
      if (sections.length === 0) { noMore = true; return; }

      const container = document.querySelector('#sections-container');
      sections.forEach(sec => container.appendChild(sec));

      noMore = getHasMoreFromEl(temp) === false;
      currentPage = next;
    } catch (e) {
      console.error(e);
    } finally {
      isLoading = false;
    }
  }, { rootMargin: '400px' });

  if (target) observer.observe(target);
}

function getHasMoreFromEl(node) {
  const hv = node.querySelector('#has-more')?.dataset.value;
  return hv ? hv === 'true' : false;
}
