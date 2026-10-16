# Rà soát FinanceManagement

Ngày: 14/10/2026. Đã đọc source Java, DTO, cấu hình, pom, frontend hiện có, seed và test. Không chạy Maven/build/test, không sửa source nghiệp vụ. Đây là rà soát tĩnh; không xác nhận ứng dụng đã chạy thành công.

## Các thay đổi đã hoàn thành so với lần đọc trước

- ExpenseController đã bỏ endpoint gọi getTotal() bị comment: không còn lỗi tham chiếu method đó.
- ExpenseRepository đã comment method Long user(User user).
- ExpenseService đã kiểm tra owner khi tạo, lấy danh sách và tạo bằng giọng nói.
- SecurityConfig đã chặn GET /api/users.
- CreateExpenseRequest có expenseDate và createExpense() đã gán ngày vào entity.
- UserService đã kiểm tra owner khi đọc/cập nhật tài khoản.

## R1 — Báo cáo tháng thiếu kiểm tra quyền [ưu tiên cao]

Vị trí: [ReportService.java:43](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/report/ReportService.java:43).

getMonthlyExpense() chỉ kiểm tra user tồn tại rồi truy vấn theo userId. Một tài khoản có JWT hợp lệ vẫn có thể gọi báo cáo tháng của người khác bằng cách đổi ID trên URL. SecurityConfig authenticated() không thay cho kiểm tra chủ sở hữu.

Sửa: đặt userAccessService.checkOwner(userId) ở đầu method, trước truy vấn báo cáo. getTotalExpense() đã có checkOwner nhưng nên gọi trước existsById; sau kiểm tra owner, query tồn tại có thể bỏ vì tài khoản đã được tìm thấy.

Nghiệm thu: token A đọc monthly của B bị từ chối, không lộ tổng chi của B.

## R2 — Khóa ký JWT nằm trong source [ưu tiên cao trước triển khai]

Vị trí: [JwtService.java:15](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/security/JwtService.java:15).

SECRET_KEY đang là chuỗi trong mã nguồn. Ai có khóa này có thể ký token mà server chấp nhận. Không đưa giá trị khóa vào tài liệu, frontend hoặc log.

Sửa: tạo khóa ngẫu nhiên đủ mạnh, cấp qua biến môi trường/cấu hình ngoài Git. Đổi khóa làm các token ký bằng khóa cũ không còn hợp lệ; chủ động đăng nhập lại. Giá trị mẫu đã xuất hiện trong source nên cần thay trước triển khai.

## R3 — Lỗi JWT/user lookup chưa được xử lý đầy đủ [ưu tiên vừa]

Vị trí: [JwtAuthenticationFilter.java:52](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/security/JwtAuthenticationFilter.java:52), [SecurityConfig.java:26](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/security/SecurityConfig.java:26).

- isTokenValid() parse token, extractEmail() parse lại. Token có thể hết hạn giữa hai lần; lần sau nằm ngoài try/catch.
- UserDetailsService có thể ném UsernameNotFoundException sau khi tài khoản bị xóa; filter chưa xử lý nhánh này.
- Chưa có authenticationEntryPoint và response lỗi API thống nhất. Không thể coi mọi nhánh xác thực hiện tại đều đã trả 401 như thiết kế.
- @Component filter đồng thời được thêm vào SecurityFilterChain; nên tắt auto servlet registration để filter chỉ được quản lý ở chain bảo mật. OncePerRequestFilter không phải lý do bỏ qua cấu hình đăng ký đúng.

Sửa: parse/verify token một lần, lấy subject từ kết quả đã kiểm tra; xử lý exception cụ thể; trả 401 cho xác thực thất bại và 403 cho vượt quyền; clear context khi thất bại. Tắt servlet registration bằng FilterRegistrationBean nếu giữ filter là bean.

## R4 — Khoảng tháng bao gồm đầu tháng kế tiếp [ưu tiên vừa]

Vị trí: [ReportService.java:51](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/report/ReportService.java:51), [ExpenseRepository.java:12](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/expense/ExpenseRepository.java:12).

end là đầu tháng tiếp theo, nhưng Between bao gồm hai mốc. Khoản đúng 00:00 ngày đầu tháng sau có thể bị tính vào tháng trước. month ngoài 1..12 có thể tạo exception chưa được map thành lỗi đầu vào rõ ràng.

Sửa: expenseDate >= start AND expenseDate < end; kiểm tra year/month trong phạm vi được hỗ trợ; tháng không có dữ liệu trả 0.

## R5 — Ngày chi đã lưu nhưng response chưa trả ngày [ưu tiên vừa cho dashboard]

Vị trí: [ExpenseResponse.java:7](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/expense/dto/ExpenseResponse.java:7), [ExpenseDTOMapper.java:11](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/expense/dto/ExpenseDTOMapper.java:11), [UpdateExpenseRequest.java:9](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/expense/dto/UpdateExpenseRequest.java:9).

Create đã nhận và lưu expenseDate. Response chỉ có id/category/amount/description; frontend không biết ngày thật để hiển thị. Update DTO chưa có expenseDate. Danh sách chưa lọc tháng, phân trang hoặc sắp xếp.

Sửa: thêm ngày vào response/mapper/update DTO; thống nhất contract trong MVP; thêm lọc theo tháng với sort ổn định và phân trang. Không sửa Expense.prePersist() để ghi đè ngày người dùng gửi.

## R6 — Chưa có dữ liệu cho hai biểu đồ [tính năng MVP]

Vị trí: [ReportController.java:17](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/report/ReportController.java:17), [ReportService.java:30](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/report/ReportService.java:30).

Hiện chỉ có tổng toàn bộ và tổng một tháng, Java tải các Expense rồi cộng. Chưa có tổng theo category hoặc danh sách 12 tháng. Đây không phải lỗi N+1 trong vòng lặp hiện tại: mapper không tải user lazy. Vấn đề là tải nhiều entity không cần thiết cho phép tính tổng.

Sửa: SUM/GROUP BY ở database, DTO/projection cho categories/yearly; trả 12 tháng và điền 0 cho tháng rỗng. Định nghĩa endpoint trong MVP.md.

## R7 — Thiếu thao tác sửa/xóa và kiểm thử nghiệp vụ [tính năng MVP]

Vị trí: [ExpenseController.java:21](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/expense/ExpenseController.java:21), [FinanceManagementApplicationTests.java:6](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/test/java/org/example/financemanagement/FinanceManagementApplicationTests.java:6).

Có Create/Get, chưa có PUT/DELETE. UserService.updateUser() cũng chưa có endpoint gọi. Test chỉ contextLoads().

Sửa: triển khai sửa/xóa khoản chi, kiểm tra khoản đó thuộc user đang đăng nhập; dùng transaction cho thao tác nhiều bước. Thêm test quyền A/B, update/delete, khoảng ngày, dữ liệu rỗng và aggregate. Các test này có giá trị nghiệp vụ, không chỉ kiểm tra getter/setter.

## R8 — Seed không phù hợp với PostgreSQL và mô hình tài khoản [ưu tiên vừa nếu dùng seed]

Vị trí: [seed.sql:12](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/resources/database/seed.sql:12).

- hashed_password_123 không phải BCrypt hash để login qua encoder hiện tại.
- SALARY không nằm trong CategoryType.
- RAND() là cú pháp không phù hợp PostgreSQL.
- Giả định user_id từ 1 đến 200 không bảo đảm đúng các user vừa insert.
- Tất cả expenseDate là NOW(), không tạo dữ liệu nhiều tháng để kiểm thử biểu đồ.

Sửa: bộ dữ liệu dev có user BCrypt hợp lệ, enum hợp lệ, ID được lấy thực tế, nhiều tháng và điểm giao tháng. Không chạy seed này vào dữ liệu thật.

## R9 — Chuẩn hóa lỗi và dọn mã [sau các lỗi quyền, trước frontend]

Vị trí: [AuthService.java:49](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/auth/AuthService.java:49), [UserService.java:34](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/user/UserService.java:34), [GlobalExceptionHandler.java:8](C:/Users/84966/E-commerce-java-springboot/FinanceManagement/src/main/java/org/example/financemanagement/common/exception/GlobalExceptionHandler.java:8).

- Dependency PasswordEncoder và biến Authentication trong AuthService không dùng tiếp. Lời gọi authenticate() vẫn phải giữ.
- findByEmail được gọi trong filter và trong checkOwner: là truy vấn lặp cố định, không phải N+1. Có thể đưa ID vào custom UserDetails sau, không cần tối ưu sớm bằng cách bỏ kiểm tra quyền.
- existsByEmail trước insert không xử lý hết trường hợp đăng ký đồng thời; unique constraint là lớp cuối, cần map đúng vi phạm email thành 409.
- GET /api/users đã bị chặn, nhưng POST /api/users còn trùng chức năng register và dành cho mọi tài khoản đã xác thực; nên quyết định bỏ hoặc giới hạn endpoint dư này.
- Bỏ import/dependency/method comment không còn dùng khi hoàn thiện từng phần. AI/voice bị comment không phải tính năng đã hoạt động.
- application.properties đang bị Git ignore; cần template cấu hình không chứa secret để người khác cài được project. .env không tự được Spring Boot nạp chỉ vì file tồn tại.
- Schema hiện dựa ddl-auto=update; cần migration trước khi triển khai có dữ liệu lâu dài.

## Bước tiếp theo cụ thể

1. Sửa R1 và R2/R3: quyền và xác thực.
2. Hoàn thiện R5: DTO ngày, lọc, phân trang, sắp xếp.
3. Thêm sửa/xóa, kiểm tra expenseId thuộc đúng user.
4. Sửa R4 và làm R6: aggregate theo danh mục, theo tháng.
5. Chuẩn hóa lỗi, tạo dữ liệu test nhiều tháng, kiểm thử backend.
6. Làm frontend và nối Chart.js theo MVP.md.
