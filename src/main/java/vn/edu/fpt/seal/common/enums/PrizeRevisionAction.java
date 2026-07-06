package vn.edu.fpt.seal.common.enums;

/**
 * Hành động chỉnh sửa giải thưởng (không bao giờ xóa cứng lịch sử):
 * REVOKED    : thu hồi giải khỏi đội hiện tại.
 * REASSIGNED : chuyển giải từ đội cũ sang đội mới.
 */
public enum PrizeRevisionAction {
    REVOKED,
    REASSIGNED
}
