package comexpensereader.model;

import java.util.Date;

public record ExpanseRequest(String departments, String projectName, Double amount, Date date, String memberName) {
}
