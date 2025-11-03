// Notifications Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initNotifications();
});

function initNotifications() {
    // 필터 탭 이벤트
    const filterTabs = document.querySelectorAll('.filter-tab');
    filterTabs.forEach(tab => {
        tab.addEventListener('click', handleFilterClick);
    });

    // 삭제 버튼 이벤트
    const deleteButtons = document.querySelectorAll('.notification-delete');
    deleteButtons.forEach(btn => {
        btn.addEventListener('click', handleDeleteNotification);
    });

    // 알림 아이템 클릭 이벤트 (읽음 표시)
    const notificationItems = document.querySelectorAll('.notification-item');
    notificationItems.forEach(item => {
        item.addEventListener('click', handleNotificationClick);
    });

    // 모두 읽음으로 표시 버튼
    const markAllBtn = document.getElementById('markAllAsRead');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', handleMarkAllAsRead);
    }
}

/**
 * 필터 탭 클릭 처리
 */
function handleFilterClick(e) {
    const clickedTab = e.currentTarget;
    const filterValue = clickedTab.getAttribute('data-filter');

    // 활성 탭 변경
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    clickedTab.classList.add('active');

    // 알림 필터링
    filterNotifications(filterValue);
}

/**
 * 알림 필터링
 */
function filterNotifications(filterValue) {
    const notificationItems = document.querySelectorAll('.notification-item');
    let visibleCount = 0;

    notificationItems.forEach(item => {
        if (filterValue === 'all') {
            item.style.display = 'flex';
            visibleCount++;
        } else {
            const category = item.getAttribute('data-category');
            if (category === filterValue) {
                item.style.display = 'flex';
                visibleCount++;
            } else {
                item.style.display = 'none';
            }
        }
    });

    // 필터 결과가 없으면 빈 상태 표시
    updateEmptyState(visibleCount === 0);
}

/**
 * 알림 삭제 처리
 */
function handleDeleteNotification(e) {
    e.stopPropagation();
    const notificationItem = e.currentTarget.closest('.notification-item');
    
    // 애니메이션 효과
    notificationItem.style.opacity = '0';
    notificationItem.style.transform = 'translateX(100%)';

    setTimeout(() => {
        notificationItem.remove();
        // 남은 알림이 없으면 빈 상태 표시
        const remainingNotifications = document.querySelectorAll('.notification-item');
        if (remainingNotifications.length === 0) {
            updateEmptyState(true);
        }
    }, 300);
}

/**
 * 알림 클릭 처리 (읽음 표시)
 */
function handleNotificationClick(e) {
    if (e.target.closest('.notification-delete')) {
        return; // 삭제 버튼 클릭은 무시
    }

    const notificationItem = e.currentTarget;
    if (notificationItem.classList.contains('unread')) {
        notificationItem.classList.remove('unread');
        notificationItem.classList.add('read');
    }
}

/**
 * 모든 알림을 읽음으로 표시
 */
function handleMarkAllAsRead() {
    const unreadNotifications = document.querySelectorAll('.notification-item.unread');
    unreadNotifications.forEach(item => {
        item.classList.remove('unread');
        item.classList.add('read');
    });
}

/**
 * 빈 상태 업데이트
 */
function updateEmptyState(isEmpty) {
    const emptyState = document.querySelector('.notifications-empty');
    const notificationsList = document.getElementById('notificationsList');

    if (isEmpty) {
        emptyState.style.display = 'flex';
        notificationsList.style.display = 'none';
    } else {
        emptyState.style.display = 'none';
        notificationsList.style.display = 'flex';
    }
}

/**
 * (옵션) 실시간 알림 추가 함수
 * 나중에 WebSocket이나 Server-Sent Events와 통합할 수 있습니다.
 */
function addNotification(notification) {
    const notificationsList = document.getElementById('notificationsList');
    
    const notificationItem = document.createElement('div');
    notificationItem.className = 'notification-item unread';
    notificationItem.setAttribute('data-category', notification.category);
    
    const iconClass = getIconClass(notification.category);
    const iconColor = getIconColor(notification.category);
    
    notificationItem.innerHTML = `
        <div class="notification-icon ${iconColor}">
            <i class="bi bi-${iconClass}"></i>
        </div>
        <div class="notification-content">
            <div class="notification-header-text">
                <p class="notification-title">${notification.title}</p>
                <span class="notification-time">방금 전</span>
            </div>
            <p class="notification-description">${notification.description}</p>
        </div>
        <button class="notification-delete" aria-label="삭제">
            <i class="bi bi-x"></i>
        </button>
    `;
    
    // 이벤트 리스너 추가
    const deleteBtn = notificationItem.querySelector('.notification-delete');
    deleteBtn.addEventListener('click', handleDeleteNotification);
    
    notificationItem.addEventListener('click', handleNotificationClick);
    
    // 목록의 맨 앞에 추가
    if (notificationsList.firstChild) {
        notificationsList.insertBefore(notificationItem, notificationsList.firstChild);
    } else {
        notificationsList.appendChild(notificationItem);
    }
    
    // 빈 상태 제거
    updateEmptyState(false);
}

/**
 * 카테고리에 따른 아이콘 클래스 반환
 */
function getIconClass(category) {
    const iconMap = {
        'challenge': 'target',
        'payment': 'credit-card',
        'community': 'chat-dots',
        'system': 'gear'
    };
    return iconMap[category] || 'bell';
}

/**
 * 카테고리에 따른 아이콘 색상 클래스 반환
 */
function getIconColor(category) {
    const colorMap = {
        'challenge': 'challenge-icon',
        'payment': 'payment-icon',
        'community': 'community-icon',
        'system': 'system-icon'
    };
    return colorMap[category] || 'challenge-icon';
}

/**
 * (옵션) 테스트 함수 - 콘솔에서 호출 가능
 * testAddNotification()
 */
function testAddNotification() {
    const testNotifications = [
        {
            category: 'challenge',
            title: '새로운 챌린지 추천',
            description: '당신이 좋아할 만한 챌린지를 찾았습니다.'
        },
        {
            category: 'payment',
            title: '결제 예약 알림',
            description: '내일 정기 구독 결제가 예정되어 있습니다.'
        },
        {
            category: 'community',
            title: '새로운 팔로워',
            description: '누군가 당신을 팔로우했습니다.'
        },
        {
            category: 'system',
            title: '시스템 유지보수',
            description: '내일 새벽 2시부터 2시간간 정기 유지보수가 있습니다.'
        }
    ];
    
    const randomNotification = testNotifications[Math.floor(Math.random() * testNotifications.length)];
    addNotification(randomNotification);
}
