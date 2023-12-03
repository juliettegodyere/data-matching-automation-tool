package net.queencoder.springboot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.queencoder.springboot.model.Claim;
import net.queencoder.springboot.model.Procedure;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

     @Query("SELECT c FROM Claim c WHERE " +
            "LOWER(c.hospitalName) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(c.narration) LIKE LOWER(concat('%', :query, '%'))")
    Page<Claim> search(@Param("query") String query, Pageable pageable);

    Optional<Claim> findByHospitalNameAndNarration(String hospitalName, String narration);

    Page<Claim> findAll(Specification<Claim> spec, Pageable pageable);

    // List<Procedure> findProceduresByClaim(Procedure procedure);
    List<Claim> findAll(Specification<Claim> spec);

    
}
