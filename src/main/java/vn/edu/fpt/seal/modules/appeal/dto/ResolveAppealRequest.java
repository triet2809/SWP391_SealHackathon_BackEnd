package vn.edu.fpt.seal.modules.appeal.dto;

import jakarta.validation.constraints.NotNull;
import vn.edu.fpt.seal.common.enums.AppealStatus;

/**
 * Yêu cầu xử lý khiếu nại của điều phối viên.
 * status chỉ nhận ACCEPTED hoặc REJECTED (service sẽ validate).
 */
public record ResolveAppealRequest(
        @NotNull AppealStatus status,
        String response
) {}
