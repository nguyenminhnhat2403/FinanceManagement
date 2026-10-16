# FinanceManagement — login and monthly comparison image prompts

Created: 2026-10-15. Tool: built-in image_gen (imagegen). Style reference / edit target: frontend-mvp-voice.png. Images are design mockups with illustrative data; no application code was changed.

## login

Use case: ui-mockup. Input image 1 is a STYLE REFERENCE for the user's existing FinanceManagement expense-tracking web app, not a screenshot to reproduce.
Create ONE separate high-fidelity desktop LOGIN SCREEN image for this exact product, visually belonging to the same app as the reference. Straight-on flat web UI screenshot, landscape 16:10, crisp legible Vietnamese accents, no photographed device.
Preserve the same FM symbol, product name "FinanceManagement", deep forest green, emerald primary actions, near-white surfaces, quiet light-gray background, clean sans-serif font and softly rounded fields. Modern practical banking-inspired technology aesthetic, polished but simple.
Layout: clean spacious two-column page. Left forest-green brand panel about 42% width, FM symbol and "FinanceManagement", prominent heading "Quản lý chi tiêu cá nhân", short text "Theo dõi từng khoản chi. Hiểu rõ mỗi tháng." with a restrained abstract chart motif. No account balances, transfers, income, real-bank logos, stock imagery or invented features.
Right column white/light neutral, center an attractive but compact login form. Exact copy:
"Chào mừng trở lại"
"Đăng nhập để xem chi tiêu của bạn."
"Email" with input placeholder "email@example.com"
"Mật khẩu" with password input shown as dots and a small visibility eye icon
full-width emerald button "Đăng nhập"
footer "Chưa có tài khoản? Đăng ký"
Small subtle label "FinanceManagement · MVP".
Do not add Google/Facebook login, forgot-password flows, OTP, financial account numbers, extra navigation, a loaded dashboard or an actual password. No watermark, no unnecessary explanatory annotations. Emphasize readable typography, aligned controls, whitespace, realistic implementable interface.

## monthly-comparison

Use case: ui-mockup. Input image 1 is the EDIT TARGET. Produce ONE UPDATED VERSION of that same FinanceManagement desktop dashboard.
Preserve its branding, overall composition, forest-green sidebar, white panels, emerald primary buttons, red outgoing money amounts, typography, account profile, both charts, visible expense table, and right-hand voice-confirmation drawer. Preserve both "Nhập giọng nói" microphone button and "+ Nhập tay" action. Keep this as a cohesive, clean, straight-on high-fidelity web app screenshot, landscape with enough room for all controls and form labels, not a collage, no devices or watermark.
Requested update: make monthly expense viewing and comparison to previous month explicit. Use completed month September 2026 so the comparison is unambiguously between two full months. Header "Chi tiêu hằng tháng". Show a month dropdown "Tháng 9" and a separate year dropdown "2026", plus "Dữ liệu minh họa".
Replace the two old summary panels with THREE evenly sized clear summary panels:
1. Label "Tháng 9/2026", prominent outgoing amount "-3.880.000 đ" in RED, caption "Tổng chi tháng đang xem".
2. Label "Tháng 8/2026", amount "-4.150.000 đ" in muted RED, caption "Tổng chi tháng trước".
3. Label "So với tháng trước", prominent GREEN "Giảm 270.000 đ", smaller GREEN "↓ 6,5%", caption "Chi tiêu ít hơn tháng trước".
Math must be correct: 4,150,000 - 3,880,000 = 270,000, relative decrease approximately 6.5%.
Keep the doughnut chart and category labels "Ăn uống", "Đi lại", "Mua sắm", "Khác". Its title "Danh mục tháng 9". Preserve plausible 46%,24%,18%,12% proportions.
Bar chart titled "So sánh chi tiêu các tháng", axis "Triệu đồng", T1 to T12, zero baseline. Values 3.45,2.98,3.12,4.36,3.78,3.92,3.46,4.15,3.88,3.25,0,0. Highlight T9 emerald and make T8 identifiable as the comparison month, all peers restrained. Month total in the selected bar must be 3.88; preceding month 4.15. Keep T11/T12 zero. Small legend for selected month and previous month if needed.
Expense table title "Chi tiêu tháng 9". Visible table columns Ngày, Mô tả, Danh mục, Số tiền. Rows: "14/09/2026 | Ăn trưa | Ăn uống | -50.000 đ", "13/09/2026 | Đổ xăng | Đi lại | -100.000 đ", "12/09/2026 | Mua sách | Mua sắm | -120.000 đ". Money RED.
At bottom of sidebar add a modest clearly labeled "Đăng xuất" action so the dashboard connects to the new login screen; preserve the profile styling.
Preserve the voice drawer as the post-recording state: "Xác nhận khoản chi", "Đã nhận giọng nói", transcript "Ăn trưa 50 nghìn", editable amount "50.000" VND, category "Ăn uống", description "Ăn trưa". Set date "15/09/2026" for a retrospective expense in the viewed month. Primary "Lưu khoản chi", secondary "Hủy", note "Chưa lưu vào danh sách". No save-success claim. Do not add income, transfer, bank balance, budget, stock or investment features.
All Vietnamese labels must be legible with correct diacritics. Preserve bank-inspired GREEN/RED money semantics and practical MVP simplicity.
