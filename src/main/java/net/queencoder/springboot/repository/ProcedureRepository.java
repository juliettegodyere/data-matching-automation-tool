package net.queencoder.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.Status;

public interface ProcedureRepository extends JpaRepository<Procedure, Long>{
	List<Procedure> findByName(String name);
    List<Procedure> findByLookUpCode(String lookUpCode);
	Page<Procedure> findByStatusIn(List<Status> statuses, Pageable pageable);

    @Query("SELECT p FROM Procedure p WHERE " +
            "LOWER(p.procedureClass) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.lookUpName) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.lookUpCode) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.quantity) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.rate) LIKE LOWER(concat('%', :query, '%')) OR " +
            "LOWER(p.status) LIKE LOWER(concat('%', :query, '%'))")
    Page<Procedure> search(@Param("query") String query, Pageable pageable);

    List<Procedure> findByStatus(Status status);
    
}
