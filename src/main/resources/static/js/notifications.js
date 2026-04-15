// Notifications Page JavaScript

let currentPage    = 0;
let isLastPage     = false;
let isLoading      = false;
let currentFilter  = 'all';

document.addEventListener('DOMContentLoaded', function() {
    loadNotifications(true);
    initFilterTabs();
    initMarkAllAsRead();
    initInfiniteScroll();
});

// 알림 목록 로드

async function loadNotifications(reset = false) {
    if (isLoading || isLastPage) return;
    isLoading = true;

    if (reset) {
        currentPage = 0;
        isLastPage  = false;
        document.getElementById('notificationsList').innerHTML = '';
    }

    try {
        const res = await secureFetch(`/api/notifications?page=${currentPage}&size=20`);
        if (!res || !res.ok) return;

        const data = await res.json();
        data.content.forEach(n => renderNotification(n, false));
        isLastPage = data.last;
        if (!isLastPage) currentPage++;

        updateEmptyState(document.querySelectorAll('.notification-item').length === 0);
        applyFilter(currentFilter);

    } catch (e) {
        console.error('[Notifications] 목록 로드 실패', e);
    } finally {
        isLoading = false;
    }
}

// 렌더링

function renderNotification(n, prepend = false) {
    const list = document.getElementById('notificationsList');
    const el   = createNotificationElement(n);
    if (prepend) {
        list.insertBefore(el, list.firstChild);
    } else {
        list.appendChild(el);
    }
    updateEmptyState(false);
}

function createNotificationElement(n) {
    const isRead    = n.isRead;
    const category  = n.filterCategory;
    const iconClass = n.iconClass || 'bi-bell';
    const timeStr   = formatTime(n.createdAt);

    const el = document.createElement('div');
    el.className = `notification-item ${isRead ? 'read' : 'unread'}`;
    el.setAttribute('data-id', n.id);
    el.setAttribute('data-category', category);

    el.innerHTML = `
    <div class="notification-icon ${category}-icon">
      <i class="bi ${iconClass}"></i>
    </div>
    <div class="notification-content">
      <div class="notification-header-text">
        <p class="notification-title">${escapeHtml(n.message)}</p>
        <span class="notification-time">${timeStr}</span>
      </div>
    </div>
    <button class="notification-delete" aria-label="삭제">
      <i class="bi bi-x"></i>
    </button>
  `;

    el.addEventListener('click', async function(e) {
        if (e.target.closest('.notification-delete')) return;

        const item = e.currentTarget;

        // 1. 즉시 UI 반영
        item.classList.remove('unread');
        item.classList.add('read');

        // 2. API 호출
        markAsRead(n.id, item);

        // 3. UI 반영 확인 후 이동 (300ms 후)
        if (n.redirectUrl) {
            setTimeout(() => { window.location.href = n.redirectUrl; }, 300);
        }
    });

    el.querySelector('.notification-delete').addEventListener('click', async function(e) {
        e.stopPropagation();

        try {
            await deleteNotification(n.id, el);

            // 성공 시 UI 제거
            el.style.opacity = '0';
            el.style.transform = 'translateX(100%)';
            el.style.transition = 'opacity .3s, transform .3s';

            setTimeout(() => {
                el.remove();
                updateEmptyState(document.querySelectorAll('.notification-item').length === 0);
            }, 300);

        } catch (e) {
            console.error(e);
        }
    });

    return el;
}

async function deleteNotification(notificationId, el) {
    try {
        await secureFetch(`/api/notifications/${notificationId}`, {
            method: 'DELETE'
        });
    } catch (e) {
        console.error('[Notifications] 삭제 실패', e);
    }
}

// SSE 실시간 알림 수신 시 navbar.js 가 호출
window.prependNotification = function(n) {
    renderNotification(n, true);
    applyFilter(currentFilter);
};

// 읽음 처리

async function markAsRead(notificationId, el) {
    try {
        await secureFetch(`/api/notifications/${notificationId}/read`, { method: 'PATCH' });
        el.classList.remove('unread');
        el.classList.add('read');
    } catch (e) {
        console.error('[Notifications] 읽음 처리 실패', e);
    }
}

function initMarkAllAsRead() {
    const btn = document.getElementById('markAllAsRead');
    if (!btn) return;
    btn.addEventListener('click', async function() {
        try {
            await secureFetch('/api/notifications/read-all', { method: 'PATCH' });
            document.querySelectorAll('.notification-item.unread').forEach(el => {
                el.classList.remove('unread');
                el.classList.add('read');
            });
            if (typeof window.updateBadge === 'function') window.updateBadge(0);
        } catch (e) {
            console.error('[Notifications] 전체 읽음 처리 실패', e);
        }
    });
}

// 필터 탭

function initFilterTabs() {
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', function() {
            document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            currentFilter = this.getAttribute('data-filter');
            applyFilter(currentFilter);
        });
    });
}

function applyFilter(filter) {
    let visible = 0;
    document.querySelectorAll('.notification-item').forEach(el => {
        const show = filter === 'all' || el.getAttribute('data-category') === filter;
        el.style.display = show ? 'flex' : 'none';
        if (show) visible++;
    });
    updateEmptyState(visible === 0);
}

function initInfiniteScroll() {
    window.addEventListener('scroll', function() {
        if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 200) {
            loadNotifications();
        }
    });
}

// 유틸

function updateEmptyState(isEmpty) {
    const empty = document.querySelector('.notifications-empty');
    const list  = document.getElementById('notificationsList');
    if (!empty) return;
    empty.style.display = isEmpty ? 'flex' : 'none';
    list.style.display  = isEmpty ? 'none' : 'flex';
}

function formatTime(isoString) {
    if (!isoString) return '';
    const diff = Date.now() - new Date(isoString).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1)  return '방금 전';
    if (m < 60) return `${m}분 전`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}시간 전`;
    const d = Math.floor(h / 24);
    if (d < 7)  return `${d}일 전`;
    return new Date(isoString).toLocaleDateString('ko-KR');
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
}