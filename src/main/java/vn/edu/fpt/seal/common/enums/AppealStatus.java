package vn.edu.fpt.seal.common.enums;

/**
 * Trạng thái của một đơn khiếu nại kết quả vòng thi.
 * PENDING  : mới nộp, đang chờ ban tổ chức xử lý.
 * ACCEPTED : khiếu nại được chấp nhận (kết quả có thể được điều chỉnh).
 * REJECTED : khiếu nại bị từ chối, giữ nguyên kết quả.
 */
public enum AppealStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
