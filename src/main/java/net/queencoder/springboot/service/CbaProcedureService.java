package net.queencoder.springboot.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.queencoder.springboot.model.CbaProcedure;

public interface CbaProcedureService {
	List<CbaProcedure> createProcedure(MultipartFile file) throws IOException;
	List<CbaProcedure> getAllProcedures();
	CbaProcedure getProcedureById(long id);
}
