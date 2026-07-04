package vn.edu.fpt.seal.modules.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.EventStatus;
import vn.edu.fpt.seal.common.enums.RoundParticipantStatus;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.event.dto.CreateEventRequest;
import vn.edu.fpt.seal.modules.event.dto.EventResponse;
import vn.edu.fpt.seal.modules.event.dto.SetupCompetitionRequest;
import vn.edu.fpt.seal.modules.event.dto.SetupCompetitionResponse;
import vn.edu.fpt.seal.modules.event.dto.UpdateEventRequest;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.event.mapper.EventMapper;
import vn.edu.fpt.seal.modules.event.repository.EventRepository;
import vn.edu.fpt.seal.modules.participant.entity.RoundParticipant;
import vn.edu.fpt.seal.modules.participant.repository.RoundParticipantRepository;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.round.repository.RoundRepository;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.team.repository.TeamRepository;
import vn.edu.fpt.seal.modules.track.entity.Track;
import vn.edu.fpt.seal.modules.track.repository.TrackRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TrackRepository trackRepository;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final RoundParticipantRepository roundParticipantRepository;

    /** Default track name auto-created while registration is open. */
    private static final String GENERAL_TRACK = "General";

    // Status transition rules:
    //   draft     -> published, cancelled
    //   published -> ongoing, cancelled
    //   ongoing   -> completed, cancelled
    //   completed -> (terminal)
    //   cancelled -> (terminal)
    private static final Map<EventStatus, Set<EventStatus>> ALLOWED_TRANSITIONS = Map.of(
            EventStatus.draft, EnumSet.of(EventStatus.published, EventStatus.cancelled),
            EventStatus.published, EnumSet.of(EventStatus.ongoing, EventStatus.cancelled),
            EventStatus.ongoing, EnumSet.of(EventStatus.completed, EventStatus.cancelled),
            EventStatus.completed, EnumSet.noneOf(EventStatus.class),
            EventStatus.cancelled, EnumSet.noneOf(EventStatus.class)
    );

    @Transactional(readOnly = true)
    public Page<EventResponse> list(EventStatus status, Pageable pageable) {
        Page<Event> page = (status == null)
                ? eventRepository.findAll(pageable)
                : eventRepository.findByStatus(status, pageable);
        return page.map(this::toResponseWithCounts);
    }

    @Transactional(readOnly = true)
    public EventResponse get(UUID id) {
        return toResponseWithCounts(findOrThrow(id));
    }

    private EventResponse toResponseWithCounts(Event e) {
        UUID eventId = e.getId();
        long tracks = trackRepository.countByEventId(eventId);
        long rounds = roundRepository.countByTrackEventId(eventId);
        long participants = teamRepository.countByTrackEventId(eventId);
        return EventMapper.toResponse(e, (int) tracks, (int) rounds, participants);
    }

    @Transactional
    public EventResponse create(CreateEventRequest req) {
        String title = req.title().trim();
        if (eventRepository.existsByTitleIgnoreCase(title)) {
            throw ApiException.conflict("Event title already exists");
        }
        Event e = Event.builder()
                .title(title)
                .description(req.description())
                .status(EventStatus.draft)
                .term(req.term())
                .prizePool(req.prizePool())
                .registrationStart(req.registrationStart())
                .registrationEnd(req.registrationEnd())
                .eventStart(req.eventStart())
                .eventEnd(req.eventEnd())
                .build();
        e = eventRepository.save(e);
        log.info("Event created: id={}, title={}", e.getId(), e.getTitle());
        return toResponseWithCounts(e);
    }

    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest req) {
        Event e = findOrThrow(id);
        if (e.getStatus() == EventStatus.completed || e.getStatus() == EventStatus.cancelled) {
            throw ApiException.badRequest("Cannot edit event in status " + e.getStatus());
        }
        if (req.title() != null) {
            String title = req.title().trim();
            if (!title.equalsIgnoreCase(e.getTitle())
                    && eventRepository.existsByTitleIgnoreCase(title)) {
                throw ApiException.conflict("Event title already exists");
            }
            e.setTitle(title);
        }
        if (req.description() != null) {
            e.setDescription(req.description());
        }
        if (req.term() != null) {
            e.setTerm(req.term());
        }
        if (req.prizePool() != null) {
            e.setPrizePool(req.prizePool());
        }
        if (req.registrationStart() != null) {
            e.setRegistrationStart(req.registrationStart());
        }
        if (req.registrationEnd() != null) {
            e.setRegistrationEnd(req.registrationEnd());
        }
        if (req.eventStart() != null) {
            e.setEventStart(req.eventStart());
        }
        if (req.eventEnd() != null) {
            e.setEventEnd(req.eventEnd());
        }
        return toResponseWithCounts(e);
    }

    @Transactional
    public EventResponse changeStatus(UUID id, EventStatus target) {
        Event e = findOrThrow(id);
        EventStatus current = e.getStatus();
        if (current == target) {
            return EventMapper.toResponse(e);
        }
        Set<EventStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(EventStatus.class));
        if (!allowed.contains(target)) {
            throw ApiException.badRequest(
                    "Invalid status transition: " + current + " -> " + target);
        }
        e.setStatus(target);
        log.info("Event {} status: {} -> {}", e.getId(), current, target);
        return EventMapper.toResponse(e);
    }

    @Transactional
    public void delete(UUID id) {
        Event e = findOrThrow(id);
        if (e.getStatus() != EventStatus.draft) {
            throw ApiException.badRequest("Only draft events can be deleted; cancel instead.");
        }
        eventRepository.delete(e);
        log.info("Event deleted: id={}", id);
    }

    /**
     * Open registration: draft -> published. Ensures a "General" track exists so
     * teams can register into the event before the organiser has designed the
     * thematic tracks.
     */
    @Transactional
    public EventResponse openRegistration(UUID id) {
        Event e = findOrThrow(id);
        if (e.getStatus() != EventStatus.draft) {
            throw ApiException.badRequest("Registration can only be opened from draft (current: " + e.getStatus() + ")");
        }
        e.setStatus(EventStatus.published);
        ensureGeneralTrack(e);
        log.info("Event {} registration opened (draft -> published)", e.getId());
        return toResponseWithCounts(e);
    }

    /**
     * Close registration: published -> ongoing. After this, teams can no longer
     * join (enforced in TeamService), and the organiser can build the competition.
     */
    @Transactional
    public EventResponse closeRegistration(UUID id) {
        Event e = findOrThrow(id);
        if (e.getStatus() != EventStatus.published) {
            throw ApiException.badRequest("Registration can only be closed when published (current: " + e.getStatus() + ")");
        }
        e.setStatus(EventStatus.ongoing);
        long teams = teamRepository.countByTrackEventId(e.getId());
        log.info("Event {} registration closed (published -> ongoing); {} teams registered", e.getId(), teams);
        return toResponseWithCounts(e);
    }

    /**
     * One-shot competition builder, run after registration closes (status=ongoing).
     * Creates thematic tracks, distributes registered teams round-robin, then for
     * each track generates R elimination rounds with an auto-computed topN funnel
     * and seeds round 1 with every team in that track.
     */
    @Transactional
    public SetupCompetitionResponse setupCompetition(UUID id, SetupCompetitionRequest req) {
        Event e = findOrThrow(id);
        if (e.getStatus() != EventStatus.ongoing) {
            throw ApiException.badRequest("Competition can only be set up after registration is closed (status must be ongoing, current: " + e.getStatus() + ")");
        }
        if (roundRepository.countByTrackEventId(e.getId()) > 0) {
            throw ApiException.conflict("Competition already set up for this event");
        }

        List<Team> teams = teamRepository.findByTrackEventId(e.getId());
        if (teams.isEmpty()) {
            throw ApiException.badRequest("No teams registered; cannot build the competition");
        }

        // 1) Resolve target tracks.
        List<Track> tracks = new ArrayList<>();
        boolean customTracks = req != null && req.tracks() != null && !req.tracks().isEmpty();
        if (customTracks) {
            Set<String> seen = new java.util.HashSet<>();
            for (SetupCompetitionRequest.TrackSpec spec : req.tracks()) {
                String name = spec.name().trim();
                if (!seen.add(name.toLowerCase())) {
                    throw ApiException.badRequest("Duplicate track name in request: " + name);
                }
                Track existing = trackRepository.findByEventId(e.getId(), org.springframework.data.domain.Pageable.unpaged())
                        .stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
                if (existing != null) {
                    tracks.add(existing);
                } else {
                    tracks.add(trackRepository.save(Track.builder()
                            .event(e).name(name).description(spec.description()).build()));
                }
            }
        } else {
            tracks.add(ensureGeneralTrack(e));
        }

        // 2) Distribute teams round-robin across the target tracks.
        for (int i = 0; i < teams.size(); i++) {
            teams.get(i).setTrack(tracks.get(i % tracks.size()));
        }
        teamRepository.saveAll(teams);

        // 3) Per track: generate rounds + seed round 1.
        List<SetupCompetitionResponse.TrackPlan> trackPlans = new ArrayList<>();
        int roundsPerTrackReported = 0;
        for (Track track : tracks) {
            List<Team> trackTeams = teams.stream().filter(t -> t.getTrack().getId().equals(track.getId())).toList();
            int n = trackTeams.size();
            if (n == 0) {
                trackPlans.add(SetupCompetitionResponse.TrackPlan.builder()
                        .trackId(track.getId()).name(track.getName()).teamCount(0).rounds(List.of()).build());
                continue;
            }
            int f = resolveFinalists(req, n);
            int r = resolveRounds(req, n, f);
            roundsPerTrackReported = Math.max(roundsPerTrackReported, r);
            int[] funnel = computeFunnel(n, f, r);
            LocalDateTime[] deadlines = spreadDeadlines(e, r);

            List<SetupCompetitionResponse.RoundPlan> roundPlans = new ArrayList<>();
            for (int seq = 1; seq <= r; seq++) {
                Round round = roundRepository.save(Round.builder()
                        .track(track)
                        .name(roundName(seq, r))
                        .sequenceNumber(seq)
                        .submissionDeadline(deadlines[seq - 1])
                        .topNToPromote(funnel[seq - 1])
                        .build());
                int seeded = 0;
                if (seq == 1) {
                    for (Team t : trackTeams) {
                        roundParticipantRepository.save(RoundParticipant.builder()
                                .round(round).team(t).status(RoundParticipantStatus.active).build());
                        seeded++;
                    }
                }
                roundPlans.add(SetupCompetitionResponse.RoundPlan.builder()
                        .roundId(round.getId()).name(round.getName()).sequenceNumber(seq)
                        .topNToPromote(funnel[seq - 1]).seededParticipants(seeded).build());
            }
            trackPlans.add(SetupCompetitionResponse.TrackPlan.builder()
                    .trackId(track.getId()).name(track.getName()).teamCount(n).rounds(roundPlans).build());
        }

        // 4) Drop an auto-created empty "General" track if the organiser used custom tracks.
        if (customTracks) {
            trackRepository.findByEventId(e.getId(), org.springframework.data.domain.Pageable.unpaged()).stream()
                    .filter(t -> t.getName().equalsIgnoreCase(GENERAL_TRACK))
                    .filter(t -> teams.stream().noneMatch(tm -> tm.getTrack().getId().equals(t.getId())))
                    .forEach(trackRepository::delete);
        }

        log.info("Event {} competition set up: {} teams, {} tracks", e.getId(), teams.size(), tracks.size());
        return SetupCompetitionResponse.builder()
                .eventId(e.getId())
                .totalTeams(teams.size())
                .trackCount(tracks.size())
                .roundsPerTrack(roundsPerTrackReported)
                .tracks(trackPlans)
                .build();
    }

    private Track ensureGeneralTrack(Event e) {
        return trackRepository.findByEventId(e.getId(), org.springframework.data.domain.Pageable.unpaged()).stream()
                .filter(t -> t.getName().equalsIgnoreCase(GENERAL_TRACK))
                .findFirst()
                .orElseGet(() -> trackRepository.save(Track.builder()
                        .event(e).name(GENERAL_TRACK).description("Default track for registered teams").build()));
    }

    /** Finalists per track: explicit value (capped to N), else max(3, ceil(0.1*N)) capped to N. */
    private int resolveFinalists(SetupCompetitionRequest req, int n) {
        int f = (req != null && req.finalistCount() != null)
                ? req.finalistCount()
                : Math.max(3, (int) Math.ceil(0.10 * n));
        return Math.min(Math.max(1, f), n);
    }

    /** Rounds per track: explicit value, else suggested by team count; clamped so funnel is valid. */
    private int resolveRounds(SetupCompetitionRequest req, int n, int f) {
        int r = (req != null && req.roundCount() != null) ? req.roundCount() : suggestRounds(n);
        // Can't eliminate anyone if N == F -> a single round. Otherwise at most (N - F) rounds make sense.
        int maxUseful = Math.max(1, n - f + 1);
        return Math.max(1, Math.min(r, maxUseful));
    }

    private int suggestRounds(int n) {
        if (n <= 8) return 1;
        if (n <= 30) return 2;
        if (n <= 80) return 3;
        return 4;
    }

    /**
     * Geometric elimination funnel: N -> ... -> F over R rounds. Returns topN per
     * round (length R), strictly decreasing when possible, last element == F.
     */
    private int[] computeFunnel(int n, int f, int r) {
        int[] out = new int[r];
        if (r == 1) {
            out[0] = f;
            return out;
        }
        double ratio = Math.pow((double) f / n, 1.0 / r);
        int prev = n;
        for (int i = 1; i <= r; i++) {
            int val = (i == r) ? f : (int) Math.round(n * Math.pow(ratio, i));
            if (val >= prev) val = prev - 1;      // enforce strictly decreasing
            if (val < f) val = f;                  // never drop below finalist target early
            out[i - 1] = Math.max(1, val);
            prev = out[i - 1];
        }
        out[r - 1] = f;
        return out;
    }

    /** Spread R submission deadlines across the event window (or from now if unset). */
    private LocalDateTime[] spreadDeadlines(Event e, int r) {
        LocalDateTime base = e.getEventStart() != null ? e.getEventStart() : LocalDateTime.now();
        LocalDateTime end = e.getEventEnd() != null && e.getEventEnd().isAfter(base)
                ? e.getEventEnd() : base.plusDays(Math.max(1, r) * 7L);
        long totalMinutes = ChronoUnit.MINUTES.between(base, end);
        long step = Math.max(1, totalMinutes / r);
        LocalDateTime[] out = new LocalDateTime[r];
        for (int i = 1; i <= r; i++) {
            out[i - 1] = base.plusMinutes(step * i);
        }
        return out;
    }

    private String roundName(int seq, int total) {
        if (total == 1) return "Final";
        if (seq == total) return "Final";
        if (seq == total - 1) return "Semifinal";
        return "Round " + seq;
    }

    private Event findOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Event not found: " + id));
    }
}
