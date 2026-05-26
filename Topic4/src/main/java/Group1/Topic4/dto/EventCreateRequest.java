package Group1.Topic4.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// @Schema giúp mô tả từng trường dữ liệu để FE biết đường mà gửi
@Schema(description = "Mẫu dữ liệu gửi lên khi tạo Sự kiện mới")
public class EventCreateRequest {

    @Schema(description = "Tên của kỳ Hackathon", example = "SEAL Spring Hackathon 2026")
    private String name;

    @Schema(description = "Mô tả ngắn gọn về sự kiện", example = "Cuộc thi lập trình mùa xuân")
    private String description;

    // Bắt buộc phải có Getter và Setter để Spring Boot đọc được dữ liệu
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}