package com.fpt.sealhackathon.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionRequest {

    private String repoUrl;

    private String demoUrl;

    private String slideUrl;

    private String reportUrl;

    private String apiMetadata;
}
