package net.queencoder.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.queencoder.springboot.model.ProcedureLookUp;

public interface ProcedureLookUpRepository extends JpaRepository<ProcedureLookUp, Long>{
	ProcedureLookUp findByCode(String code);
}
