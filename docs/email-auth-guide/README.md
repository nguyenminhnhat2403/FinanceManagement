# Đăng ký xác minh Gmail và quên mật khẩu
Ngày soạn: 18/10/2026.

Đây là bộ code mẫu để bạn tự copy vào project. Các file trong thư mục này KHÔNG tự chạy và chưa thay đổi code ứng dụng hiện tại. Chưa chạy Maven, chưa gửi email thật, chưa kiểm thử tích hợp Gmail.

## Luồng và phạm vi
Đăng ký: nhập tên/Gmail/mật khẩu → gửi mã → mở Gmail đọc mã → nhập 6 số vào trang → bấm tạo tài khoản.
Server chỉ tạo user sau khi mã hợp lệ. Không yêu cầu mật khẩu Google của người dùng.
Quên mật khẩu: nhập Gmail → gửi mã → nhập mã và mật khẩu mới của app → server kiểm tra → cập nhật mật khẩu bằng BCrypt.
Đây là xác minh email qua Gmail, không phải đăng nhập Google OAuth. Bản này dùng mã số, không dùng đường link kích hoạt.

## 1. Thêm thư viện gửi mail
Trong phần dependencies của pom.xml, thêm:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

Không ghi version riêng; dùng version do Spring Boot parent của project quản lý. Bấm Load Maven Changes trong IntelliJ.

## 2. Cấu hình Gmail gửi thư
Bạn cần một Gmail của BẠN làm địa chỉ gửi mã cho người dùng.
Bật 2-Step Verification, sau đó tạo App Password nếu tài khoản có hỗ trợ.
Không dùng mật khẩu đăng nhập Google thông thường; không gửi App Password vào chat hoặc frontend.
- Hướng dẫn Google: https://support.google.com/mail/answer/185833?hl=en
- Trang tạo App Password: https://myaccount.google.com/apppasswords

Thêm vào application.properties hiện tại (không xóa cấu hình database):

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_APP_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

Trong IntelliJ: Run → Edit Configurations → chọn cấu hình FinanceManagementApplication → Environment variables.
Đặt hai biến, giá trị do bạn tự điền:
- MAIL_USERNAME: Gmail dùng gửi thư.
- MAIL_APP_PASSWORD: App Password của Gmail gửi thư.

Các tài khoản khác nhau có thể không được Google cung cấp App Password (ví dụ một số tài khoản tổ chức/Advanced Protection). Nếu không có tùy chọn này thì cần chọn cách gửi mail khác, không bật “Less secure apps”.

## 3. Tạo package Java mới
Tạo package org.example.financemanagement.auth.email.

Copy TỪNG FILE dưới đây từ thư mục hướng dẫn sang:
src/main/java/org/example/financemanagement/auth/email/

- EmailCode.java
- EmailCodeRepository.java
- InvalidEmailCodeException.java
- EmailCodeService.java
- EmailRequest.java
- ResetPasswordRequest.java
- PasswordResetService.java
- EmailAuthController.java

Các class dùng Lombok, thư viện này đã có trong pom.xml của bạn.
EmailCode là entity lưu phiên xác minh; không phải tài khoản. Có thể có phiên mã cho một email chưa đăng ký.
Nếu bạn đang dùng spring.jpa.hibernate.ddl-auto=update, Hibernate sẽ tạo bảng email_codes khi chạy. Nếu cấu hình schema khác thì cần migration tương ứng.
Không có cột lưu mã thô; codeHash là BCrypt hash.

## 4. Thay RegisterRequest.java
Copy nội dung file RegisterRequest.java trong thư mục hướng dẫn vào file đang có:
src/main/java/org/example/financemanagement/auth/RegisterRequest.java

Request mới có thêm challengeId và code. Không tạo hai class RegisterRequest ở hai package.

## 5. Sửa AuthService để bắt buộc xác minh ở backend
Thêm các import sau:

```java
import org.example.financemanagement.auth.email.EmailCodeService;
import org.example.financemanagement.auth.email.InvalidEmailCodeException;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
```

Thêm field:

```java
private final EmailCodeService emailCodeService;
```

Thêm EmailCodeService emailCodeService vào danh sách tham số constructor hiện tại và gán:

```java
this.emailCodeService = emailCodeService;
```

Ví dụ phần cuối constructor thành:

```java
public AuthService(
        UserService userService,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        EmailCodeService emailCodeService
) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.emailCodeService = emailCodeService;
}
```

Thay DUY NHẤT method register hiện tại bằng:

```java
@Transactional(noRollbackFor = InvalidEmailCodeException.class)
public UserResponse register(RegisterRequest request) {
    if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Mật khẩu vượt quá giới hạn 72 byte."
        );
    }

    String email = emailCodeService.normalizeEmail(request.getEmail());

    emailCodeService.consume(
            request.getChallengeId(),
            email,
            request.getCode(),
            EmailCodeService.REGISTER
    );

    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setName(request.getName().trim());
    createRequest.setEmail(email);
    createRequest.setPassword(request.getPassword());

    return userService.createUser(createRequest);
}
```

Giữ nguyên AuthController.register: nó đã nhận @Valid RegisterRequest và gọi AuthService.

Vì sao dùng noRollbackFor? Mã sai sẽ tăng số lần thử trong entity. Nếu rollback cả lỗi này, bộ đếm cũng bị hoàn tác. Cả consume() và method bao ngoài phải có ngoại lệ không rollback tương ứng như bộ mẫu. Với các lỗi khác, transaction vẫn rollback bình thường.
Consume và tạo tài khoản/đổi mật khẩu cùng transaction; khóa hàng ngăn hai request cùng dùng một mã thành công.

Gmail được lưu dạng chữ thường cho tài khoản mới. Để người dùng mới có thể nhập email viết hoa khi đăng nhập, trong method login khai báo:
```java
String email = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
```
Sau đó dùng biến email thay cho request.getEmail() ở hai chỗ: UsernamePasswordAuthenticationToken và userRepository.findByEmail.
Tài khoản cũ có email lưu viết hoa cần được xử lý dữ liệu riêng trước khi chuẩn hóa toàn hệ thống; không tự đổi dữ liệu cũ trong bước này.

## 6. Sửa SecurityConfig
Trong matcher POST đăng ký/đăng nhập, bổ sung ba API mới:

```java
.requestMatchers(
        HttpMethod.POST,
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/register-code",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
).permitAll()
```

Trong matcher GET phục vụ giao diện, thêm:
```java
"/forgot-password.html",
```

register.html và /assets/** đã được permit trong code hiện tại.

Project còn có POST /api/users tạo user trực tiếp. Để không đi vòng qua xác minh email, thêm matcher này TRƯỚC .anyRequest().authenticated():

```java
.requestMatchers(HttpMethod.POST, "/api/users").denyAll()
```

Giữ nguyên các matcher khác. Không mở permitAll cho API dữ liệu cá nhân.

## 7. Copy giao diện
Copy:
- register.html → src/main/resources/static/register.html
- forgot-password.html → src/main/resources/static/forgot-password.html
- auth-extra.css → src/main/resources/static/assets/css/auth-extra.css
- auth-flow.js → src/main/resources/static/assets/js/auth-flow.js

Hai trang dùng lại login.css đang có để đồng bộ phong cách.
Không dùng login.js cho hai trang mới, vì login.js tìm các ID riêng của form đăng nhập.

## 8. Gắn link ở login.html
Thay dòng “Trang đăng ký sẽ được làm ở bước tiếp theo” bằng:

```html
<p class="signup-note">
  Chưa có tài khoản? <a href="/register.html">Đăng ký ngay</a>
</p>
<p class="signup-note">
  <a href="/forgot-password.html">Quên mật khẩu?</a>
</p>
```

## 9. API thực hiện những gì?
| API POST | Body chính | Kết quả |
|---|---|---|
| /api/auth/register-code | email | Gửi mã đăng ký, trả challengeId |
| /api/auth/register | name,email,password,challengeId,code | Kiểm tra mã rồi tạo user |
| /api/auth/forgot-password | email | Gửi mã reset, trả challengeId |
| /api/auth/reset-password | email,challengeId,code,newPassword | Kiểm tra mã rồi cập nhật mật khẩu |

challengeId chỉ là ID của phiên xác minh, không phải mã OTP. Frontend giữ ID trong bộ nhớ trang, không lưu mật khẩu/mã vào localStorage.
Mã đăng ký và mã reset có purpose khác nhau nên không dùng lẫn nhau.
Reset gửi cùng kiểu phản hồi và email cho mọi Gmail hợp lệ; chỉ tài khoản tồn tại mới đổi được mật khẩu.
Sau khi gửi lại mã, giao diện dùng challengeId của lần gửi mới; nhập mã trong email mới tương ứng.
Đổi Gmail trên form thì phải gửi mã lại.
Tài khoản được tạo từ bước gửi register cuối, không tạo từ bước xin mã.

## 10. Tự kiểm tra
1. Chạy lại Spring Boot sau khi cấu hình đủ MAIL_USERNAME và MAIL_APP_PASSWORD.
2. Mở http://localhost:8080/register.html.
3. Nhập Gmail thật bạn có thể đọc; bấm gửi mã, kiểm tra Inbox và Spam.
4. Nhập mã, tên, mật khẩu app rồi tạo tài khoản.
5. Xem bảng users: chỉ sau bước 4 mới có tài khoản mới.
6. Đăng nhập bằng mật khẩu app vừa đặt, không phải mật khẩu Google.
7. Mở http://localhost:8080/forgot-password.html, gửi mã và đặt mật khẩu mới.
8. Sau reset, đăng nhập mới bằng mật khẩu cũ phải thất bại; mật khẩu mới thành công.
9. Thử mã sai 5 lần, mã đã dùng, mã hết 10 phút, mã đăng ký đem reset: đều không được chấp nhận.
10. Thử POST /api/auth/register không có mã: phải bị từ chối, kể cả gửi trực tiếp bằng HTTP Client.
11. Thử POST /api/users với token hợp lệ: bị từ chối, không tạo user vòng qua xác minh.
12. Khi mất kết nối SMTP, trang báo lỗi; không hiển thị “đã gửi mã” giả.

Không gửi email thật hay tạo tài khoản tự động trong lúc chuẩn bị bộ code này.

## 11. Giới hạn cần biết
- Đây là bước học triển khai local. Giới hạn gửi 3 lần/15 phút là kiểm tra đơn giản theo email và purpose; chưa có bộ giới hạn nguyên tử theo IP/toàn hệ thống để đưa API gửi thư công khai. Các request xin mã đồng thời có thể vượt ngưỡng này.
- Mã hết hạn vẫn còn trong bảng; sau này cần dọn định kỳ.
- User cũ được tạo trước khi thêm cơ chế vẫn tồn tại. Bản này bắt buộc xác minh ở luồng đăng ký mới, không tự đánh dấu toàn bộ user cũ đã xác minh.
- Reset thay đổi mật khẩu và frontend xóa token trong tab đang reset; JWT đã phát hành trước đó vẫn có thể hợp lệ đến lúc hết hạn theo JwtService hiện tại. Thu hồi tất cả JWT cũ cần thêm tokenVersion hoặc cơ chế tương đương.
- Các tác vụ sửa/xóa khoản chi chưa được kiểm thử lại trong bước này.

## Tài liệu đã đối chiếu
- Spring Boot JavaMailSender và mail starter: https://docs.spring.io/spring-boot/reference/io/email.html
- Gmail SMTP: https://support.google.com/a/answer/176600?hl=en
- Google App Password: https://support.google.com/mail/answer/185833?hl=en
- Mã khôi phục hết hạn/dùng một lần/giới hạn thử: https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html

