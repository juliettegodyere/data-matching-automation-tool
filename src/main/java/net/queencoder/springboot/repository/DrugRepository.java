package net.queencoder.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.queencoder.springboot.model.Drug;

public interface DrugRepository extends JpaRepository<Drug, Long>{
	List<String> findByDrugName(String drugName);
}
