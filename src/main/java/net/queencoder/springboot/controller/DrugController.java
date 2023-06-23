package net.queencoder.springboot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.queencoder.springboot.model.CbaProcedure;
import net.queencoder.springboot.model.Drug;
import net.queencoder.springboot.service.DrugService;

@Controller
public class DrugController {
	
	@Autowired
	public DrugService drugService;
	
	@GetMapping("/")
    public String index() {
        return "index";
    }
	
	//For API test
	@PostMapping("/upload")
	public ResponseEntity<List<Drug>> uploadDrug(@RequestParam("file") MultipartFile file) throws Exception {
		return new ResponseEntity<>(drugService.uploadData(file), HttpStatus.OK);
	}
	@PostMapping("/upload-csv-file")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, Model model) {

        // validate file
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            model.addAttribute("status", false);
            return "redirect:/";
        } else {
        		try {
                // save users list on model
        		List<Drug> drugs = drugService.uploadData(file);
                model.addAttribute("drugs", drugs);
                model.addAttribute("status", true);
                model.addAttribute("message", "Upload was successful, please view records.");

            } catch (Exception ex) {
            	model.addAttribute("status", false);
                model.addAttribute("message", ex.getMessage());
                return "redirect:/";
//            	if(ex.getMessage().equals("Some records already exists. Download matches first")) {
//            		return "redirect:/view-matches";
//            	}else {
//            		model.addAttribute("status", false);
//                    model.addAttribute("message", ex.getMessage());
//                    return "redirect:/";
//                    
//            	}
            	
            }
        }

        return "redirect:/view-matches";
    }
	
	// display list of employees
    @GetMapping("/view-matches")
    public String viewHomePage(Model model) {
        model.addAttribute("drugs", drugService.getAllMatches());
        return "all_matches";
    }
    
    @GetMapping("/authorise/{id}")
	public String authorisedMatch(@PathVariable(value = "id") long id) {
    	drugService.authoriseMatch(id);
    	return "redirect:/view-matches";
	}
    @GetMapping("/matches/{id}")
	public String possibleMatch(@PathVariable(value = "id") long id, Model model) {
    	List<Drug> matches = drugService.getMatchesById(id);
    	model.addAttribute("matches", matches);
    	return "redirect:/view-matches";
	}
    @PutMapping("/update/{id}")
    public String UpdateProcedure(@PathVariable(value = "id") long id) {
    	drugService.updateMatches(id);
        return "redirect:/view-matches";
    }
}
