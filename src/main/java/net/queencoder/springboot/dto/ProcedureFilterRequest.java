package net.queencoder.springboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureFilterRequest {
    private String name;
    private String lookUpName;
    private String lookUpCode;
    private Integer editDistance;
    private Long claimId = 1L;
    private String statuses = "ACCEPTED,UNACCEPTED";
    private String pageSize = "50";
    private String sortField = "editDistance";
    private String sortDir = "asc";
}
