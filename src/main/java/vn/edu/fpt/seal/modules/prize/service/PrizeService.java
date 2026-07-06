package vn.edu.fpt.seal.modules.prize.service;
import lombok.RequiredArgsConstructor; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import vn.edu.fpt.seal.common.enums.PrizeRevisionAction; import vn.edu.fpt.seal.common.enums.TimelineEventType; import vn.edu.fpt.seal.common.exception.ApiException; import vn.edu.fpt.seal.modules.event.entity.Event; import vn.edu.fpt.seal.modules.event.repository.EventRepository; import vn.edu.fpt.seal.modules.prize.dto.*; import vn.edu.fpt.seal.modules.prize.entity.Prize; import vn.edu.fpt.seal.modules.prize.entity.PrizeRevision; import vn.edu.fpt.seal.modules.prize.mapper.PrizeMapper; import vn.edu.fpt.seal.modules.prize.repository.PrizeRepository; import vn.edu.fpt.seal.modules.prize.repository.PrizeRevisionRepository; import vn.edu.fpt.seal.modules.team.entity.Team; import vn.edu.fpt.seal.modules.team.repository.TeamRepository; import vn.edu.fpt.seal.modules.teamtimeline.service.TeamTimelineService; import vn.edu.fpt.seal.modules.track.entity.Track; import vn.edu.fpt.seal.modules.track.repository.TrackRepository; import vn.edu.fpt.seal.modules.user.entity.User; import vn.edu.fpt.seal.modules.user.repository.UserRepository; import vn.edu.fpt.seal.security.CurrentUser; import java.util.List; import java.util.UUID;
@Service @RequiredArgsConstructor public class PrizeService{private final PrizeRepository repo; private final EventRepository eventRepo; private final TrackRepository trackRepo; private final TeamRepository teamRepo; private final PrizeRevisionRepository revisionRepo; private final UserRepository userRepo; private final TeamTimelineService timelineService;
 @Transactional(readOnly=true) public Page<PrizeResponse> list(UUID eventId,UUID trackId,UUID teamId,Pageable p){Page<Prize> page=eventId!=null?repo.findByEventId(eventId,p):trackId!=null?repo.findByTrackId(trackId,p):teamId!=null?repo.findByTeamId(teamId,p):repo.findAll(p); return page.map(PrizeMapper::toResponse);} @Transactional(readOnly=true) public PrizeResponse get(UUID id){return PrizeMapper.toResponse(find(id));}
 @Transactional public PrizeResponse create(CreatePrizeRequest r){Event e=eventRepo.findById(r.eventId()).orElseThrow(()->ApiException.notFound("Event not found: "+r.eventId())); Track tr=track(r.trackId()); Team tm=team(r.teamId()); validateScope(e,tr,tm); return PrizeMapper.toResponse(repo.save(Prize.builder().event(e).track(tr).team(tm).name(r.name().trim()).prizeAmount(r.prizeAmount()).description(trim(r.description())).awardedAt(r.awardedAt()).build()));}
 @Transactional public PrizeResponse update(UUID id,UpdatePrizeRequest r){Prize p=find(id); if(r.trackId()!=null)p.setTrack(track(r.trackId())); if(r.teamId()!=null)p.setTeam(team(r.teamId())); validateScope(p.getEvent(),p.getTrack(),p.getTeam()); if(r.name()!=null)p.setName(r.name().trim()); if(r.prizeAmount()!=null)p.setPrizeAmount(r.prizeAmount()); if(r.description()!=null)p.setDescription(trim(r.description())); if(r.awardedAt()!=null)p.setAwardedAt(r.awardedAt()); return PrizeMapper.toResponse(p);}
 // Không cho xóa cứng giải đã có lịch sử chỉnh sửa — bảo toàn dấu vết cho tranh chấp
 @Transactional public void delete(UUID id){Prize p=find(id); if(revisionRepo.existsByPrizeId(id)) throw ApiException.badRequest("This prize has revision history and cannot be deleted; use revoke instead"); repo.delete(p);}

 /**
  * Thu hồi giải khỏi đội hiện tại: prize.team -> null, ghi một dòng lịch sử
  * (REVOKED) và một mốc PRIZE_REVOKED vào timeline của đội bị thu hồi.
  */
 @Transactional public PrizeResponse revoke(UUID id,RevokePrizeRequest r,Authentication auth){
  Prize p=find(id);
  Team oldTeam=p.getTeam();
  if(oldTeam==null) throw ApiException.badRequest("Prize is not currently assigned to any team");
  User actor=currentUser(auth);
  revisionRepo.save(PrizeRevision.builder().prize(p).action(PrizeRevisionAction.REVOKED).oldTeam(oldTeam).newTeam(null).reason(r.reason().trim()).evidenceNote(trim(r.evidenceNote())).changedBy(actor).build());
  p.setTeam(null); // giữ nguyên bản ghi prize, chỉ gỡ liên kết đội — không xóa lịch sử
  timelineService.record(oldTeam,null,TimelineEventType.PRIZE_REVOKED,"Prize revoked","Prize '"+p.getName()+"' was revoked. Reason: "+r.reason().trim());
  return PrizeMapper.toResponse(p);}

 /**
  * Chuyển giải sang đội khác: ghi lịch sử (REASSIGNED, có oldTeam/newTeam)
  * và ghi mốc timeline cho CẢ HAI đội liên quan.
  */
 @Transactional public PrizeResponse reassign(UUID id,ReassignPrizeRequest r,Authentication auth){
  Prize p=find(id);
  Team oldTeam=p.getTeam();
  Team newTeam=teamRepo.findWithTrackById(r.newTeamId()).orElseThrow(()->ApiException.notFound("Team not found: "+r.newTeamId()));
  if(oldTeam!=null&&oldTeam.getId().equals(newTeam.getId())) throw ApiException.badRequest("Prize is already assigned to this team");
  // Đội mới phải thuộc đúng phạm vi (event/track) của giải
  validateScope(p.getEvent(),p.getTrack(),newTeam);
  User actor=currentUser(auth);
  revisionRepo.save(PrizeRevision.builder().prize(p).action(PrizeRevisionAction.REASSIGNED).oldTeam(oldTeam).newTeam(newTeam).reason(r.reason().trim()).evidenceNote(trim(r.evidenceNote())).changedBy(actor).build());
  p.setTeam(newTeam);
  if(oldTeam!=null) timelineService.record(oldTeam,null,TimelineEventType.PRIZE_REASSIGNED,"Prize reassigned to another team","Prize '"+p.getName()+"' was reassigned to team '"+newTeam.getName()+"'. Reason: "+r.reason().trim());
  timelineService.record(newTeam,null,TimelineEventType.PRIZE_REASSIGNED,"Prize received by reassignment","Prize '"+p.getName()+"' was reassigned to this team"+(oldTeam==null?"":" from team '"+oldTeam.getName()+"'")+". Reason: "+r.reason().trim());
  return PrizeMapper.toResponse(p);}

 /** Lịch sử chỉnh sửa của một giải (mới nhất trước). */
 @Transactional(readOnly=true) public List<PrizeRevisionResponse> revisions(UUID prizeId){find(prizeId); return revisionRepo.findByPrizeIdOrderByChangedAtDesc(prizeId).stream().map(PrizeService::toRevisionResponse).toList();}

 private static PrizeRevisionResponse toRevisionResponse(PrizeRevision rv){return PrizeRevisionResponse.builder().id(rv.getId()).prizeId(rv.getPrize().getId()).action(rv.getAction()).oldTeamId(rv.getOldTeam()==null?null:rv.getOldTeam().getId()).oldTeamName(rv.getOldTeam()==null?null:rv.getOldTeam().getName()).newTeamId(rv.getNewTeam()==null?null:rv.getNewTeam().getId()).newTeamName(rv.getNewTeam()==null?null:rv.getNewTeam().getName()).reason(rv.getReason()).evidenceNote(rv.getEvidenceNote()).changedById(rv.getChangedBy().getId()).changedByName(rv.getChangedBy().getFullName()).changedAt(rv.getChangedAt()).build();}
 // Lấy user hiện tại từ SecurityContext — cùng pattern với các service khác
 private User currentUser(Authentication auth){if(auth!=null&&auth.getPrincipal() instanceof CurrentUser c) return userRepo.findById(c.getId()).orElseThrow(()->ApiException.notFound("User not found: "+c.getId())); throw ApiException.forbidden("Authentication required");}
 private Prize find(UUID id){return repo.findWithRelationsById(id).orElseThrow(()->ApiException.notFound("Prize not found: "+id));} private Track track(UUID id){return id==null?null:trackRepo.findById(id).orElseThrow(()->ApiException.notFound("Track not found: "+id));} private Team team(UUID id){return id==null?null:teamRepo.findById(id).orElseThrow(()->ApiException.notFound("Team not found: "+id));}
 private void validateScope(Event e,Track tr,Team tm){if(tr!=null&&!tr.getEvent().getId().equals(e.getId())) throw ApiException.badRequest("Track must belong to prize event"); if(tm!=null){if(tr!=null&&!tm.getTrack().getId().equals(tr.getId())) throw ApiException.badRequest("Team must belong to prize track"); if(!tm.getTrack().getEvent().getId().equals(e.getId())) throw ApiException.badRequest("Team must belong to prize event");}}
 private String trim(String s){return s==null?null:s.trim();}}
