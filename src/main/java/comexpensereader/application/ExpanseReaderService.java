package comexpensereader.application;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import comexpensereader.model.Expanse;
import comexpensereader.model.ExpanseDto;
import comexpensereader.model.ExpanseRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExpanseReaderService {

    public static final String DEPARTMENTS = "departments";
    public static final String PROJECT_NAME = "project_name";
    public static final String AMOUNT = "amount";
    public static final String DATE = "date";
    public static final String MEMBER_NAME = "member_name";
    public static final String EXPANSES_CSV = "csv/expanses.csv";

    public List<ExpanseDto> filterExpanses(ExpanseRequest expanseRequest) throws Exception {
        List<Expanse> expanseList = buildExpanses(readExpanseCsv());
        List<Expanse> filteredExpanseList = filterExpanses(expanseRequest, expanseList);
        return mapToExpanseDtoList(filteredExpanseList);
    }

    public List<ExpanseDto> sortExpanses(String sort, String order) throws Exception {
        List<Expanse> expanseList = buildExpanses(readExpanseCsv());
        List<Expanse> filteredExpanseList = sortExpanses(sort,order, expanseList);
        return mapToExpanseDtoList(filteredExpanseList);
    }
    public ExpanseDto getExpanse(String fields) throws Exception {
        Optional<Expanse> filteredExpanse = buildExpanses(readExpanseCsv()).stream().findAny();
        Expanse expanseWithSparseFields = singleExpenseWithSparseFields(fields,filteredExpanse.get());
        return mapToExpanseDto(expanseWithSparseFields);
    }

    public Double totalSum(String by) throws Exception {
        List<Expanse> expanseList = buildExpanses(readExpanseCsv());
        return totalSumByField(by,  expanseList);
    }

    private List<Expanse> filterExpanses(ExpanseRequest expanseRequest, List<Expanse> expanseList) {
        Stream<Expanse> expenseStream = expanseList.stream();
        if (!Objects.isNull(expanseRequest.departments())) {
            expenseStream = expenseStream.filter(expanse -> expanse.getDepartments().equals(expanseRequest.departments()));
        }
        if (!Objects.isNull(expanseRequest.projectName())) {
            expenseStream = expenseStream.filter(expanse -> expanse.getProjectName().equals(expanseRequest.projectName()));
        }
        if (!Objects.isNull(expanseRequest.amount())) {
            expenseStream = expenseStream.filter(expanse -> expanse.getAmount().equals(expanseRequest.amount()));
        }
        if (!Objects.isNull(expanseRequest.date())) {
            expenseStream = expenseStream.filter(expanse -> expanse.getDate().equals(expanseRequest.date()));
        }
        if (!Objects.isNull(expanseRequest.memberName())) {
            expenseStream = expenseStream.filter(expanse -> expanse.getMemberName().equals(expanseRequest.memberName()));
        }
        return expenseStream.collect(Collectors.toList());
    }

    private List<Expanse> sortExpanses(String sort, String order, List<Expanse> expanseList) {
        Stream<Expanse> expenseStream = expanseList.stream();
        boolean desc = !Objects.isNull(order) && order.equalsIgnoreCase("desc");

        if (sort.equalsIgnoreCase(DEPARTMENTS)) {
            expenseStream = desc ? expenseStream.sorted(Comparator.comparing(Expanse::getDepartments).reversed()) :
                                   expenseStream.sorted(Comparator.comparing(Expanse::getDepartments));
        }
        if (sort.equalsIgnoreCase(PROJECT_NAME)) {
            expenseStream = desc ? expenseStream.sorted(Comparator.comparing(Expanse::getProjectName).reversed()) :
                                   expenseStream.sorted(Comparator.comparing(Expanse::getProjectName));
        }
        if (sort.equalsIgnoreCase(AMOUNT)) {
            expenseStream = desc ? expenseStream.sorted(Comparator.comparing(Expanse::getAmount).reversed()) :
                                   expenseStream.sorted(Comparator.comparing(Expanse::getAmount));
        }
        if (sort.equalsIgnoreCase(DATE)) {
            expenseStream = desc ? expenseStream.sorted(Comparator.comparing(Expanse::getDate).reversed()) :
                                   expenseStream.sorted(Comparator.comparing(Expanse::getDate));
        }
        if (sort.equalsIgnoreCase(MEMBER_NAME)) {
            expenseStream = desc ? expenseStream.sorted(Comparator.comparing(Expanse::getMemberName).reversed()) :
                                   expenseStream.sorted(Comparator.comparing(Expanse::getMemberName));
        }
        return expenseStream.collect(Collectors.toList());
    }

    private Expanse singleExpenseWithSparseFields(String fields,Expanse filteredExpanse) {
        Expanse.ExpanseBuilder expanse = null;
        String[] fieldsArray = fields.split(",");
        for (String field: fieldsArray) {
            expanse = Expanse.builder();
            if (field.equalsIgnoreCase(DEPARTMENTS)) {
                expanse.departments(filteredExpanse.getDepartments());
            }
            if (field.equalsIgnoreCase(PROJECT_NAME)) {
                expanse.projectName(filteredExpanse.getProjectName());
            }
            if (field.equalsIgnoreCase(AMOUNT)) {
                expanse.amount(filteredExpanse.getAmount());
            }
            if (field.equalsIgnoreCase(DATE)) {
                expanse.date(filteredExpanse.getDate());
            }
            if (field.equalsIgnoreCase(MEMBER_NAME)) {
                expanse.memberName(filteredExpanse.getMemberName());
            }
        }
        return expanse.build();
    }

    private Double totalSumByField(String field,List<Expanse> expanseList) {
        Stream<Expanse> expenseStream = expanseList.stream();
        if (field.equalsIgnoreCase(DEPARTMENTS)) {
            expenseStream = expenseStream.filter(expanse -> field.equalsIgnoreCase(expanse.getDepartments()));
        }
        else if (field.equalsIgnoreCase(PROJECT_NAME)) {
            expenseStream = expenseStream.filter(expanse -> field.equalsIgnoreCase(expanse.getProjectName()));
        }
        else if (field.equalsIgnoreCase(AMOUNT)) {
            expenseStream = expenseStream.filter(expanse -> field.equalsIgnoreCase(expanse.getAmount().toString()));
        }
        else if (field.equalsIgnoreCase(DATE)) {
            expenseStream = expenseStream.filter(expanse -> field.equalsIgnoreCase(expanse.getDate().toString()));
        }
        else if (field.equalsIgnoreCase(MEMBER_NAME)) {
            expenseStream = expenseStream.filter(expanse -> field.equalsIgnoreCase(expanse.getMemberName()));
        }

        return expenseStream.
                mapToDouble(expanse -> Double.parseDouble(removeSpecialCharactersFromAmount(expanse.getAmount()))).sum();
    }


    private List<String[]> readExpanseCsv() throws Exception {
        Reader reader = Files.newBufferedReader(Paths.get(
                        ClassLoader.getSystemResource(EXPANSES_CSV).toURI()));

        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        List<String[]> expenses = csvReader.readAll();
        reader.close();
        csvReader.close();
        return expenses;
    }

    private List<Expanse> buildExpanses(List<String[]> expanses) throws ParseException {
        List<Expanse> expanseList = new ArrayList<>();
        Expanse expanse;
        for(String[] exp : expanses) {
            expanse =  Expanse.builder().departments(exp[0]).projectName(exp[1]).
                       amount(exp[2]).
                       date(exp[3]).
                       memberName(exp[4]).build();
            expanseList.add(expanse);
        }
        return expanseList;
    }

    private String removeSpecialCharactersFromAmount(String amount) {
        return amount.replace("€","").replace(",","");
    }

   private Date convertStringToDate(String date) throws ParseException {
       return new SimpleDateFormat("dd/MM/yyyy").parse(date);
   }

   private List<ExpanseDto> mapToExpanseDtoList(List<Expanse> filteredExpanseList) {
       List<ExpanseDto> expanseDtoList = new ArrayList<>();
       for (Expanse filteredExpanse : filteredExpanseList) {
           expanseDtoList.add(new ExpanseDto(filteredExpanse.getDepartments(), filteredExpanse.getProjectName(),
                   filteredExpanse.getAmount(), filteredExpanse.getDate(), filteredExpanse.getMemberName()));
       }
       return expanseDtoList;
   }

   private ExpanseDto mapToExpanseDto(Expanse filteredExpanse) {
        return new ExpanseDto(filteredExpanse.getDepartments(),filteredExpanse.getProjectName(),filteredExpanse.getAmount(),
                    filteredExpanse.getDate(), filteredExpanse.getMemberName());
   }

}
