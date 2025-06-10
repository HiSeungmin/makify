// api.js

// 자동 재발급 로직이 포함된 fetch 래퍼 함수
async function secureFetch(url, options = {}) {
  let response = await fetch(url, {
    ...options,
    credentials: "include"  // 쿠키를 반드시 포함해야 함
  });

  if (response.status === 401) {
    console.warn("[secureFetch] Access Token 만료 → 재발급 시도");

    // 1. refresh-token으로 재발급 요청
    const reissue = await fetch("/auth/reissue", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"  // 꼭 명시
      },
      body: JSON.stringify({}) // 비어 있어도 JSON 명시
    });

    if (reissue.ok) {
      console.info("[secureFetch] 재발급 성공 → 원래 요청 재시도");
      response = await fetch(url, {
        ...options,
        credentials: "include"
      });
    } else {
      console.error("[secureFetch] 재발급 실패 → 로그인 페이지로 이동");
      alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
      window.location.href = "/login";
      return;
    }
  }

  return response;
}