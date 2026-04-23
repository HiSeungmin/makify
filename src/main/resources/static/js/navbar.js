// Navbar JavaScript

document.addEventListener('DOMContentLoaded', function() {
  initializeNavbar();
  setActiveMenu();

  // 로그인 상태면 SSE 연결
  const bell = document.getElementById('notificationBell');
  if (bell) {
    initSse();
    initPushSubscription();
  }

  showIosInstallBanner();
});

// Web Push 구독

async function initPushSubscription() {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)) return;

  try {
    const reg = await navigator.serviceWorker.register('/service_worker.js');
    await navigator.serviceWorker.ready;

    // 이미 구독 중이면 서버에 재등록 (키 갱신 대비)
    let sub = await reg.pushManager.getSubscription();

    if (!sub) {
      // 권한 요청
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') return;

      // VAPID public key 조회
      const res = await fetch('/api/push/vapid-key');
      const { publicKey } = await res.json();

      sub = await reg.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(publicKey)
      });
    }

    // 서버에 구독 정보 전송
    await fetch('/api/push/subscribe', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(sub.toJSON())
    });

  } catch (e) {
    console.warn('[Push] 구독 실패:', e);
  }
}

function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(base64);
  const arr = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) arr[i] = raw.charCodeAt(i);
  return arr;
}

// iOS PWA 설치 안내 배너

function showIosInstallBanner() {
  // iOS Safari에서만 + standalone 모드가 아닐 때만 + 이전에 닫지 않았을 때만
  const isIos = /iPhone|iPad|iPod/.test(navigator.userAgent);
  const isStandalone = window.navigator.standalone === true;
  const dismissed = localStorage.getItem('makify-ios-banner-dismissed');

  if (!isIos || isStandalone || dismissed) return;

  const banner = document.createElement('div');
  banner.id = 'iosBanner';
  banner.innerHTML = `
        <div style="
            position:fixed; bottom:0; left:0; right:0; z-index:9999;
            background:var(--color-background-secondary, #f8f8f8);
            border-top:1px solid var(--color-border-tertiary, #ddd);
            padding:16px 20px; display:flex; align-items:center; gap:12px;
            font-family:'Noto Sans KR',sans-serif; font-size:14px;
            color:var(--color-text-primary, #333);
        ">
            <div style="font-size:28px;">📲</div>
            <div style="flex:1;">
                <strong>Makify를 홈 화면에 추가하세요</strong><br>
                <span style="font-size:12px; color:var(--color-text-secondary, #666);">
                    하단 <strong>공유 버튼</strong> → <strong>홈 화면에 추가</strong>를 눌러
                    푸시 알림을 받을 수 있어요
                </span>
            </div>
            <button onclick="dismissIosBanner()" style="
                background:none; border:none; font-size:20px; cursor:pointer;
                color:var(--color-text-secondary, #999); padding:4px;
            ">&times;</button>
        </div>
    `;
  document.body.appendChild(banner);
}

function dismissIosBanner() {
  document.getElementById('iosBanner')?.remove();
  localStorage.setItem('makify-ios-banner-dismissed', 'true');
}

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
window.dismissIosBanner = dismissIosBanner;
