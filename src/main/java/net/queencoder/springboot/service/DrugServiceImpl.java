package net.queencoder.springboot.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import net.queencoder.springboot.model.CbaProcedure;
import net.queencoder.springboot.model.Drug;
import net.queencoder.springboot.model.ProcedureMatch;
import net.queencoder.springboot.repository.CbaProcedureRepository;
import net.queencoder.springboot.repository.DrugRepository;
import net.queencoder.springboot.repository.PrecedureMatchRepository;

@Service
public class DrugServiceImpl implements DrugService {

	@Autowired
	private DrugRepository drugRepository;

	@Autowired
	private CbaProcedureRepository cbaProcedureRepository;

	@Override
	public List<CbaProcedure> getAllCbaPrecedures() {
		return cbaProcedureRepository.findAll();
	}
	@Override
	public List<Drug> getAllMatches() {
		return drugRepository.findAll();
	}

	@Override
	public List<Drug> uploadData(MultipartFile file) throws Exception {
		if(getAllMatches().equals(null)) {
			throw new RuntimeException("Some records already exists. Download matches first");
		}else {
		InputStream inputStream;
		inputStream = file.getInputStream();
		CsvParserSettings setting = new CsvParserSettings();
		setting.setHeaderExtractionEnabled(true);
		CsvParser parser = new CsvParser(setting);
		List<Record> parseAllRecords = parser.parseAllRecords(inputStream);
		return saveMatches(matchProcedures(parseAllRecords));}
	}

	@Override
	public List<Drug> saveMatches(List<Drug> drug) {
		return drugRepository.saveAll(drug);
	}

	@Override
	public List<Drug> matchProcedures(List<Record> records) {

		List<CbaProcedure> procedureList = getAllCbaPrecedures();
		List<Drug> druglist = new ArrayList<>();

		if (procedureList != null) {
			records.forEach(record -> {

				for (int i = 0; i < procedureList.size(); i++) {

					Drug drug = new Drug();

					int m = record.getString("Drug Name").length();
					int n = procedureList.get(i).getProcedureName().length();

					int edit_dist = eD2(record.getString("Drug Name"), procedureList.get(i).getProcedureName(), m, n);

					if (getMatch(record.getString("Drug Name"), procedureList.get(i).getProcedureName(),
							edit_dist) == true) {

						drug.setMatchName(procedureList.get(i).getProcedureName());
						drug.setMatchCode(procedureList.get(i).getProcedureCode());
						drug.setFlag(false);
						drug.setStatus(false);
						drug.setEditDistance(edit_dist);
						drug.setDrugClass(record.getString("Drug Class"));
						drug.setDrugName(record.getString("Drug Name"));
						drug.setQuantity(record.getString("Quantity"));
						drug.setRate(record.getString("Rate"));
						druglist.add(drug);

					}

				}
			});
		}

		return druglist;
	}

	@Override
	public List<Drug> getMatchesById(long id) {
		Drug existingMatch = drugRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Could not find the id " + id));

		return filterMatches(getAllMatches(), existingMatch);
	}

	private List<Drug> filterMatches(List<Drug> matches, Drug existingMatch) {
		List<Drug> possibleMatches = matches.stream()
				.filter(data -> data.equals(existingMatch.getMatchName()) && data.isStatus() == false)
				.collect(Collectors.toList());
		return possibleMatches;
	}

	@Override
	public Drug updateMatches(long id) {
		List<Drug> valueToSave = new ArrayList<>();
		Drug selectedMatch = drugRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Could not find the id " + id));

		List<Drug> possibleMatches = getAllMatches().stream()
				.filter(data -> data.isStatus() == true && data.getDrugName() == selectedMatch.getDrugName())
				.collect(Collectors.toList());

		if (possibleMatches.size() != 1) {
			throw new IllegalStateException();
		}
		Drug recordToUpdate = possibleMatches.get(0);
		recordToUpdate.setStatus(false);
		selectedMatch.setStatus(true);

		valueToSave.add(recordToUpdate);
		valueToSave.add(selectedMatch);
		drugRepository.saveAll(valueToSave);

		return null;
	}

	@Override
	public List<Drug> getAccurateMatch() {
		List<Drug> procedureList = getAllMatches();
		List<Drug> druglist = new ArrayList<>();
		if (procedureList != null) {
			int minMatch = Integer.MAX_VALUE;
			Drug drug = new Drug();
			for(int i = 0; i < procedureList.size(); i++) {
				if(anagram(procedureList.get(i).getDrugName(), procedureList.get(i).getMatchName())) {
					druglist.add(procedureList.get(i));
				}else {
					List<Drug> list = filterMatches(procedureList,procedureList.get(i));
					Drug temp = new Drug();
//					for(int j = 0; j < list.size(); j++) {
//						if(list.get(j).getEditDistance() <= minMatch) {
//							minMatch = procedureList.get(j).getEditDistance();
//							temp = procedureList.get(j);
//							//druglist.add(procedureList.get(j));
//						}
//					}
//					druglist.add(temp);
				}
			}

//////			for (int i = 0; i < procedureList.size(); i++) {
//////
//////				if (anagram(procedureList.get(i).getDrugName(), procedureList.get(i).getMatchName())) {
//////					drug.setStatus(true);
//////					drug.setDrugClass(procedureList.get(i).getDrugClass());
//////					drug.setDrugName(procedureList.get(i).getDrugName());
//////					drug.setEditDistance(procedureList.get(i).getEditDistance());
//////					drug.setFlag(procedureList.get(i).isStatus());
//////					drug.setMatchCode(procedureList.get(i).getMatchCode());
//////					drug.setMatchName(procedureList.get(i).getMatchName());
//////					drug.setQuantity(procedureList.get(i).getQuantity());
//////					drug.setRate(procedureList.get(i).getRate());
//////				} else if (procedureList.get(i).getEditDistance() <= minMatch) {
//////					minMatch = procedureList.get(i).getEditDistance();
//////					drug.setStatus(true);
//////					drug.setDrugClass(procedureList.get(i).getDrugClass());
//////					drug.setDrugName(procedureList.get(i).getDrugName());
//////					drug.setEditDistance(procedureList.get(i).getEditDistance());
//////					drug.setFlag(procedureList.get(i).isStatus());
//////					drug.setMatchCode(procedureList.get(i).getMatchCode());
//////					drug.setMatchName(procedureList.get(i).getMatchName());
//////					drug.setQuantity(procedureList.get(i).getQuantity());
//////					drug.setRate(procedureList.get(i).getRate());
//////				}
////				druglist.add(drug);
//
//			}
		}
		return druglist;
	}

	@Override
	public String authoriseMatch(long id) {
		Drug existDrugs = drugRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Could not find the id " + id));
		existDrugs.setFlag(true);
		existDrugs.setStatus(true);
		drugRepository.save(existDrugs);
		deleteAllMatchesWhereIdIs(existDrugs);
		return "Success";
	}

	@Override
	public void deleteAllMatchesWhereIdIs(Drug existDrugs) {
		List<Drug> deleteUnusedMatches = getAllMatches().stream().filter(data -> data.isStatus() == false
				&& data.isFlag() == false && data.getDrugName().equals(existDrugs.getDrugName()))
				.collect(Collectors.toList());
		drugRepository.deleteAll(deleteUnusedMatches);
	}

	// This method returns the max length between the Strings
	private int getLen(int m, int n) {
		return Math.max(m, n);
	}

	// This method handles the percentage of matching
	private boolean getMatch(String str1, String str2, int edit_dist) {
		int m = str1.length(), n = str2.length();

		double percentage = 0.7;
		int maxMatch = (int) (getLen(m, n) * percentage);
		if (edit_dist < maxMatch) {
			return true;
		}
		return false;

	}

	// This method is the edit distance dynamic programming solution
	static int eD2(String s1, String s2, int m, int n) {
		int dp[][] = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));

				}
			}
		}

		return dp[m][n];
	}

	// This method ensures that the ED is less that the max length
	static final int CHAR = 256;

	public boolean anagram(String str1, String str2) {
		int m = str1.length(), n = str2.length();

		int count[] = new int[CHAR];
		if (n != m) {
			return false;
		}
		for (int i = 0; i < n; i++) {
			count[str1.charAt(i)]++;
			count[str2.charAt(i)]--;
		}
		for (int i = 0; i < CHAR; i++) {
			if (count[i] != 0) {
				return false;
			}
		}
		return true;
	}

}
