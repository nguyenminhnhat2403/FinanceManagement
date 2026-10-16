# Frontend MVP — phương án 2
Ngày: 16/10/2026

Đây là thiết kế đề xuất cho FinanceManagement, không phải các màn hình đã được lập trình. Các ảnh dùng dữ liệu minh họa. Không thay đổi mã Java trong lần thiết kế này.

## 1. Phong cách chung
- Thanh menu ngang xanh navy: Tổng quan / Chi tiêu / Báo cáo.
- Nền xám rất nhạt, nội dung trong thẻ trắng, nút chính xanh mint.
- Số tiền chi hiển thị đỏ. Mức giảm chi so với kỳ trước xanh; mức tăng đỏ; không đổi trung tính. Luôn kèm chữ và mũi tên để không phụ thuộc màu.
- Tiền lưu là số dương ở backend; dấu trừ trong danh sách thể hiện khoản chi ở giao diện.
- Một tài khoản chỉ xem và thao tác dữ liệu của mình. Kiểm tra quyền ở backend cho mọi API; ẩn nút ở frontend không thay thế kiểm tra quyền.
- Avatar hiển thị tên người dùng và Đăng xuất. MVP chưa cần một trang cài đặt riêng.
- Trên điện thoại: menu ba mục ở dưới, các thẻ xếp dọc, bảng đổi thành danh sách, biểu mẫu chiếm chiều rộng màn hình.

## 2. Bản đồ màn hình
Đăng ký → Đăng nhập → Tổng quan.
Từ menu có thể đi tới Chi tiêu hoặc Báo cáo.
Từ Tổng quan hoặc Chi tiêu có thể mở Nhập tay hoặc Nhập giọng nói.
Lưu thành công → cập nhật danh sách, tổng tiền và biểu đồ liên quan.
Đăng xuất → xóa trạng thái đăng nhập phía trình duyệt → về Đăng nhập.

Các đường dẫn dưới đây là đề xuất frontend, không phải route đang có:
| Trang | Đường dẫn đề xuất | Vai trò |
|---|---|---|
| Đăng nhập | /login.html | Vào tài khoản |
| Đăng ký | /register.html | Tạo tài khoản |
| Tổng quan | /dashboard.html | Xem nhanh tháng đã chọn |
| Chi tiêu | /expenses.html | Xem, tìm, thêm và quản lý khoản chi |
| Báo cáo | /reports.html | So sánh tháng và xu hướng năm |

Nhập tay, ghi âm, xác nhận kết quả, sửa/xóa là cửa sổ trong trang; không cần tạo thêm một trang HTML cho mỗi thao tác.

## 3. Đăng nhập và đăng ký
Ảnh: [01-auth.png](01-auth.png)

Đăng nhập gồm email, mật khẩu, nút hiện/ẩn mật khẩu, nút Đăng nhập và liên kết Đăng ký.
Khi gửi: khóa nút tạm thời, hiện “Đang đăng nhập…”. Thành công thì chuyển Tổng quan; sai thông tin hiện lỗi gần biểu mẫu.
Gọi POST /api/auth/login. Backend hiện có trả token cùng thông tin người dùng. Frontend cần quản lý phiên đăng nhập và gửi token trên các request cần xác thực.

Đăng ký gồm họ tên, email, mật khẩu, nút Tạo tài khoản và liên kết Đăng nhập.
Gọi POST /api/auth/register. Thành công thì chuyển Đăng nhập kèm thông báo. Không tự coi đăng ký là đã đăng nhập.
Email trùng và dữ liệu không hợp lệ cần thông báo rõ. Ràng buộc frontend phải khớp backend.

## 4. Tổng quan
Ảnh: phần trên của [02-overview-reports.png](02-overview-reports.png)

Đây là trang đầu tiên sau đăng nhập:
- Bộ chọn tháng/năm.
- Ba thẻ: tổng chi tháng đang chọn, tổng chi tháng trước, chênh lệch.
- Biểu đồ vòng tròn: tỷ trọng chi theo danh mục.
- Một số khoản chi mới nhất; bấm Xem tất cả để sang Chi tiêu.
- Hai nút luôn dễ thấy: Nhập giọng nói và + Nhập tay.

Ví dụ tháng 9/2026 chi 3.880.000đ, tháng 8 chi 4.150.000đ.
Giảm 270.000đ, xấp xỉ 6,5%. Đây là giảm chi, không phải thu nhập.
Khi đổi tháng, cả tổng tiền, biểu đồ và danh sách gần đây cùng đổi theo.

## 5. Báo cáo tháng và năm
Ảnh: phần dưới của [02-overview-reports.png](02-overview-reports.png)

Chế độ Theo tháng: chọn tháng/năm; hai cột đặt cạnh nhau cho tháng được chọn và tháng trước. Kèm chênh lệch tiền và phần trăm.
Có thể hiển thị mức thay đổi từng danh mục khi backend đã cung cấp số tổng hợp.
Chế độ Theo năm: hiển thị 12 cột từ tháng 1 tới tháng 12; tháng không có dữ liệu bằng 0. Bấm một cột để xem tháng đó.

Quy ước:
- Chênh lệch = tháng được chọn - tháng trước.
- Phần trăm = chênh lệch / tháng trước × 100.
- Nếu tháng trước bằng 0, không chia cho 0; hiện “Chưa có dữ liệu để so sánh” hoặc “Tháng trước chưa phát sinh”.
- Tháng 1 phải so với tháng 12 của năm trước.
- Nếu chọn tháng hiện tại chưa kết thúc: ghi rõ “Tính đến hôm nay”; không diễn giải đó là mức tiết kiệm cả tháng.
- Để so sánh công bằng tháng đang diễn ra, có thể bổ sung lựa chọn cùng kỳ: từ đầu tháng đến cùng ngày. Hai kỳ phải được ghi rõ.

Biểu đồ chỉ vẽ dữ liệu; backend chịu trách nhiệm kiểm tra quyền, lọc kỳ và tổng hợp. Không lấy riêng một trang danh sách rồi cộng làm tổng cả tháng.

## 6. Danh sách chi tiêu
Ảnh: phần trái của [03-expenses-manual.png](03-expenses-manual.png)

Bộ lọc tháng/năm, danh mục và ô tìm theo nội dung.
Mỗi dòng: ngày, nội dung, danh mục, số tiền, nút sửa và xóa.
Có tổng chi theo bộ lọc và phân trang nếu nhiều dữ liệu.
Bấm dòng có thể mở chi tiết; MVP có thể dùng ngay biểu mẫu sửa để tránh thêm màn hình không cần thiết.

Sửa: mở biểu mẫu đã điền dữ liệu cũ, thay đổi, bấm Lưu thay đổi; đóng cửa sổ và cập nhật danh sách sau khi server xác nhận.
Xóa: mở hộp “Xóa khoản chi Ăn trưa 50.000đ?” với Hủy / Xóa. Chỉ bỏ dòng sau khi API thành công.
Hai thao tác này là đề xuất, controller hiện chưa có endpoint sửa/xóa.

## 7. Nhập tay
Ảnh: phần phải của [03-expenses-manual.png](03-expenses-manual.png)

Bấm + Nhập tay → cửa sổ Thêm khoản chi.
Nhập số tiền, danh mục, nội dung, ngày và giờ chi.
Ngày mặc định hôm nay nhưng có thể đổi để ghi bổ sung khoản cũ.
Số tiền phải lớn hơn 0; có lỗi thì hiện ngay dưới trường tương ứng.

Bấm Lưu khoản chi → gửi API → hiện trạng thái đang lưu → khi thành công mới đóng cửa sổ và báo “Đã lưu khoản chi”.
Thông báo thành công chỉ hiển thị sau khi API xác nhận lưu; không hiển thị đồng thời với biểu mẫu chưa lưu.
Hủy hoặc đóng X thì không tạo khoản chi.

API tạo hiện có: POST /api/users/{userId}/expenses.
Ngày/giờ gửi theo định dạng mà DTO backend nhận, không gửi nguyên chuỗi ngày đã định dạng cho người đọc.

## 8. Giọng nói
Ảnh: [04-voice-flow.png](04-voice-flow.png)

1. Bấm micro → trình duyệt xin quyền sử dụng micro nếu cần.
2. Hiện “Đang nghe…”, nút Dừng ghi âm, Hủy và nội dung nghe được.
3. Dừng → chuyển giọng nói thành văn bản → phân tích văn bản thành các trường.
4. Hiện Xác nhận khoản chi với câu gốc và các ô có thể sửa.
5. Người dùng kiểm tra số tiền, danh mục, nội dung, ngày/giờ rồi bấm Lưu khoản chi.
6. API lưu thành công → thông báo và cập nhật danh sách/tổng tiền.

Ví dụ: “Ăn trưa hôm nay hết 50 nghìn” → 50.000 VND, Ăn uống, Ăn trưa, ngày hôm nay.
Không nhận được ngày thì dùng ngày mặc định được hiển thị rõ để kiểm tra. Trường chưa xác định được để người dùng bổ sung.
Nói lại chỉ thực hiện ghi âm lại; không tạo thêm khoản chi.
Không nghe rõ → Thử lại. Từ chối quyền hoặc trình duyệt không hỗ trợ → Nhập tay.
Phần nhận giọng nói và phân loại văn bản là hai bước khác nhau. Backend hiện chưa có luồng voice → parse → lưu hoàn chỉnh đang hoạt động.

## 9. Các trạng thái nhỏ cũng phải làm
| Tình huống | Cách hiển thị |
|---|---|
| Đang tải | Thẻ hoặc dòng chờ, không hiển thị 0 như kết quả thật |
| Tháng chưa có khoản chi | “Chưa có chi tiêu trong tháng này”, nút Nhập tay / Giọng nói; không vẽ tỷ lệ giả |
| Bộ lọc không có kết quả | “Không tìm thấy khoản chi”, nút Xóa bộ lọc |
| API lỗi | Giữ dữ liệu nhập, thông báo và nút Thử lại |
| Đang lưu | Khóa nút lưu để tránh bấm liên tục |
| Token hết hạn / 401 | Thông báo đăng nhập lại, chuyển trang đăng nhập |
| Không có quyền / 403 | Thông báo không có quyền thao tác |
| Xóa khoản chi | Xác nhận cụ thể nội dung và số tiền trước khi gửi |
| Đăng xuất | Xóa token/trạng thái phía client; JWT đã phát hành không tự bị thu hồi ở server |

## 10. Đối chiếu backend hiện tại
Kiểm tra controller ngày 16/10/2026:
- Đã có endpoint đăng ký, đăng nhập.
- Đã có endpoint tạo và lấy danh sách chi tiêu.
- Đã có endpoint báo cáo tổng và tổng theo tháng.
- Chưa thấy endpoint riêng cho báo cáo nhóm danh mục, báo cáo 12 tháng, cập nhật/xóa chi tiêu trong controller đang hoạt động.
- Bộ lọc, tìm kiếm, phân trang, dữ liệu ngày trong response và quyền sở hữu của mọi thao tác cần được rà soát khi triển khai.
- Thiết kế ảnh không phải bằng chứng tính năng backend đã hoàn thành.

## 11. Thứ tự triển khai
1. Hoàn thiện kiểm tra quyền dữ liệu theo tài khoản và DTO chi tiêu.
2. Hoàn thiện lọc tháng và API tổng hợp dùng cho báo cáo; kiểm tra khoảng thời gian đầu tháng bao gồm, đầu tháng kế tiếp không bao gồm.
3. Làm frontend Đăng ký → Đăng nhập → danh sách Chi tiêu.
4. Làm cửa sổ Nhập tay và phản hồi thành công/lỗi.
5. Làm Tổng quan và báo cáo; kiểm tra tiền, tỷ lệ, tháng trước bằng 0.
6. Nối giọng nói → phân tích → xác nhận → API lưu.
7. Bổ sung sửa/xóa, tìm kiếm/phân trang và bố cục điện thoại.

## 12. Các ảnh và prompt
Bốn ảnh tạo bằng công cụ imagegen tích hợp. Prompt đầy đủ trong [prompts.md](prompts.md).
Ảnh là tài liệu thiết kế; lúc xây frontend cần dùng HTML/CSS/JavaScript và biểu đồ thật, không dùng ảnh làm giao diện tương tác.

