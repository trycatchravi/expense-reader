### Description
expanse-reader service reads expanses form csv and read expanses based 
headers. The data can be filtered, sorted, based on any header.
It also returns expanse based on sparse field set and calculate sum 
based on header.

### Run 
* run application with command `mvn spring-boot:run`

### Test
* Get `/expanses_data/filter`: filters expanse data based on different headers
* Get  `/expanses_data/sort`: sort  expanse data based on different headers
* Get `/expanses_data/getSparseFields`: returns random expense for sparse field-set. It's implementation can be 
improved, but I didn't understand the requirement completely.
* Get `/expanses_data/aggregates`: aggregates the amount based on field-set. It's implementation can be
  improved, but I didn't understand the requirement completely.

### Tech Stack
* JAVA 17
* Spring 2.7.0
* OpenCsv 5.6

### Improvements
* Csv can be loaded while running spring boot application.
* Implement test cases.