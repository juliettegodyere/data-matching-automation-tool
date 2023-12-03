package net.queencoder.springboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimFilterRequest {
    private String hospitalName;
    private String narration;
    // private Long claimId = 0L;
    private String pageSize = "50";
    private String sortField = "createdDate";
    private String sortDir = "asc";
    private String statuses = "ACCEPTED,UNACCEPTED";

}
