document.addEventListener('DOMContentLoaded', function() {
    // --- 요소 선택 ---
    const peopleButtons = document.querySelectorAll('.people-btn');
    const peopleInput = document.getElementById('people-input');
    const timeButtons = document.querySelectorAll('.time-btn');
    const resetButton = document.getElementById('reset-btn');
    const reserveButton = document.getElementById('reserve-btn');
    const calendarGrid = document.querySelector('.calendar-grid');
    const monthYearDisplay = document.getElementById('month-year-display');
    const prevMonthBtn = document.querySelector('.nav-arrow.prev');
    const nextMonthBtn = document.querySelector('.nav-arrow.next');
    const selectionInfo = document.getElementById('current-selection-info');
    const timePeriodButtons = document.querySelectorAll('.time-period-btn');
    const amSlots = document.getElementById('am-slots');
    const pmSlots = document.getElementById('pm-slots');

    // --- 체크박스 요소들을 선택합니다. ---
    const emailCheckbox = document.querySelector('.checkbox-container input[type="checkbox"][checked]');
    const smsCheckbox = document.querySelector('.checkbox-container:nth-of-type(2) input[type="checkbox"]');

    // --- 상태 관리 변수 ---
    let currentDate = new Date();
    let selectedDate = null;
    let selectedTime = null;
    let selectedPeople = null;

    // --- 함수: 달력 생성 ---
    function generateCalendar(year, month) {
        calendarGrid.innerHTML = `
            <span class="day-of-week">일</span>
            <span class="day-of-week">월</span>
            <span class="day-of-week">화</span>
            <span class="day-of-week">수</span>
            <span class="day-of-week">목</span>
            <span class="day-of-week">금</span>
            <span class="day-of-week">토</span>
        `;

        const firstDayOfMonth = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const today = new Date();

        // 빈 칸 채우기
        for (let i = 0; i < firstDayOfMonth; i++) {
            const blankDay = document.createElement('span');
            blankDay.classList.add('day', 'inactive');
            calendarGrid.appendChild(blankDay);
        }

        // 날짜 채우기
        for (let day = 1; day <= daysInMonth; day++) {
            const dayElement = document.createElement('span');
            dayElement.classList.add('day');
            dayElement.textContent = day;
            dayElement.dataset.date = `${year}-${month + 1}-${day}`;

            const fullDate = new Date(year, month, day);

            // 지난 날짜는 비활성화
            if (fullDate < new Date(today.getFullYear(), today.getMonth(), today.getDate())) {
                dayElement.classList.add('inactive');
            } else {
                dayElement.addEventListener('click', () => {
                    document.querySelectorAll('.day').forEach(d => d.classList.remove('selected'));
                    dayElement.classList.add('selected');
                    selectedDate = dayElement.dataset.date;
                    updateSelectionInfo();
                    console.log("날짜 선택:", selectedDate);
                });
            }

            // 오늘 날짜 하이라이트
            if (fullDate.getFullYear() === today.getFullYear() && fullDate.getMonth() === today.getMonth() && fullDate.getDate() === today.getDate()) {
                dayElement.classList.add('today');
            }

            calendarGrid.appendChild(dayElement);
        }

        // 헤더 업데이트
        monthYearDisplay.textContent = `${year}. ${month + 1}`;
    }

    // --- 함수: 선택 정보 업데이트 ---
    function updateSelectionInfo() {
        let datePart = selectedDate ? `${new Date(selectedDate).getMonth() + 1}.${new Date(selectedDate).getDate()}(${['일', '월', '화', '수', '목', '금', '토'][new Date(selectedDate).getDay()]})` : '';
        let timePart = selectedTime ? ` / ${selectedTime}` : ' / 시간 선택하세요';

        selectionInfo.textContent = datePart + timePart;
    }

    // --- 이벤트 리스너 ---
    // 초기 달력 생성
    generateCalendar(currentDate.getFullYear(), currentDate.getMonth());

    // 이전 달 버튼
    prevMonthBtn.addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() - 1);
        generateCalendar(currentDate.getFullYear(), currentDate.getMonth());
    });

    // 다음 달 버튼
    nextMonthBtn.addEventListener('click', () => {
        currentDate.setMonth(currentDate.getMonth() + 1);
        generateCalendar(currentDate.getFullYear(), currentDate.getMonth());
    });

    // 인원수 버튼 클릭 로직
    peopleButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            peopleButtons.forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
            peopleInput.value = '';
            selectedPeople = parseInt(btn.dataset.value, 10);
            console.log("인원 선택 (버튼):", selectedPeople);
        });
    });

    // 인원수 입력 필드 로직
    peopleInput.addEventListener('input', () => {
        peopleButtons.forEach(b => b.classList.remove('selected'));
        const peopleCount = parseInt(peopleInput.value, 10);

        if (!isNaN(peopleCount) && peopleCount >= 1) {
            selectedPeople = peopleCount;
            console.log("인원 선택 (입력):", selectedPeople);
        } else {
            selectedPeople = null;
            console.log("유효하지 않은 인원 수");
        }
    });

    // 오전/오후 버튼 클릭 로직
    timePeriodButtons.forEach(periodBtn => {
        periodBtn.addEventListener('click', () => {
            timePeriodButtons.forEach(btn => btn.classList.remove('selected'));
            periodBtn.classList.add('selected');

            amSlots.classList.add('hidden');
            pmSlots.classList.add('hidden');

            const selectedPeriod = periodBtn.dataset.period;
            if (selectedPeriod === 'am') {
                amSlots.classList.remove('hidden');
            } else if (selectedPeriod === 'pm') {
                pmSlots.classList.remove('hidden');
            }

            document.querySelectorAll('.time-btn').forEach(timeBtn => {
                timeBtn.classList.remove('selected');
            });
            selectedTime = null;
            updateSelectionInfo();
        });
    });

    // 시간 선택
    timeButtons.forEach(timeBtn => {
        timeBtn.addEventListener('click', () => {
            timeButtons.forEach(t => t.classList.remove('selected'));
            timeBtn.classList.add('selected');
            selectedTime = timeBtn.dataset.time;
            updateSelectionInfo();
            console.log("시간 선택:", selectedTime);
        });
    });

    // 초기화 버튼
    resetButton.addEventListener('click', () => {
        peopleButtons.forEach(b => b.classList.remove('selected'));
        peopleInput.value = '';
        document.querySelectorAll('.day').forEach(d => d.classList.remove('selected'));
        timeButtons.forEach(t => t.classList.remove('selected'));
        timePeriodButtons.forEach(btn => btn.classList.remove('selected'));
        amSlots.classList.remove('hidden');
        pmSlots.classList.add('hidden');
        document.querySelector('.time-period-btn[data-period="am"]').classList.add('selected');

        selectedDate = null;
        selectedTime = null;
        selectedPeople = null;

        // ✅ 모든 체크박스 해제 후 '이메일'만 다시 체크합니다.
        if (emailCheckbox) {
            emailCheckbox.checked = true;
        }
        if (smsCheckbox) {
            smsCheckbox.checked = false;
        }

        updateSelectionInfo();
        console.log("모든 선택 초기화됨");
    });

    // 예약하기 버튼
    reserveButton.addEventListener('click', () => {
        if (selectedPeople && selectedDate && selectedTime) {
            alert(`예약이 완료되었습니다!\n인원: ${selectedPeople}명\n날짜: ${selectedDate}\n시간: ${selectedTime}`);
        } else {
            alert("인원, 날짜, 시간을 모두 선택해주세요.");
        }
    });
});