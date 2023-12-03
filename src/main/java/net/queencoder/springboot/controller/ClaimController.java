package net.queencoder.springboot.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.queencoder.springboot.dto.ClaimFilterRequest;
import net.queencoder.springboot.dto.ProcedureFilterRequest;
import net.queencoder.springboot.exception.CustomNotFoundException;
import net.queencoder.springboot.model.Claim;
import net.queencoder.springboot.model.Procedure;
import net.queencoder.springboot.model.Status;
import net.queencoder.springboot.service.ClaimService;

@Controller
@AllArgsConstructor
public class ClaimController {
     @Autowired
    public ClaimService claimService;

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo", required = false) Optional<Integer> pageNoOptional, @ModelAttribute ClaimFilterRequest filterRequest, Model model) {

        int pageSize = Integer.parseInt(filterRequest.getPageSize());
        int pageNo = pageNoOptional.orElse(1);


        List<Status> statusEnums = Arrays.stream(filterRequest.getStatuses().split(","))
                .map(Status::valueOf)
                .collect(Collectors.toList());

        Page<Claim> page = claimService.findPaginated(pageNo, pageSize, filterRequest.getSortField(), filterRequest.getSortDir(),
                statusEnums, filterRequest);
        //  Page<Procedure> page = procedureService.findPaginated_v2(pageNo, filterRequest);
        List<Claim> claim = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        model.addAttribute("sortField", filterRequest.getSortField());
        model.addAttribute("sortDir", filterRequest.getSortDir());
        model.addAttribute("reverseSortDir", filterRequest.getSortDir().equals("asc") ? "desc" : "asc");

        model.addAttribute("statuses", filterRequest.getStatuses());

         model.addAttribute("claims", claim);
         model.addAttribute("claimId", filterRequest.getSortField());


        int previousPage = Math.max(1, pageNo - 1);
        int nextPage = Math.min(page.getTotalPages(), pageNo + 1);

        model.addAttribute("previousPage", previousPage);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("filterRequest", filterRequest);

        return "index";
    }

    @GetMapping("/")
    public String viewHomePage(@PathVariable(value = "pageNo", required = false) Optional<Integer> pageNoOptional,  @ModelAttribute("filterRequest")  ClaimFilterRequest filterRequest,
    Model model) {

        return findPaginated(pageNoOptional,filterRequest, model);
    }

     @GetMapping("/page/{currentPage}/delete")
    public String bulkDelete(
            @ModelAttribute ClaimFilterRequest filterRequest,
            RedirectAttributes redirectAttributes,
            @PathVariable(value = "currentPage") int currentPage,
            HttpServletRequest request,
            Model model) throws CustomNotFoundException {
        List<Claim> deletedClaim = claimService.deleteSelectedRecords(filterRequest);
        String referer = request.getHeader("Referer");
    
        // Add flash attributes for success and deleted records
        if (!deletedClaim.isEmpty()) {
            redirectAttributes.addFlashAttribute("successMessage", "Records successfully deleted.");
            redirectAttributes.addFlashAttribute("deletedClaims", deletedClaim);
    
            return "redirect:/page/" + currentPage;
        } else {
            return "redirect:" + referer;
        }
    }
    
}