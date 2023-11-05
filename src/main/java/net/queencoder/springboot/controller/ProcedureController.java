package net.queencoder.springboot.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.queencoder.springboot.exception.CustomNotFoundException;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.ProcedureLookUp;
import net.queencoder.springboot.model.Status;
import net.queencoder.springboot.service.ProcedureService;

@Controller
// @RequestMapping("/api/v1")
@Slf4j
public class ProcedureController {

    @Autowired
    public ProcedureService procedureService;

    @GetMapping("/page/{pageNo}")
    public String findPaginated(
            @PathVariable(value = "pageNo") int pageNo,
            @RequestParam("sortField") String sortField,
            @RequestParam("sortDir") String sortDir,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "statuses", defaultValue = "ACCEPTED,UNACCEPTED") String statuses,
            Model model) {
        int pageSize = 20;

        List<Status> statusEnums = Arrays.stream(statuses.split(","))
                .map(Status::valueOf)
                .collect(Collectors.toList());

        Page<Procedure> page = procedureService.findPaginated(pageNo, pageSize, sortField, sortDir, query, statusEnums);
        List<Procedure> procedures = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("procedures", procedures);
        List<String> staticStatus = Arrays.asList("ACCEPTED", "UNACCEPTED", "REJECTED");
        model.addAttribute("staticStatus", staticStatus);
        model.addAttribute("statuses", statuses);
        model.addAttribute("query", query);
        return "index";
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        return findPaginated(1, "editDistance", "asc", "", "ACCEPTED,UNACCEPTED", model);
    }

    // This is to test the procedure upload
    @PostMapping("/api/v1/upload")
    public ResponseEntity<List<Procedure>> uploadDrug(@RequestParam("file") MultipartFile file) throws Exception {
        return new ResponseEntity<>(procedureService.uploadData(file), HttpStatus.OK);
    }

    @GetMapping("/upload-schedule")
    public String uploadSchedule() {
        return "upload";
    }

    @PostMapping("/upload-csv-file")
    public String uploadCSVFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("operationType") String operationType,
            Model model) throws Exception {
        if ("create".equals(operationType)) {
            // Process the file for updating existing data
            log.info("Uploading new schedule");
            List<Procedure> procedures = procedureService.uploadData(file);
        } else if ("update".equals(operationType)) {
            // Process the file for creating new data
            log.info("Updating look up table");
            procedureService.uploadLookUpData(file);
        }

        return "redirect:/";
    }

    /**
     * This method is used to accept or reject just a single record
     */
    @GetMapping("/updateStatus/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam(value = "status") String status,
            HttpServletRequest request,
            Model model) throws CustomNotFoundException {
        Procedure existingProcedure = procedureService.findById(id);
        List<Procedure> lookupCodeResult = procedureService.findByLookUpCode(existingProcedure);

        String referer = request.getHeader("Referer");

        if (lookupCodeResult.size() == 1) {
            procedureService.updateStatus(existingProcedure, "accept".equals(status)? Status.ACCEPTED : Status.REJECTED);
        } else {
            procedureService.updateStatusInBatches(lookupCodeResult, "accept".equals(status)? Status.REJECTED : null);
            procedureService.updateStatus(existingProcedure, "accept".equals(status)? Status.ACCEPTED : Status.REJECTED);
        }
        return "redirect:" + referer;
    }

    @GetMapping("/matches/{id}")
    public String findAllMatches(
            @PathVariable Long id,
            HttpServletRequest request,
            Model model) throws CustomNotFoundException {
        Procedure existingProcedure = procedureService.findById(id);
        List<Procedure> lookupCodeResult = procedureService.findByLookUpCode(existingProcedure);

        String referer = request.getHeader("Referer");

        if (lookupCodeResult != null && !lookupCodeResult.isEmpty()) {
            model.addAttribute("procedures", lookupCodeResult);
            return "modal";
        } else {
            model.addAttribute("message", "No matches found for this record");
            return "modal"; // Return the same view to display the message
        }
    }
    
    @GetMapping("/bulkAccept")
    public String bulkAccept(
        @RequestParam("recordIds") List<Long> recordIds,
        HttpServletRequest request,
        Model model
    ) throws CustomNotFoundException {
        String referer = request.getHeader("Referer");
        if (!recordIds.isEmpty()) {
            List<Procedure> selectProcedures = procedureService.getAllById(recordIds);
            procedureService.markProcedureAsRejected(selectProcedures);
            
            procedureService.updateStatusInBatches(selectProcedures, Status.ACCEPTED);
        }

        return "redirect:" + referer;
    }

     @GetMapping("/bulkReject")
    public String bulkReject(
        @RequestParam("recordIds") List<Long> recordIds,
        HttpServletRequest request,
        Model model
    ) throws CustomNotFoundException {
        String referer = request.getHeader("Referer");
        log.info("IDs {}", recordIds);
        if (!recordIds.isEmpty()) {
             List<Procedure> selectProcedures = procedureService.getAllById(recordIds);
            
            procedureService.updateStatusInBatches(selectProcedures, Status.REJECTED);
        }

        return "redirect:" + referer;
    }
    
    /*
     * This method is to rematch records when CBA code matches to multiple records
     */
    // @GetMapping("/rematch/{id}")
    // public String rematchAndUpdateStatus(
    //         @PathVariable Long id,
    //          @RequestParam(value = "status") String status,
    //         HttpServletRequest request,
    //         Model model) throws CustomNotFoundException {
    //     Procedure existingProcedure = procedureService.findById(id);
    //     List<Procedure> lookupCodeResult = procedureService.findByLookUpCode(existingProcedure);
    //       List<Procedure> updatedProcedures = new ArrayList<>();
        
    //     if("accept".equals(status)){
    //         existingProcedure.setStatus(Status.ACCEPTED);
    //          updatedProcedures = lookupCodeResult.stream()
    //             .filter(record -> record.getId() != existingProcedure.getId())
    //             .peek(record -> record.setStatus(Status.REJECTED))
    //             .collect(Collectors.toList());
    //     }else if ("reject".equals(status)) {
    //            updatedProcedures = lookupCodeResult.stream()
    //             .peek(record -> record.setStatus(Status.REJECTED))
    //             .collect(Collectors.toList());
    //     }
    //     updatedProcedures.add(existingProcedure);

    //     if (!updatedProcedures.isEmpty()) {
    //         procedureService.updateStatusInBatches(updatedProcedures);
    //     }
    //     String referer = request.getHeader("Referer");
    //     return "redirect:" + referer; // Redirect back to the original page
    // }

    @GetMapping("/bulkDownload")
    public String bulkDownload(
        @RequestParam List<Long> recordIds,
        HttpServletRequest request
        ) {
            log.info("IDs {}", recordIds);
        // Fetch records from the database using the recordIds
        List<Procedure> records = procedureService.getRecordsByIds(recordIds);

        // Create a downloadable resource (e.g., a zip file containing records)
        procedureService.createDownloadableResource(records);

        // Return the resource as a ResponseEntity
         String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    //This method is used to download records using their status. The bulk upload replaced this
    @GetMapping("/download/{status}")
    public String downloadRecordsByStatus(@PathVariable String status, HttpServletRequest request) {
        procedureService.downloadRecordsByStatus(status);
        // return "redirect:/";
        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    // This method is used as an API to upload the lookup table procedures. 
    @PostMapping("/api/v1/upload-lookup")
    public ResponseEntity<List<ProcedureLookUp>> uploadLookUpData(@RequestParam("File") MultipartFile file)
            throws Exception {
        return new ResponseEntity<>(procedureService.uploadLookUpData(file), HttpStatus.OK);
    }
}
