// HabitChallenge Navbar JavaScript

document.addEventListener('DOMContentLoaded', function() {
  initializeNavbar();
  setActiveMenu();
  // 페이지 로드시 한 번만 체크
  const accessToken = getCookie("access-token");
  updateAuthUI(!!accessToken);
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
  
  if (menu && toggle) {
    const isActive = menu.classList.contains('active');
    
    if (isActive) {
      // 메뉴 닫기
      menu.classList.remove('active');
      toggle.classList.remove('active');
      document.body.style.overflow = '';
    } else {
      // 메뉴 열기
      menu.classList.add('active');
      toggle.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  }
}

function closeMobileMenu() {
  const menu = document.getElementById('habitNavMenu');
  const toggle = document.querySelector('.habit-mobile-toggle');
  
  if (menu && toggle) {
    menu.classList.remove('active');
    toggle.classList.remove('active');
    document.body.style.overflow = '';
  }
}

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

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }
  return null;
}

function updateAuthUI(isAuthenticated) {
  const desktopActions = document.querySelector('.habit-nav-actions');
  const menu = document.getElementById('habitNavMenu');
  
  if (isAuthenticated) {
    // 데스크탑: 알림, 마이페이지, 로그아웃 버튼들
    if (desktopActions) {
      desktopActions.innerHTML = `
        <a href="/notifications" class="habit-btn-login">
          <i class="bi bi-bell"></i> 알림
        </a>
        <a href="/mypage" class="habit-btn-login">
          <i class="bi bi-person"></i> 마이페이지
        </a>
        <button onclick="logout()" class="habit-btn-login">
          <i class="bi bi-box-arrow-right"></i> 로그아웃
        </button>
      `;
    }

    // 모바일: 마이페이지와 로그아웃
    if (menu) {
      const existingAuthItems = menu.querySelectorAll('.nav-auth-item');
      existingAuthItems.forEach(item => item.remove());
      
      const profileItem = document.createElement('li');
      profileItem.className = 'nav-auth-item';
      profileItem.innerHTML = '<a href="/profile" class="habit-btn-login"><i class="bi bi-person"></i> 마이페이지</a>';
      menu.appendChild(profileItem);
      
      const logoutItem = document.createElement('li');
      logoutItem.className = 'nav-auth-item';
      logoutItem.innerHTML = '<button onclick="logout()" class="habit-btn-login"><i class="bi bi-box-arrow-right"></i> 로그아웃</button>';
      menu.appendChild(logoutItem);
    }
  } else {
    // 로그아웃 상태: 로그인만
    if (desktopActions) {
      desktopActions.innerHTML = `
        <a href="/login" class="habit-btn-login">로그인</a>
      `;
    }

    // 모바일: 로그인만
    if (menu) {
      const existingAuthItems = menu.querySelectorAll('.nav-auth-item');
      existingAuthItems.forEach(item => item.remove());
      
      const loginItem = document.createElement('li');
      loginItem.className = 'nav-auth-item';
      loginItem.innerHTML = '<a href="/login" class="habit-btn-login">로그인</a>';
      menu.appendChild(loginItem);
    }
  }
}

function logout() {
  fetch("/logout", {
    method: "POST",
    credentials: "include"
  }).then(res => {
    if (res.ok || res.status === 302) {
      // 쿠키 삭제
      document.cookie = "access-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      document.cookie = "refresh-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      
      // 홈페이지로 이동 (페이지 새로고침으로 UI 자동 업데이트)
      window.location.href = "/";
    } else {
      alert("로그아웃 실패");
    }
  }).catch(error => {
    console.error('로그아웃 오류:', error);
    alert("로그아웃 중 오류가 발생했습니다.");
  });
}

// 전역 함수로 노출
window.toggleMobileMenu = toggleMobileMenu;
window.logout = logout;
