import org.apache.commons.csv.CSVFormat
import groovy.json.JsonSlurper

try {
  def apiUrl = 'https://eltex-co.ru/test/users.php'
  def outputPath = 'output.csv'

  def responseData = httpGet(apiUrl)

  if (responseData) {
    def usersData = parseResponse(responseData)

    if (usersData != null) {
      //If need to filter empty records - uncomment
      //usersData = filterRecordsWithEmptyFields(usersData)

      def filteredUsers = filterUsersBySalary(usersData, 3500)
      def sortedUsers = sortUsersByName(filteredUsers)

      outputToCSV(sortedUsers, outputPath)
      println('CSV file has been generated successfully.')
    } else {
      println('Failed to parse user data.')
    }
  } else {
    println('Failed to fetch data from the URL.')
  }
} catch (Exception e) {
  println('An error occurred: ' + e.message)
}

def static httpGet(String url) {
  try {
    URL apiUrl = new URL(url)
    HttpURLConnection connection = apiUrl.openConnection()
    connection.setRequestMethod("GET")
    connection.setReadTimeout(60000)

    int responseCode = connection.getResponseCode()

    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
      StringBuilder response = new StringBuilder()
      String line

      while ((line = reader.readLine()) != null) {
        response.append(line)
      }
      reader.close()

      return response.toString()
    } else {
      println("Request failed with status code: ${responseCode}")
      return null
    }
  } catch (Exception e) {
    println("An error occurred: ${e.message}")
    return null
  }
}

def static parseResponse(String response) {
  try {
    def slurper = new JsonSlurper()
    def data = slurper.parseText(response)

    if (!data instanceof List) {
      println("Invalid response format. Expected an array of records.")
      return null
    }
    return data
  } catch (Exception e) {
    println("An error occurred while parsing the response: ${e.message}")
  }
}

def static filterUsersBySalary(users, minSalary) {
  return users.findAll { user -> user.salary > minSalary }
}

def static sortUsersByName(users) {
  return users.sort { a, b -> a.name <=> b.name }
}

def static filterRecordsWithEmptyFields(users) {
  return users.findAll { user -> hasNoEmptyFields(user)}
}

def static hasNoEmptyFields(user) {
  return user.id != null && !user.id.isEmpty() &&
         user.name != null && !user.name.isEmpty() &&
         user.email != null && !user.email.isEmpty() &&
         user.salary != null
}

def static outputToCSV(users, outputPath) {
  def outputFile = new File(outputPath)
  def csvPrinter = CSVFormat.DEFAULT.withHeader("id", "name", "email", "salary").print(outputFile.newWriter())

  users.each { user ->
    csvPrinter.printRecord(user.id, user.name, user.email, user.salary)
  }

  csvPrinter.close()
}

