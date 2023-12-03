package net.queencoder.springboot.dto;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProcedureSpecifications {

    public static Specification<Procedure> filterByProcedureRequest(ProcedureFilterRequest filterRequest) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // if (filterRequest.getStatuses() != null && !filterRequest.getStatuses().isEmpty()) {
               
            // } else {
            //     List<Status> defaultStatusEnums = Arrays.stream("ACCEPTED,UNACCEPTED".split(","))
            //             .map(Status::valueOf)
            //             .collect(Collectors.toList());
    
            //     predicates.add(root.get("status").in(defaultStatusEnums));
            // }

             List<Status> validStatusEnums = Arrays.stream(filterRequest.getStatuses().split(","))
                .map(String::trim)
                .filter(status -> {
                    try {
                        Status.valueOf(status);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(Status::valueOf)
                .collect(Collectors.toList());
    
            if (!validStatusEnums.isEmpty()) {
                predicates.add(root.get("status").in(validStatusEnums));
            }

            if (filterRequest.getName() != null) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + filterRequest.getName().toLowerCase() + "%"));
            }

            if (filterRequest.getLookUpName() != null) {
                predicates.add(builder.like(builder.lower(root.get("lookUpName")), "%" + filterRequest.getLookUpName().toLowerCase() + "%"));
            }

            if (filterRequest.getLookUpCode() != null) {
                predicates.add(builder.like(builder.lower(root.get("lookUpCode")), "%" + filterRequest.getLookUpCode().toLowerCase() + "%"));
            }
            if (filterRequest.getEditDistance() != null) {
                predicates.add(builder.equal(root.get("editDistance"), filterRequest.getEditDistance()));
            }

            if (filterRequest.getClaimId() != null) {
                predicates.add(builder.equal(root.get("claim").get("id"), filterRequest.getClaimId()));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
