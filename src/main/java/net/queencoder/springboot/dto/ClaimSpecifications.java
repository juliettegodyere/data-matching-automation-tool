package net.queencoder.springboot.dto;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import net.queencoder.springboot.model.Claim;

import java.util.ArrayList;
import java.util.List;
public class ClaimSpecifications {
    public static Specification<Claim> filterByClaimRequest(ClaimFilterRequest filterRequest) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            
            if (filterRequest.getHospitalName() != null) {
                predicates.add(builder.like(builder.lower(root.get("hospitalName")), "%" + filterRequest.getHospitalName().toLowerCase() + "%"));
            }

            // if (filterRequest.getClaimId() != null) {
            //     predicates.add(builder.equal(root.get("id"), filterRequest.getClaimId()));
            // }

            if (filterRequest.getNarration() != null) {
                predicates.add(builder.like(builder.lower(root.get("narration")), "%" + filterRequest.getNarration().toLowerCase() + "%"));
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
