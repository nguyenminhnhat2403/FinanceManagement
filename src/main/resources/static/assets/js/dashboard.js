"use strict";

(async function () {
    const $ = (id) => document.getElementById(id);

    const token = sessionStorage.getItem("fm_token");

    let user;

    try {
        user = JSON.parse(sessionStorage.getItem("fm_user") || "null");
    } catch {
        user = null;
    }

    if (!token || !user || user.id == null) {
        window.location.replace("/login.html");
        return;
    }

    const categories = {
        FOOD_COFFEE: { name: "Ăn uống", color: "#2ed49b" },
        TRANSPORT: { name: "Di chuyển", color: "#3594ed" },
        SHOPPING: { name: "Mua sắm", color: "#a78bfa" },
        ENTERTAINMENT: { name: "Giải trí", color: "#f4ad49" },
        EDUCATION: { name: "Giáo dục", color: "#7b64d8" },
        HOUSING: { name: "Nhà ở", color: "#164c83" },
        HEALTH: { name: "Sức khỏe", color: "#f47d94" },
        OTHER: { name: "Khác", color: "#aeb8c8" }
    };

    const currency = new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    });

    const money = (value) => currency.format(value);

    const categoryOf = (key) => categories[key] || categories.OTHER;

    const totalOf = (expenses) =>
        expenses.reduce((total, expense) => total + Number(expense.amount), 0);

    const today = new Date();

    let yearExpenses = [];
    let previousDecember = [];

    let categoryChart = null;
    let monthlyChart = null;

    let requestVersion = 0;
    let saving = false;
    let editingExpenseId = null;
    let deleting = false;

    let recognition = null;
    let listening = false;

    const dialog = $("expenseDialog");

    // Hiển thị user bằng textContent để nội dung được coi là văn bản.
    $("userName").textContent = user.name || user.email;
    $("userEmail").textContent = user.email || "";
    $("avatar").textContent = (user.name || user.email || "U")
        .charAt(0)
        .toUpperCase();

    for (let month = 1; month <= 12; month++) {
        $("monthSelect").add(new Option(`Tháng ${month}`, month));
    }

    $("monthSelect").value = String(today.getMonth() + 1);
    $("yearInput").value = String(today.getFullYear());

    for (const [key, category] of Object.entries(categories)) {
        // Ô danh mục trong form tạo khoản chi
        $("categoryInput").add(new Option(category.name, key));

        // Ô lọc danh mục phía trên bảng
        $("categoryFilter").add(new Option(category.name, key));
    }

    function selectedYear() {
        return Number($("yearInput").value);
    }

    function selectedMonth() {
        return Number($("monthSelect").value);
    }

    function periodKey(year, month) {
        return `${String(year).padStart(4, "0")}-${String(month).padStart(2, "0")}`;
    }

    function expensesInMonth(expenses, year, month) {
        const key = periodKey(year, month);

        return expenses.filter((expense) =>
            String(expense.expenseDate || "").startsWith(key)
        );
    }

    function showPageMessage(message, error = false) {
        $("pageMessage").textContent = message;
        $("pageMessage").classList.toggle("error", error);
    }

    function logout() {
        sessionStorage.removeItem("fm_token");
        sessionStorage.removeItem("fm_user");
        window.location.replace("/login.html");
    }

    // Mọi API cần xác thực đều đi qua hàm này.
    async function api(path, options = {}) {
        const response = await fetch(path, {
            ...options,
            headers: {
                Authorization: `Bearer ${token}`,
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...(options.headers || {})
            }
        });

        if (response.status === 401) {
            logout();
            throw new Error("Phiên đăng nhập đã hết hạn.");
        }

        if (response.status === 403) {
            throw new Error(
                "Không có quyền truy cập. Nếu phiên đã hết hạn, hãy đăng nhập lại."
            );
        }

        if (!response.ok) {
            throw new Error(
                `Yêu cầu thất bại (HTTP ${response.status}). Vui lòng kiểm tra dữ liệu hoặc thử lại.`
            );
        }

        if (response.status === 204) {
            return null;
        }

        return response.json();
    }

    function clearDashboard() {
        yearExpenses = [];
        previousDecember = [];

        for (const id of [
            "currentTotal",
            "previousTotal",
            "differenceTotal",
            "differenceNote"
        ]) {
            $(id).textContent = "—";
        }

        $("expenseRows").replaceChildren();
        $("categoryLegend").replaceChildren();
        $("expenseCount").textContent = "—";
        $("listEmpty").hidden = true;
        $("categoryEmpty").hidden = true;

        categoryChart?.destroy();
        monthlyChart?.destroy();

        categoryChart = null;
        monthlyChart = null;
    }

    async function loadData(successMessage = "") {
        const version = ++requestVersion;
        const year = selectedYear();
        const month = selectedMonth();

        clearDashboard();

        if (!Number.isInteger(year) || year < 1 || year > 9999) {
            showPageMessage("Nhập năm từ 1 đến 9999.", true);
            return;
        }

        $("searchInput").disabled = true;
        showPageMessage("Đang tải chi tiêu...");

        try {
            const base = `/api/users/${encodeURIComponent(user.id)}/expenses`;

            // Lấy cả năm để vẽ 12 cột bằng API đã có.
            const yearRequest = api(`${base}?year=${year}`);

            // Tháng 1 cần so sánh với tháng 12 năm trước.
            const previousRequest =
                month === 1 && year > 1
                    ? api(`${base}?year=${year - 1}&month=12`)
                    : Promise.resolve([]);

            const [all, december] = await Promise.all([
                yearRequest,
                previousRequest
            ]);

            // Không cho response cũ ghi đè khi người dùng đổi bộ lọc nhanh.
            if (version !== requestVersion) return;

            if (!Array.isArray(all) || !Array.isArray(december)) {
                throw new Error("API danh sách trả về dữ liệu không đúng định dạng.");
            }

            yearExpenses = all;
            previousDecember = december;

            renderDashboard();

            showPageMessage(
                successMessage || `Đã tải chi tiêu năm ${year}.`
            );
        } catch (error) {
            if (version === requestVersion) {
                showPageMessage(
                    `${successMessage ? successMessage + " " : ""}${error.message}`,
                    true
                );
            }
        } finally {
            if (version === requestVersion) {
                $("searchInput").disabled = false;
            }
        }
    }

    function renderDashboard() {
        const year = selectedYear();
        const month = selectedMonth();

        const current = expensesInMonth(yearExpenses, year, month);

        const previous =
            month === 1
                ? previousDecember
                : expensesInMonth(yearExpenses, year, month - 1);

        const currentTotal = totalOf(current);
        const previousTotal = totalOf(previous);
        const difference = currentTotal - previousTotal;

        $("currentLabel").textContent = `Chi tiêu tháng ${month}/${year}`;

        $("previousLabel").textContent =
            month === 1
                ? `Chi tiêu tháng 12/${year - 1}`
                : `Chi tiêu tháng ${month - 1}/${year}`;

        $("currentTotal").textContent = money(currentTotal);
        $("previousTotal").textContent = money(previousTotal);

        const isCurrentMonth =
            year === today.getFullYear() &&
            month === today.getMonth() + 1;

        $("periodNote").textContent = isCurrentMonth
            ? "Tháng đang diễn ra, chưa đủ tháng"
            : "Tổng theo tháng đã chọn";

        const differenceElement = $("differenceTotal");

        differenceElement.textContent =
            `${difference > 0 ? "+" : difference < 0 ? "−" : ""}` +
            money(Math.abs(difference));

        differenceElement.className =
            difference > 0 ? "increase" : difference < 0 ? "decrease" : "";

        if (previousTotal === 0) {
            $("differenceNote").textContent =
                "Tháng trước bằng 0, không tính tỷ lệ";
        } else if (difference === 0) {
            $("differenceNote").textContent = "Không thay đổi";
        } else {
            const percent = Math.abs(difference / previousTotal * 100);

            $("differenceNote").textContent =
                `${difference < 0 ? "Giảm" : "Tăng"} ` +
                `${percent.toLocaleString("vi-VN", {
                    maximumFractionDigits: 1
                })}% so với tháng trước`;
        }

        $("categoryPeriod").textContent = `Tháng ${month}, ${year}`;
        $("yearLabel").textContent = `Năm ${year}`;

        renderList();
        renderCharts(current);
    }
    function openEditExpense(expense) {
        if (saving || deleting) return;

        editingExpenseId = expense.id;

        $("expenseForm").reset();
        $("formMessage").textContent = "";
        $("transcriptInput").value = "";

        $("voiceSection").hidden = true;
        $("formTitle").textContent = "Sửa khoản chi";
        $("saveButton").textContent = "Lưu thay đổi";

        $("amountInput").value = expense.amount;
        $("categoryInput").value = expense.category;
        $("descriptionInput").value = expense.description || "";

        const dateTime = String(expense.expenseDate || "");

        $("dateInput").value = dateTime.slice(0, 10);
        $("timeInput").value = dateTime.slice(11, 16);

        dialog.showModal();
        $("amountInput").focus();
    }

    async function removeExpense(expense, button) {
        if (saving || deleting) return;

        const confirmed = window.confirm(
            `Xóa khoản chi "${expense.description}" ` +
            `với số tiền ${money(Number(expense.amount))}?\n\n` +
            "Khoản chi sẽ bị xóa khỏi dữ liệu."
        );

        if (!confirmed) return;

        deleting = true;
        button.disabled = true;
        button.textContent = "Đang xóa...";

        try {
            await api(
                `/api/users/${encodeURIComponent(user.id)}` +
                `/expenses/${encodeURIComponent(expense.id)}`,
                { method: "DELETE" }
            );

            await loadData("Đã xóa khoản chi.");
        } catch (error) {
            showPageMessage(error.message, true);
        } finally {
            deleting = false;
            button.disabled = false;
            button.textContent = "Xóa";
        }
    }

    function renderList() {
        const query = $("searchInput").value.trim().toLocaleLowerCase("vi");

        const expenses = expensesInMonth(
            yearExpenses,
            selectedYear(),
            selectedMonth()
        );

        const selectedCategory = $("categoryFilter").value;

        const filtered = expenses.filter((expense) => {
            // Chọn Tất cả hoặc khoản chi thuộc đúng danh mục được chọn
            const matchesCategory =
                selectedCategory === "" ||
                expense.category === selectedCategory;

            // Đồng thời phải khớp nội dung tìm kiếm
            const text =
                `${expense.description || ""} ${categoryOf(expense.category).name}`;

            const matchesSearch =
                text.toLocaleLowerCase("vi").includes(query);

            return matchesCategory && matchesSearch;
        });

        filtered.sort((a, b) =>
            String(b.expenseDate).localeCompare(String(a.expenseDate))
        );

        const tbody = $("expenseRows");
        tbody.replaceChildren();

        for (const expense of filtered) {
            const row = document.createElement("tr");

            const rawDate = String(expense.expenseDate || "").slice(0, 10);
            const parts = rawDate.split("-");
            const displayDate =
                parts.length === 3 ? parts.reverse().join("/") : "—";

            const values = [
                displayDate,
                expense.description || "",
                categoryOf(expense.category).name,
                `−${money(Number(expense.amount))}`
            ];

            values.forEach((value, index) => {
                const cell = document.createElement("td");
                cell.textContent = value;

                if (index === 3) {
                    cell.className = "align-right money-out";
                }

                row.appendChild(cell);
            });
            const actionsCell = document.createElement("td");

            const actions = document.createElement("div");
            actions.className = "expense-actions";

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "button";
            editButton.textContent = "Sửa";

            editButton.addEventListener("click", () => {
                openEditExpense(expense);
            });

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.className = "button danger-button";
            deleteButton.textContent = "Xóa";

            deleteButton.addEventListener("click", () => {
                removeExpense(expense, deleteButton);
            });

            actions.append(editButton, deleteButton);
            actionsCell.appendChild(actions);
            row.appendChild(actionsCell);

            tbody.appendChild(row);
        }

        $("expenseCount").textContent =
            `${filtered.length}/${expenses.length} khoản chi`;

        $("listEmpty").hidden = filtered.length !== 0;
    }

    function renderCharts(currentExpenses) {
        const totals = {};

        for (const expense of currentExpenses) {
            const key = expense.category;

            totals[key] = (totals[key] || 0) + Number(expense.amount);
        }

        const entries = Object.entries(totals)
            .sort((a, b) => b[1] - a[1]);

        const total = totalOf(currentExpenses);
        const legend = $("categoryLegend");
        legend.replaceChildren();

        for (const [key, amount] of entries) {
            const category = categoryOf(key);

            const row = document.createElement("div");
            row.className = "legend-row";

            const dot = document.createElement("span");
            dot.className = "color-dot";
            dot.style.backgroundColor = category.color;

            const label = document.createElement("span");
            label.textContent = category.name;

            const value = document.createElement("strong");
            const percent = total > 0 ? amount / total * 100 : 0;

            value.textContent =
                `${money(amount)} · ${percent.toLocaleString("vi-VN", {
                    maximumFractionDigits: 1
                })}%`;

            row.append(dot, label, value);
            legend.appendChild(row);
        }

        $("categoryEmpty").hidden = entries.length !== 0;

        if (!window.Chart) {
            $("chartMessage").textContent =
                "Không tải được thư viện biểu đồ. Kiểm tra kết nối mạng rồi tải lại trang.";
            return;
        }

        $("chartMessage").textContent = "";

        categoryChart?.destroy();
        monthlyChart?.destroy();

        categoryChart = new Chart($("categoryChart"), {
            type: "doughnut",
            data: {
                labels: entries.map(([key]) => categoryOf(key).name),
                datasets: [{
                    data: entries.map(([, amount]) => amount),
                    backgroundColor: entries.map(([key]) => categoryOf(key).color),
                    borderWidth: 3,
                    borderColor: "#ffffff"
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "68%",
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) =>
                                `${context.label}: ${money(context.parsed)}`
                        }
                    }
                }
            }
        });

        const monthTotals = Array.from({ length: 12 }, (_, index) =>
            totalOf(expensesInMonth(
                yearExpenses,
                selectedYear(),
                index + 1
            ))
        );

        monthlyChart = new Chart($("monthlyChart"), {
            type: "bar",
            data: {
                labels: monthTotals.map((_, index) => `T${index + 1}`),
                datasets: [{
                    label: "Chi tiêu",
                    data: monthTotals,
                    backgroundColor: monthTotals.map((_, index) =>
                        index + 1 === selectedMonth() ? "#009966" : "#a7e7d1"
                    ),
                    borderRadius: 5,
                    maxBarThickness: 34
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => money(context.parsed.y)
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false }
                    },
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) =>
                                (Number(value) / 1000000).toLocaleString("vi-VN")
                        },
                        grid: { color: "#edf2f7" }
                    }
                }
            }
        });
    }

    function setLocalDateTime() {
        const now = new Date();
        const pad = (value) => String(value).padStart(2, "0");

        $("dateInput").value =
            `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;

        $("timeInput").value =
            `${pad(now.getHours())}:${pad(now.getMinutes())}`;
    }

    function openExpenseForm(voiceMode) {
        editingExpenseId = null;
        $("saveButton").textContent = "Lưu khoản chi";

        $("expenseForm").reset();
        $("formMessage").textContent = "";
        $("transcriptInput").value = "";

        $("voiceSection").hidden = !voiceMode;
        $("formTitle").textContent =
            voiceMode ? "Xác nhận khoản chi" : "Thêm khoản chi";

        $("voiceStatus").textContent = "Bấm Bắt đầu nói để nhập giọng nói.";

        setLocalDateTime();
        dialog.showModal();

        if (!voiceMode) $("amountInput").focus();
    }

    function stopRecognition() {
        if (recognition && listening) {
            recognition.abort();
        }
    }

    function closeExpenseForm() {
        if (saving) return;

        stopRecognition();
        dialog.close();
    }

    dialog.addEventListener("cancel", (event) => {
        if (saving) {
            event.preventDefault();
        } else {
            stopRecognition();
        }
    });

    function setSaving(value) {
        saving = value;

        for (const id of [
            "saveButton",
            "cancelButton",
            "closeDialog",
            "recordButton"
        ]) {
            $(id).disabled = value;
        }

        $("saveButton").textContent = value
            ? "Đang lưu..."
            : editingExpenseId !== null
                ? "Lưu thay đổi"
                : "Lưu khoản chi";
    }

    $("expenseForm").addEventListener("submit", async (event) => {
        event.preventDefault();

        if (
            saving ||
            deleting ||
            !$("expenseForm").reportValidity()
        ) {
            return;
        }

        const description = $("descriptionInput").value.trim();
        const amount = Number($("amountInput").value);

        if (!description || !Number.isFinite(amount) || amount <= 0) {
            $("formMessage").textContent =
                "Nhập nội dung và số tiền lớn hơn 0.";
            return;
        }

        const expenseDate =
            `${$("dateInput").value}T${$("timeInput").value}:00`;

        const body = {
            categoryType: $("categoryInput").value,
            amount,
            description,
            expenseDate
        };

        const isEditing = editingExpenseId !== null;

        const baseUrl =
            `/api/users/${encodeURIComponent(user.id)}/expenses`;

        const url = isEditing
            ? `${baseUrl}/${encodeURIComponent(editingExpenseId)}`
            : baseUrl;

        stopRecognition();
        setSaving(true);
        $("formMessage").textContent = "";

        try {
            await api(url, {
                method: isEditing ? "PUT" : "POST",
                body: JSON.stringify(body)
            });
        } catch (error) {
            $("formMessage").textContent = error.message;
            setSaving(false);
            return;
        }

        // Server đã xác nhận lưu thành công
        setSaving(false);
        dialog.close();
        editingExpenseId = null;

        // Chuyển sang tháng của khoản vừa lưu
        $("yearInput").value = expenseDate.slice(0, 4);
        $("monthSelect").value =
            String(Number(expenseDate.slice(5, 7)));

        // Xóa bộ lọc để thấy khoản vừa cập nhật
        $("searchInput").value = "";
        $("categoryFilter").value = "";

        await loadData(
            isEditing
                ? "Đã cập nhật khoản chi."
                : "Đã thêm khoản chi."
        );
    });

    // Chỉ nhận giọng nói thành văn bản, chưa tự phân loại bằng AI.
    $("recordButton").addEventListener("click", () => {
        const SpeechRecognition =
            window.SpeechRecognition || window.webkitSpeechRecognition;

        if (!SpeechRecognition) {
            $("voiceStatus").textContent =
                "Trình duyệt chưa hỗ trợ. Bạn có thể nhập tay bên dưới.";
            return;
        }

        if (listening) {
            recognition.stop();
            return;
        }

        recognition = new SpeechRecognition();
        recognition.lang = "vi-VN";
        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.onstart = () => {
            listening = true;
            $("recordButton").textContent = "Dừng ghi âm";
            $("voiceStatus").textContent = "Đang nghe...";
        };

        recognition.onresult = (event) => {
            if (!dialog.open || saving) return;

            const text = event.results[0][0].transcript;

            $("transcriptInput").value = text;
            $("descriptionInput").value = text;

            $("voiceStatus").textContent =
                "Đã nhận giọng nói. Kiểm tra thông tin rồi bấm lưu.";
        };

        recognition.onerror = (event) => {
            if (event.error === "aborted") return;

            $("voiceStatus").textContent =
                event.error === "not-allowed"
                    ? "Chưa được cấp quyền micro. Bạn có thể nhập tay."
                    : "Chưa nhận được giọng nói. Hãy thử lại hoặc nhập tay.";
        };

        recognition.onend = () => {
            listening = false;
            $("recordButton").textContent = "Bắt đầu nói";

            if ($("voiceStatus").textContent === "Đang nghe...") {
                $("voiceStatus").textContent = "Đã dừng. Bạn có thể nói lại.";
            }
        };

        try {
            recognition.start();
        } catch {
            $("voiceStatus").textContent =
                "Không khởi động được micro. Hãy thử lại.";
        }
    });

    $("logoutButton").addEventListener("click", logout);
    $("manualButton").addEventListener("click", () => openExpenseForm(false));
    $("voiceButton").addEventListener("click", () => openExpenseForm(true));

    $("closeDialog").addEventListener("click", closeExpenseForm);
    $("cancelButton").addEventListener("click", closeExpenseForm);

    $("monthSelect").addEventListener("change", () => loadData());
    $("yearInput").addEventListener("change", () => loadData());
    $("reloadButton").addEventListener("click", () => loadData());
    $("searchInput").addEventListener("input", renderList);

    $("categoryFilter").addEventListener("change", () => {
        // Nếu đang tải dữ liệu, renderDashboard sẽ cập nhật bảng sau.
        if (!$("searchInput").disabled) {
            renderList();
        }
    });

    await loadData();
})();