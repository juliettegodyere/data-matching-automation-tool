package net.queencoder.springboot.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import net.queencoder.springboot.dto.ProcedureFilterRequest;
import net.queencoder.springboot.exception.CustomNotFoundException;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.ProcedureLookUp;
import net.queencoder.springboot.model.Status;

public interface ProcedureService {
	public List<Procedure> uploadData(MultipartFile file) throws Exception;
	public List<ProcedureLookUp> getAllExistingPrecedures();
	public List<Procedure> getAllMatches();
	public List<Procedure> saveMatches(List<Procedure> procedure);
	public List<ProcedureLookUp> uploadLookUpData(MultipartFile file) throws Exception;
	public Page<Procedure> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection, ProcedureFilterRequest filterRequest);
	public Procedure findById(Long id) throws CustomNotFoundException;
	public List<Procedure> getAllById(List<Long> id) throws CustomNotFoundException;
	public Procedure updateStatus(Procedure existingProcedure, Status status);
    public void downloadRecordsByStatus(String status);
	public List<Procedure> findByLookUpCode(Procedure existingLookUpCode);
	public List<Procedure> updateStatusInBatches(List<Procedure> lookupCodeResult, Status status);
    public List<Procedure> getRecordsByIds(List<Long> recordIds);
    public void createDownloadableResource(ProcedureFilterRequest filterRequest) throws CustomNotFoundException;
	public void markProcedureAsRejected(List<Procedure> procedures);
    public List<Procedure> deleteSelectedRecords(ProcedureFilterRequest filterRequest);
}
