// HabitChallenge Navbar JavaScript

document.addEventListener('DOMContentLoaded', function() {
  initializeNavbar();
  setActiveMenu();
  checkAuthenticationStatus();
});

function initializeNavbar() {
  const mobileToggle = document.querySelector('.habit-mobile-toggle');
  const menu = document.getElementById('habitNavMenu');
  
  if (mobileToggle && menu) {
    mobileToggle.addEventListener('click', function() {
      toggleMobileMenu();
    });
  }

  // Close mobile menu when clicking outside
  document.addEventListener('click', function(event) {
    const toggle = document.querySelector('.habit-mobile-toggle');
    
    if (menu && toggle && !menu.contains(event.target) && !toggle.contains(event.target)) {
      closeMobileMenu();
    }
  });

  // Close mobile menu when clicking on menu links
  if (menu) {
    menu.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', function() {
        closeMobileMenu();
      });
    });
  }
}

function toggleMobileMenu() {
  const menu = document.getElementById('habitNavMenu');
  const toggle = document.querySelector('.habit-mobile-toggle');
  const actions = document.querySelector('.habit-nav-actions');
  
  if (menu && toggle) {
    const isActive = menu.classList.contains('active');
    
    if (isActive) {
      // 메뉴 닫기
      menu.classList.remove('active');
      toggle.classList.remove('active');
      if (actions) actions.style.display = 'none';
      document.body.style.overflow = '';
    } else {
      // 메뉴 열기
      menu.classList.add('active');
      toggle.classList.add('active');
      if (actions) actions.style.display = 'flex';
      document.body.style.overflow = 'hidden';
    }
  }
}

function closeMobileMenu() {
  const menu = document.getElementById('habitNavMenu');
  const toggle = document.querySelector('.habit-mobile-toggle');
  const actions = document.querySelector('.habit-nav-actions');
  
  if (menu && toggle) {
    menu.classList.remove('active');
    toggle.classList.remove('active');
    if (actions) actions.style.display = 'none';
    document.body.style.overflow = '';
  }
}

function setActiveMenu() {
  const currentPath = window.location.pathname;
  const menuLinks = document.querySelectorAll('.habit-nav-menu a');
  
  menuLinks.forEach(link => {
    const href = link.getAttribute('href');
    
    // Remove any existing active class
    link.classList.remove('active');
    
    // Add active class based on current path
    if (currentPath === '/' && href === '/') {
      link.classList.add('active');
    } else if (currentPath !== '/' && href !== '/' && currentPath.startsWith(href)) {
      link.classList.add('active');
    }
  });
}

function checkAuthenticationStatus() {
  // 쿠키에서 토큰 확인
  const accessToken = getCookie("access-token");
  updateAuthUI(!!accessToken);
}

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  return null;
}

function updateAuthUI(isAuthenticated) {
  const navActions = document.querySelector('.habit-nav-actions');
  if (!navActions) return;

  if (isAuthenticated) {
    // 로그인된 상태
    navActions.innerHTML = `
      <a href="/notifications" class="habit-btn-login">
        <i class="bi bi-bell"></i> 알림
      </a>
      <a href="/profile" class="habit-btn-login">
        <i class="bi bi-person"></i> 프로필
      </a>
      <button onclick="logout()" class="habit-btn-login">
        <i class="bi bi-box-arrow-right"></i> 로그아웃
      </button>
    `;
  } else {
    // 로그아웃된 상태
    navActions.innerHTML = `
      <a href="/login" class="habit-btn-login">로그인</a>
      <a href="/register" class="habit-btn-signup">회원가입</a>
    `;
  }
}

// 로그아웃 함수 (기존 기능 유지)
function logout() {
  fetch("/logout", {
    method: "POST",
    credentials: "include"
  }).then(res => {
    if (res.ok || res.status === 302) {
      // 쿠키 삭제
      document.cookie = "access-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      document.cookie = "refresh-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      
      // UI 업데이트
      updateAuthUI(false);
      
      // 홈페이지로 이동
      window.location.href = "/";
    } else {
      alert("로그아웃 실패");
    }
  }).catch(error => {
    console.error('로그아웃 오류:', error);
    alert("로그아웃 중 오류가 발생했습니다.");
  });
}

// 페이지 로드 시와 주기적으로 인증 상태 체크
setInterval(checkAuthenticationStatus, 5000); // 5초마다 체크

// 전역 함수로 노출 (필요시 HTML에서 직접 호출 가능)
window.toggleMobileMenu = toggleMobileMenu;
window.logout = logout;
window.updateAuthUI = updateAuthUI;