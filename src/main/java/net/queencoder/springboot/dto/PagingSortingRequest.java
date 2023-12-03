package net.queencoder.springboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagingSortingRequest {
    private int pageNo = 1;
    private String pageSize = "50";
    private String sortField = "editDistance";
    private String sortDir = "asc";
}
