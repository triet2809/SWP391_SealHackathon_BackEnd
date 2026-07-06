package vn.edu.fpt.seal.common.enums;

/**
 * Mức hiển thị của một điều luật (rule) trong sự kiện:
 * PUBLIC       : thí sinh nhìn thấy và phải chấp nhận khi đăng ký.
 * INTERNAL     : chỉ ban tổ chức / điều phối viên xem được.
 * DISPUTE_ONLY : chỉ dùng nội bộ khi xử lý tranh chấp/khiếu nại, không công khai.
 */
public enum RuleVisibility {
    PUBLIC,
    INTERNAL,
    DISPUTE_ONLY
}
