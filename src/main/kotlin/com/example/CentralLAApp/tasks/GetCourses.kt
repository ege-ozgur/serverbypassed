//
//
//import com.example.CentralLAApp.service.HTTPService
//import org.springframework.http.HttpHeaders
//import org.springframework.http.ResponseEntity
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//
//
//@Component
//class GetCourses (private val httpService: HTTPService) {
//    @Scheduled(fixedDelay = 6 * 30 * 24 * 60 * 60 * 1000) // 6 months in milliseconds
//    fun fetchAndProcessJson() {
//        val versionNumber = 35
//        val coursesUrl = "https://aburakayaz.github.io/suchedule/data-v${versionNumber}.min.json"
//        val response: ResponseEntity<String> = httpService.get(coursesUrl)
//        val json = response.body
//
//
//        if (response.statusCode.is2xxSuccessful) {
//            // Perform your desired operations on the JSON data
//            processJsonData(json)
//        } else {
//            // Handle the case where the request was not successful (e.g., log an error)
//            println("HTTP request failed with status code ${response.statusCode}")
//        }
//    }
//
//    private fun processJsonData(json: String?) {
//        // Implement your JSON processing logic here
//        if (json != null) {
//
//        } else {
//            // Handle the case where the JSON data is null (e.g., log an error)
//            println("Received null JSON data")
//        }
//    }
//}