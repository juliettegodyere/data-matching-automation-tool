package net.queencoder.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.queencoder.springboot.model.CbaProcedure;

public interface CbaProcedureRepository extends JpaRepository<CbaProcedure, Long>{
	
}
