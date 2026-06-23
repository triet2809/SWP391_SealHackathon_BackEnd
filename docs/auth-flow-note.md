# Ghi chú auth flow

Tài liệu này giải thích luồng auth của backend theo cách dễ hiểu cho người mới học Spring Security và JWT.

## 1. Register FPT hoạt động như thế nào

Endpoint: `POST /auth/register/fpt`

Request body cần:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@fpt.edu.vn",
  "password": "123456",
  "campusId": "SE123456"
}
```

Flow xử lý:

1. Controller nhận request và chạy validation bằng annotation như `@NotBlank`, `@Email`, `@Size`.
2. `AuthServiceImpl.registerFpt()` kiểm tra `campusId` có rỗng hay không.
3. Hệ thống kiểm tra email đã tồn tại trong bảng `users` chưa.
4. Nếu email chưa tồn tại, password sẽ được hash bằng `BCryptPasswordEncoder`.
5. Hệ thống gán `studentType = fpt`, `status = active`.
6. Role mặc định `ROLE_STUDENT` sẽ được gán cho user. Nếu role này chưa có trong DB thì service tự tạo.
7. User được lưu vào DB và trả về thông tin cơ bản của tài khoản vừa tạo.

Ý chính:

- FPT student bắt buộc có `campusId`.
- Password không bao giờ lưu dạng plain text.

## 2. Register External hoạt động như thế nào

Endpoint: `POST /auth/register/external`

Request body cần:

```json
{
  "fullName": "Tran Van B",
  "email": "b@gmail.com",
  "password": "123456"
}
```

Flow gần giống register FPT, chỉ khác ở chỗ:

1. Không yêu cầu `campusId`.
2. `studentType` sẽ là `external`.
3. Các bước còn lại như check email trùng, hash password, gán role mặc định vẫn giống nhau.

## 3. Login hoạt động như thế nào

Endpoint: `POST /auth/login`

Request body:

```json
{
  "email": "a@fpt.edu.vn",
  "password": "123456"
}
```

Flow xử lý:

1. Controller nhận email/password.
2. `AuthServiceImpl.login()` tìm user theo email.
3. Nếu không tìm thấy user hoặc password sai, service trả lỗi `Invalid credentials`.
4. Nếu user không ở trạng thái `active`, service từ chối đăng nhập.
5. Nếu hợp lệ, hệ thống tải `UserDetails` từ `CustomUserDetailsService`.
6. `JwtService` tạo:
   - `accessToken`: token sống ngắn, dùng để gọi API protected
   - `refreshToken`: token sống dài hơn, dùng để xin token mới
7. Response trả về token và thông tin user.

## 4. JWT access token được tạo và dùng như thế nào

Access token được tạo trong `JwtService.generateAccessToken()`.

Access token chứa:

- `subject`: email của user
- `issuedAt`: thời điểm tạo token
- `expiration`: thời điểm hết hạn
- `tokenType = access`: đánh dấu đây là access token

Client sẽ gửi token qua header:

```http
Authorization: Bearer <access_token>
```

Khi request đi vào backend:

1. `JwtAuthenticationFilter` đọc header `Authorization`.
2. Filter kiểm tra token có bị blacklist chưa.
3. Filter giải mã JWT để lấy email.
4. Filter gọi `CustomUserDetailsService` để load user từ DB.
5. Nếu token hợp lệ và là `access token`, filter đưa thông tin user vào `SecurityContext`.
6. Từ lúc đó controller có thể lấy `Authentication` hiện tại.

Nói ngắn gọn:

- Access token là “vé vào cửa” cho các API cần đăng nhập.

## 5. Refresh token hoạt động như thế nào

Endpoint: `POST /auth/refresh-token`

Request body:

```json
{
  "refreshToken": "..."
}
```

Flow xử lý:

1. Service lấy email từ refresh token.
2. Service load `UserDetails` tương ứng với email đó.
3. `JwtService.isRefreshTokenValid()` kiểm tra:
   - token đúng user
   - token chưa hết hạn
   - token có `tokenType = refresh`
4. Nếu hợp lệ, hệ thống tạo lại access token mới.
5. Trong implementation hiện tại, hệ thống cũng trả về refresh token mới để client dùng tiếp.

Ý chính:

- Refresh token không dùng để gọi API business.
- Refresh token chỉ dùng để xin bộ token mới.

## 6. Logout invalidate token như thế nào

Endpoint: `POST /auth/logout`

Header cần:

```http
Authorization: Bearer <access_token>
```

Flow xử lý:

1. Service lấy access token từ header.
2. Service kiểm tra token có đúng định dạng và còn hợp lệ hay không.
3. Nếu hợp lệ, token sẽ được đưa vào `TokenBlacklistService`.
4. `TokenBlacklistService` lưu token vào một `Set` thread-safe trong bộ nhớ.
5. Ở các request sau, `JwtAuthenticationFilter` sẽ chặn token đó ngay từ đầu.

Lưu ý quan trọng:

- Blacklist hiện tại là in-memory.
- Nếu server restart thì danh sách token đã logout sẽ mất.
- Đây là cách làm MVP nhanh và phù hợp cho demo/hackathon.

## 7. GET /auth/me lấy current user như thế nào

Endpoint: `GET /auth/me`

Header cần:

```http
Authorization: Bearer <access_token>
```

Flow:

1. Request đi qua `JwtAuthenticationFilter`.
2. Nếu token hợp lệ, filter gắn `Authentication` vào `SecurityContext`.
3. `AuthController.me()` nhận `Authentication` từ Spring Security.
4. `AuthServiceImpl.getCurrentUser()` lấy `authentication.getName()`, chính là email.
5. Service query DB theo email để lấy thông tin user mới nhất.
6. Response trả về dữ liệu current user.

Điểm hay của cách này:

- Controller không cần tự giải mã JWT.
- Việc xác thực đã được làm ở filter trước đó rồi.

## 8. SecurityConfig đang cho phép/chặn endpoint nào

Public endpoint đang được `permitAll()`:

- `POST /auth/register/fpt`
- `POST /auth/register/external`
- `POST /auth/login`
- `POST /auth/refresh-token`
- Các API CRUD đang public sẵn:
  - `/api/events/**`
  - `/api/rounds/**`
  - `/api/round-tracks/**`
  - `/api/criteria-templates/**`
  - `/api/round-criteria/**`
- Swagger:
  - `/swagger-ui/**`
  - `/swagger-ui.html`
  - `/v3/api-docs/**`

Protected endpoint bắt buộc phải có JWT hợp lệ:

- `GET /auth/me`
- `POST /auth/logout`

Ngoài ra:

- CSRF đang tắt vì backend này dùng REST API.
- Session được cấu hình `STATELESS`, nghĩa là server không giữ session login kiểu truyền thống.
- `JwtAuthenticationFilter` được gắn trước `UsernamePasswordAuthenticationFilter`.

## 9. Cách test full flow bằng Postman

### Bước 1: Register FPT

`POST http://localhost:8080/auth/register/fpt`

Body:

```json
{
  "fullName": "Nguyen Van A",
  "email": "a@fpt.edu.vn",
  "password": "123456",
  "campusId": "SE123456"
}
```

Kỳ vọng:

- Status `201 Created`
- Response trả về user mới tạo

### Bước 2: Login

`POST http://localhost:8080/auth/login`

Body:

```json
{
  "email": "a@fpt.edu.vn",
  "password": "123456"
}
```

Kỳ vọng:

- Status `200 OK`
- Lấy được `accessToken` và `refreshToken`

### Bước 3: Gọi `/auth/me`

`GET http://localhost:8080/auth/me`

Header:

```http
Authorization: Bearer <accessToken>
```

Kỳ vọng:

- Status `200 OK`
- Trả về đúng user vừa login

### Bước 4: Refresh token

`POST http://localhost:8080/auth/refresh-token`

Body:

```json
{
  "refreshToken": "<refreshToken>"
}
```

Kỳ vọng:

- Status `200 OK`
- Trả về access token mới

### Bước 5: Logout

`POST http://localhost:8080/auth/logout`

Header:

```http
Authorization: Bearer <accessToken>
```

Kỳ vọng:

- Status `200 OK`
- Token hiện tại bị đưa vào blacklist

### Bước 6: Thử gọi lại `/auth/me` bằng token cũ

`GET http://localhost:8080/auth/me`

Header:

```http
Authorization: Bearer <accessToken_da_logout>
```

Kỳ vọng:

- Status `401 Unauthorized`

### Bước 7: Dùng access token mới từ refresh flow

Nếu ở bước refresh bạn đã lấy được token mới, hãy dùng token mới đó để gọi lại `/auth/me`.

Kỳ vọng:

- Status `200 OK`

## Tóm tắt nhanh

- Register tạo user mới và hash password.
- Login cấp access token + refresh token.
- Access token dùng để gọi API protected.
- Refresh token dùng để xin token mới.
- Logout blacklist access token hiện tại.
- `/auth/me` lấy current user từ `SecurityContext`.
