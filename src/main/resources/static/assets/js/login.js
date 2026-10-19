"use strict";

const form = document.getElementById("loginForm");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const rememberEmail = document.getElementById("rememberEmail");
const togglePassword = document.getElementById("togglePassword");
const loginMessage = document.getElementById("loginMessage");
const loginButton = document.getElementById("loginButton");
const loginButtonText = document.getElementById("loginButtonText");

// Chỉ ghi nhớ email nếu người dùng đồng ý. KHÔNG lưu mật khẩu.
try {
  const remembered = localStorage.getItem("fm_remembered_email");
  if (remembered) {
    emailInput.value = remembered;
    rememberEmail.checked = true;
  }
} catch (_) { /* Có thể browser đang chặn storage */ }

togglePassword.addEventListener("click", () => {
  const showing = passwordInput.type === "password";
  passwordInput.type = showing ? "text" : "password";
  togglePassword.textContent = showing ? "Ẩn" : "Hiện";
  togglePassword.setAttribute("aria-label", showing ? "Ẩn mật khẩu" : "Hiện mật khẩu");
  togglePassword.setAttribute("aria-pressed", String(showing));
});

function showMessage(text, type) {
  loginMessage.hidden = false;
  loginMessage.className = `form-message ${type}`;
  loginMessage.textContent = text; // textContent giúp không diễn giải thông báo như HTML
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const email = emailInput.value.trim();
  const password = passwordInput.value;

  if (!email || !emailInput.validity.valid) {
    showMessage("Bạn hãy nhập một địa chỉ email hợp lệ.", "error");
    emailInput.focus();
    return;
  }
  if (!password.trim()) {
    showMessage("Bạn chưa nhập mật khẩu.", "error");
    passwordInput.focus();
    return;
  }

  loginButton.disabled = true;
  loginButtonText.textContent = "Đang đăng nhập...";
  loginMessage.hidden = true;

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("Email hoặc mật khẩu không chính xác.");
      }
      if (response.status === 403) {
        throw new Error("Request bị từ chối (403). Hãy kiểm tra cấu hình Spring Security/JWT Filter.");
      }
      if (response.status >= 500) {
        throw new Error("Máy chủ gặp lỗi. Vui lòng thử lại sau.");
      }
      throw new Error(`Đăng nhập thất bại (HTTP ${response.status}).`);
    }

    const data = await response.json();
    if (!data.token || data.id == null) {
      throw new Error("Response đăng nhập thiếu token hoặc id.");
    }

    // DEMO: sessionStorage duy trì phiên khi chuyển trang trong cùng tab.
    // Token trong Web Storage có rủi ro XSS: không dùng mặc định này cho production.
    sessionStorage.setItem("fm_token", data.token);
    sessionStorage.setItem("fm_user", JSON.stringify({
      id: data.id, name: data.name, email: data.email
    }));

    try {
      if (rememberEmail.checked) localStorage.setItem("fm_remembered_email", email);
      else localStorage.removeItem("fm_remembered_email");
    } catch (_) { /* Không ảnh hưởng đến đăng nhập */ }

    passwordInput.value = "";
    showMessage(`Đăng nhập thành công! Chào ${data.name || data.email}. Bước tiếp theo chúng ta sẽ tạo dashboard.html.`, "success");

    // Khi đã xây dựng dashboard.html, bỏ comment dòng dưới để chuyển trang:
    window.location.assign("/dashboard.html");
  } catch (error) {
    showMessage(error.message || "Không thể kết nối đến máy chủ.", "error");
  } finally {
    loginButton.disabled = false;
    loginButtonText.textContent = "Đăng nhập";
  }
});
