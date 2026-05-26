package Group1.Topic4.controller;

import Group1.Topic4.dto.EventCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
// 1. Nhãn @Tag: Dùng để gom nhóm các API. Trên giao diện Swagger sẽ hiện một thư mục tên "Quản lý Sự kiện"
@Tag(name = "Quản lý Sự kiện", description = "Các API dùng để tạo và quản lý các kỳ Hackathon (Spring, Summer, Fall)")
public class HackathonEventController {

    // 2. Nhãn @Operation: Mô tả chi tiết chức năng của API này
    @Operation(
            summary = "Tạo sự kiện Hackathon mới",
            description = "API này dành cho Event Coordinator (BTC) để tạo một kỳ thi mới."
    )
    // 3. Nhãn @ApiResponses: Khai báo trước các trường hợp trả về cho Frontend biết
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo sự kiện thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu gửi lên không hợp lệ (ví dụ: thiếu tên sự kiện)")
    })
    @PostMapping
    public ResponseEntity<String> createEvent(@RequestBody EventCreateRequest request) {

        // Hiện tại cứ để logic trống, chỉ trả về thành công giả lập
        return ResponseEntity.status(201).body("Đã tạo sự kiện: " + request.getName());
    }
}