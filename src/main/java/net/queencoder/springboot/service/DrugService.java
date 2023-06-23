package net.queencoder.springboot.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.queencoder.springboot.model.CbaProcedure;
import net.queencoder.springboot.model.Drug;
import com.univocity.parsers.common.record.Record;


public interface DrugService {
	public List<Drug> uploadData(MultipartFile file) throws Exception;
	public List<Drug> matchProcedures(List<Record> records);
	public List<Drug> saveMatches(List<Drug> drug);
	public List<Drug> getAllMatches();
	public List<CbaProcedure> getAllCbaPrecedures();
	public List<Drug> getMatchesById(long id);
	public List<Drug>getAccurateMatch();
	public Drug updateMatches(long id);
	public void deleteAllMatchesWhereIdIs(Drug drug);
	public String authoriseMatch(long id);
//	public List<CbaProcedure>findPossibleMatches(long id);
//	public List<CbaProcedure> getAllCbaPrecedures();
//	public List<Drug> findAcurateMatches(MultipartFile file) throws Exception;
//	public List<Record> uploadReceivedProcedures(MultipartFile file) throws Exception;
//	public Drug updateProcedure(CbaProcedure cbaProcedure, long id);
}
