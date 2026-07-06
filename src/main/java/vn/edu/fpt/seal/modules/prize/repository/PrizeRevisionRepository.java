package vn.edu.fpt.seal.modules.prize.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.prize.entity.PrizeRevision;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrizeRevisionRepository extends JpaRepository<PrizeRevision, UUID> {

    /** Lịch sử chỉnh sửa của một giải, mới nhất trước. */
    @EntityGraph(attributePaths = {"oldTeam", "newTeam", "changedBy"})
    List<PrizeRevision> findByPrizeIdOrderByChangedAtDesc(UUID prizeId);

    /** Giải đã từng bị chỉnh sửa chưa? — dùng để chặn xóa cứng. */
    boolean existsByPrizeId(UUID prizeId);
}
