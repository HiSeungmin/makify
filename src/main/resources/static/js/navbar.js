// Navbar JavaScript

document.addEventListener('DOMContentLoaded', function() {
  initializeNavbar();
  setActiveMenu();

  // 로그인 상태면 SSE 연결
  const bell = document.getElementById('notificationBell');
  if (bell) {
    initSse();
  }
});

// 모바일 메뉴

function initializeNavbar() {
  // 기존 nav-auth-item 방식 제거 — navbar.html의 mobile-menu-drawer로 대체
}

function toggleMobileMenu() {
  const overlay = document.getElementById('mobileMenuOverlay');
  const drawer  = document.getElementById('mobileMenuDrawer');
  const toggle  = document.querySelector('.habit-mobile-toggle');
  const isActive = drawer.classList.contains('active');
  if (isActive) {
    closeMobileMenu();
  } else {
    overlay.classList.add('active');
    drawer.classList.add('active');
    toggle.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeMobileMenu() {
  const overlay = document.getElementById('mobileMenuOverlay');
  const drawer  = document.getElementById('mobileMenuDrawer');
  const toggle  = document.querySelector('.habit-mobile-toggle');
  if (!overlay || !drawer) return;
  overlay.classList.remove('active');
  drawer.classList.remove('active');
  toggle && toggle.classList.remove('active');
  document.body.style.overflow = '';
}

// 활성 메뉴

function setActiveMenu() {
  const currentPath = window.location.pathname;
  const menuLinks = document.querySelectorAll('.habit-nav-menu a');

  menuLinks.forEach(link => {
    const href = link.getAttribute('href');
    link.classList.remove('active');
    if (currentPath === '/' && href === '/') {
      link.classList.add('active');
    } else if (currentPath !== '/' && href !== '/' && currentPath.startsWith(href)) {
      link.classList.add('active');
    }
  });
}

// SSE

let sseSource = null;

function initSse() {
  if (sseSource) return;

  sseSource = new EventSource('/api/notifications/subscribe', { withCredentials: true });

  sseSource.addEventListener('connect', () => {
    console.debug('[SSE] connected');
  });

  sseSource.addEventListener('unread-count', (e) => {
    updateBadge(Number(e.data));
  });

  sseSource.addEventListener('notification', (e) => {
    const notification = JSON.parse(e.data);
    if (typeof window.prependNotification === 'function') {
      window.prependNotification(notification);
    }
  });

  sseSource.onerror = () => {
    console.warn('[SSE] 연결 끊김, 30초 후 재연결 시도');
    sseSource.close();
    sseSource = null;
    setTimeout(initSse, 30000);
  };
}

function updateBadge(count) {
  const badge = document.getElementById('notificationBadge');
  if (!badge) return;
  if (count > 0) {
    badge.textContent = count > 99 ? '99+' : count;
    badge.style.display = 'flex';
  } else {
    badge.style.display = 'none';
  }
}

// 로그아웃

function logout() {
  fetch("/logout", { method: "POST", credentials: "include" }).then(res => {
    if (res.ok || res.status === 302) {
      if (sseSource) { sseSource.close(); sseSource = null; }
      document.cookie = "access-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      document.cookie = "refresh-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      window.location.href = "/";
    } else {
      alert("로그아웃 실패");
    }
  }).catch(error => {
    console.error('로그아웃 오류:', error);
    alert("로그아웃 중 오류가 발생했습니다.");
  });
}

window.toggleMobileMenu = toggleMobileMenu;
window.logout = logout;
window.updateBadge = updateBadge;
