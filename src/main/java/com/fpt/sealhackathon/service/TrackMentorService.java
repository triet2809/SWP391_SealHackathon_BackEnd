package com.fpt.sealhackathon.service;

import com.fpt.sealhackathon.dto.MentorAssignRequest;
import com.fpt.sealhackathon.dto.MentorTeamResponse;
import com.fpt.sealhackathon.entity.*;
import com.fpt.sealhackathon.repository.RoundParticipantRepository;
import com.fpt.sealhackathon.repository.RoundTrackRepository;
import com.fpt.sealhackathon.repository.TeamRepository;
import com.fpt.sealhackathon.repository.TrackMentorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrackMentorService {
    @Autowired
    private TrackMentorRepository trackMentorRepository;
    @Autowired
    private RoundTrackRepository roundTrackRepository;
    @Autowired
    private RoundParticipantRepository roundParticipantRepository;
    @Autowired
    private TeamRepository teamRepository;

    public List<TrackMentor> getMentorsByRoundTrack(UUID roundTrackId) {
        return trackMentorRepository.findByRoundTrackId(roundTrackId);
    }
    public TrackMentor assignMentor(UUID roundTrackId, MentorAssignRequest request) {

        if (trackMentorRepository.existsByRoundTrackIdAndUserId(
                roundTrackId, request.getUserId())) {
            return null;
        }

        RoundTrack roundTrack = roundTrackRepository.findById(roundTrackId)
                .orElseThrow(() -> new RuntimeException("Round track not found"));

        TrackMentor trackMentor = new TrackMentor();

        trackMentor.setId(UUID.randomUUID());

        trackMentor.setEventId(roundTrack.getEventId());

        trackMentor.setRoundTrackId(roundTrackId);

        trackMentor.setUserId(request.getUserId());

        trackMentor.setAssignedAt(LocalDateTime.now());

        return trackMentorRepository.save(trackMentor);
    }
    public boolean removeMentor(UUID roundMentorId) {

        TrackMentor trackMentor = trackMentorRepository.findById(roundMentorId).orElse(null);

        if (trackMentor == null) {
            return false;
        }

        trackMentorRepository.delete(trackMentor);

        return true;
    }
    public List<MentorTeamResponse> getMyTeams(UUID mentorId) {

        List<TrackMentor> mentors =
                trackMentorRepository.findByUserId(mentorId);

        List<MentorTeamResponse> result = new ArrayList<>();

        for (TrackMentor mentor : mentors) {

            List<RoundParticipant> participants =
                    roundParticipantRepository.findByRoundTrackId(
                            mentor.getRoundTrackId());

            for (RoundParticipant participant : participants) {

                Team team = teamRepository.findById(participant.getTeamId()).orElseThrow(() ->
                                new RuntimeException("Team not found"));

                MentorTeamResponse response =
                        new MentorTeamResponse();

                response.setTeamId(team.getId());
                response.setTeamName(team.getName());
                response.setRoundTrackId(participant.getRoundTrackId());

                result.add(response);
            }
        }

        return result;
    }
}
