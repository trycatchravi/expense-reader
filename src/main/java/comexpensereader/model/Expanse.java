package comexpensereader.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class Expanse {
    private String departments;
    private String projectName;
    private String amount;
    private String date;
    private String memberName;
}
