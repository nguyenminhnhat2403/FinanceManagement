"use strict";

(() => {
  const $ = (id) => document.getElementById(id);
  const isRegister = document.body.dataset.mode === "register";

  let challengeId = null;
  let challengeEmail = "";
  let busy = false;
  let complete = false;

  function normalizedEmail() {
    return $("email").value.trim().toLowerCase();
  }

  function message(text, error = false) {
    $("authMessage").hidden = false;
    $("authMessage").className = "form-message " + (error ? "error" : "success");
    $("authMessage").textContent = text;
  }

  async function post(url, body) {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    const contentType = response.headers.get("content-type") || "";
    let data = {};

    if (contentType.includes("json")) {
      data = await response.json().catch(() => ({}));
    }

    if (!response.ok) {
      let text = "Yêu cầu thất bại. Hãy kiểm tra thông tin.";

      if (response.status === 409) text = "Email đã được sử dụng.";
      else if (response.status === 429) text = "Yêu cầu mã quá nhiều lần. Hãy thử lại sau.";
      else if (response.status === 503) text = "Chưa gửi được email. Hãy thử lại sau.";
      else if (response.status >= 500) text = "Máy chủ gặp lỗi. Vui lòng thử lại.";
      else if (response.status === 400) text = "Kiểm tra mã, thời hạn mã và dữ liệu nhập.";

      throw new Error(data.detail || data.message || text);
    }

    return data;
  }

  $("email").addEventListener("input", () => {
    challengeId = null;
    challengeEmail = "";
    $("code").value = "";
    $("authMessage").hidden = true;
  });

  $("sendCodeButton").addEventListener("click", async () => {
    if (busy || complete) return;

    if (!$("email").reportValidity()) return;

    const email = normalizedEmail();

    if (!email.endsWith("@gmail.com")) {
      message("Hãy nhập địa chỉ @gmail.com.", true);
      return;
    }

    busy = true;
    $("authFields").disabled = true;
    const oldText = $("sendCodeButton").textContent;
    $("sendCodeButton").textContent = "Đang gửi mã...";

    try {
      const data = await post(
        isRegister ? "/api/auth/register-code" : "/api/auth/forgot-password",
        { email }
      );

      challengeId = data.challengeId;
      challengeEmail = email;
      $("code").value = "";

      message(data.message || "Đã gửi mã. Hãy kiểm tra Gmail.");
    } catch (error) {
      message(error.message || "Không kết nối được server.", true);
    } finally {
      busy = false;
      $("authFields").disabled = false;
      $("sendCodeButton").textContent = oldText;
    }
  });

  $("authForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    if (busy || complete || !$("authForm").reportValidity()) return;

    const email = normalizedEmail();

    if (!challengeId || challengeEmail !== email) {
      message("Hãy gửi mã xác nhận cho Gmail này trước.", true);
      return;
    }

    if ($("password").value !== $("confirmPassword").value) {
      message("Hai mật khẩu chưa giống nhau.", true);
      return;
    }

    if (new TextEncoder().encode($("password").value).length > 72) {
      message("Mật khẩu vượt quá giới hạn 72 byte.", true);
      return;
    }

    if (isRegister && !$("name").value.trim()) {
      message("Bạn chưa nhập họ tên.", true);
      return;
    }

    const body = {
      email,
      challengeId,
      code: $("code").value.trim()
    };

    if (isRegister) {
      body.name = $("name").value.trim();
      body.password = $("password").value;
    } else {
      body.newPassword = $("password").value;
    }

    busy = true;
    $("authFields").disabled = true;
    const oldText = $("submitButton").textContent;
    $("submitButton").textContent = "Đang xử lý...";

    try {
      await post(
        isRegister ? "/api/auth/register" : "/api/auth/reset-password",
        body
      );

      complete = true;
      challengeId = null;
      $("password").value = "";
      $("confirmPassword").value = "";

      if (!isRegister) {
        sessionStorage.removeItem("fm_token");
        sessionStorage.removeItem("fm_user");
      }

      message(isRegister
        ? "Đăng ký thành công. Bấm Đăng nhập bên dưới để tiếp tục."
        : "Đã đổi mật khẩu. Bấm Quay lại đăng nhập bên dưới.");

      // Không tự đăng nhập sau khi tạo tài khoản/đặt lại mật khẩu.
    } catch (error) {
      message(error.message || "Không kết nối được server.", true);
    } finally {
      busy = false;
      $("authFields").disabled = complete;
      $("submitButton").textContent = oldText;
    }
  });
})();

