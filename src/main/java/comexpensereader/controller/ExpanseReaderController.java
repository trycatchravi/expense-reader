package comexpensereader.controller;

import comexpensereader.application.ExpanseReaderService;
import comexpensereader.model.ExpanseDto;
import comexpensereader.model.ExpanseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/expanses_data")
@RequiredArgsConstructor
public class ExpanseReaderController {

    private final ExpanseReaderService expanseReaderService;

    @GetMapping("/filter")
    public ResponseEntity<?> filter(@RequestParam(required = false)  String departments, @RequestParam(required = false) String projectName,
                                    @RequestParam(required = false) Double amount,@RequestParam(required = false) Date date,
                                    @RequestParam(required = false) String memberName) {
        try {
            List<ExpanseDto> expanseList = expanseReaderService.filterExpanses(mapRequest(departments,projectName,amount,date,memberName));
            return ResponseEntity.status(HttpStatus.OK).body(expanseList);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/sort")
    public ResponseEntity<?> sort(@RequestParam String sort, @RequestParam(required = false) String order) {
        try {
            List<ExpanseDto> expanseList = expanseReaderService.sortExpanses(sort,order);
            return ResponseEntity.status(HttpStatus.OK).body(expanseList);
        }
        catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/getSparseFields")
    public ResponseEntity<?> get(@RequestParam String fields) {
        try {
            ExpanseDto expanseDto = expanseReaderService.getExpanse(fields);
            return ResponseEntity.status(HttpStatus.OK).body(expanseDto);
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/aggregates")
    public ResponseEntity<?> totalSum(@RequestParam String by) {
        try {
            Double totalSum = expanseReaderService.totalSum(by);
            return ResponseEntity.status(HttpStatus.OK).body(totalSum);
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    private ExpanseRequest mapRequest(String departments, String projectName, Double amount, Date date, String memberName) {
        return new ExpanseRequest(departments,projectName,amount,date, memberName);
    }

}
