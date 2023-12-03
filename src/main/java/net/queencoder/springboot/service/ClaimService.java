package net.queencoder.springboot.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.micrometer.common.util.StringUtils;
import net.queencoder.springboot.dto.ClaimFilterRequest;
import net.queencoder.springboot.dto.ClaimSpecifications;
import net.queencoder.springboot.dto.ProcedureFilterRequest;
import net.queencoder.springboot.dto.ProcedureSpecifications;
import net.queencoder.springboot.model.Claim;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.Status;
import net.queencoder.springboot.repository.ClaimRepository;

@Service
public class ClaimService {

    @Autowired
	private ClaimRepository claimRepository;

	
	public Page<Claim> findPaginated(int pageNo, int pageSize, String sortField, String sortDir,List<Status> statuses, ClaimFilterRequest filterRequest) {

		Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending()
				: Sort.by(sortField).descending();		

		Pageable pageable = PageRequest.of(pageNo - 1, Integer.parseInt(filterRequest.getPageSize()), sort);
		Page<Claim> page = claimRepository.findAll(ClaimSpecifications.filterByClaimRequest(filterRequest), pageable);

        return page;
	}

    public List<Claim> getAllClaims(){
        return claimRepository.findAll();
    }

	public List<Claim> deleteSelectedRecords(ClaimFilterRequest filterRequest) {
		List<Claim> claim = claimRepository.findAll(ClaimSpecifications.filterByClaimRequest(filterRequest));
		claimRepository.deleteAll(claim);
		return claim;
	}

    
}
