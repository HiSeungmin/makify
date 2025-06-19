document.addEventListener("DOMContentLoaded", function () {
    const navbar = document.getElementById("mainNavbar");
    const navbarCollapse = document.getElementById("navbarNav");

    // Bootstrap collapse 이벤트에 따라 배경 처리
    navbarCollapse.addEventListener("show.bs.collapse", function () {
      navbar.classList.remove("bg-transparent");
      navbar.classList.add("bg-white");
    });

    navbarCollapse.addEventListener("hide.bs.collapse", function () {
      navbar.classList.remove("bg-white");
      navbar.classList.add("bg-transparent");
    });

    // 쿠키에서 토큰 확인 후 버튼 전환
    function getCookie(name) {
      const value = `; ${document.cookie}`;
      const parts = value.split(`; ${name}=`);
      if (parts.length === 2) return parts.pop().split(';').shift();
    }

    const accessToken = getCookie("access-token"); // 쿠키 이름 정확히 확인 필요

    const loginBtn = document.getElementById("login-btn");
    const logoutBtn = document.getElementById("logout-btn");
    const mypageBtn = document.getElementById("mypage-btn");

    if (accessToken) {
      loginBtn?.classList.add("d-none");
      logoutBtn?.classList.remove("d-none");
      mypageBtn?.classList.remove("d-none");
    } else {
      loginBtn?.classList.remove("d-none");
      logoutBtn?.classList.add("d-none");
      mypageBtn?.classList.add("d-none");
    }
  });

  // 로그아웃 함수
  function logout() {
    fetch("/logout", {
      method: "POST",
      credentials: "include"
    }).then(res => {
      if (res.ok || res.status === 302) {
        document.cookie = "access-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
        document.cookie = "refresh-token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
        location.href = "/";
      } else {
        alert("로그아웃 실패");
      }
    });
  }