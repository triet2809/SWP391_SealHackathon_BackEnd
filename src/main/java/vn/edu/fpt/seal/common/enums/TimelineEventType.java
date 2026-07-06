package vn.edu.fpt.seal.common.enums;

/**
 * Loại sự kiện trên dòng thời gian (timeline) của một đội thi.
 * Lưu dưới dạng VARCHAR trong DB (không dùng pg enum) để dễ mở rộng về sau.
 */
public enum TimelineEventType {
    TEAM_CREATED,          // Đội được tạo mới
    TEAM_DISQUALIFIED,     // Đội bị loại (vi phạm quy chế)
    TEAM_REACTIVATED,      // Đội được khôi phục sau khi bị loại
    RANKING_RECALCULATED,  // Xếp hạng vòng thi được tính lại
    TEAM_PROMOTED,         // Đội được thăng hạng vào vòng tiếp theo
    TEAM_ELIMINATED,       // Đội bị loại theo kết quả xếp hạng
    APPEAL_SUBMITTED,      // Đội nộp đơn khiếu nại kết quả
    APPEAL_RESOLVED,       // Khiếu nại đã được ban tổ chức xử lý
    TIE_BREAK_DECISION,    // Có quyết định phân định hòa thủ công (review mã nguồn GitHub)
    PRIZE_REVOKED,         // Giải thưởng của đội bị thu hồi
    PRIZE_REASSIGNED       // Giải thưởng được chuyển sang đội khác
}
