package net.queencoder.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.queencoder.springboot.dto.ProcedureFilterRequest;
import net.queencoder.springboot.model.Claim;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.Status;
import org.springframework.data.jpa.domain.Specification;

public interface ProcedureRepository extends JpaRepository<Procedure, Long>{
	List<Procedure> findByName(String name);
    List<Procedure> findByLookUpCode(String lookUpCode);
    List<Procedure> findByLookUpCodeAndStatus(String lookUpCode, Status status);

	Page<Procedure> findByStatusIn(List<Status> statuses, Pageable pageable);

    Page<Procedure> findByClaim(Claim claim, Pageable pageable);
    
    Page<Procedure> findAll(Specification<Procedure> spec, Pageable pageable);

    List<Procedure> findAll(Specification<Procedure> spec);

    List<Procedure> findByStatus(Status status);
    
}
