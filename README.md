Log Analyzer works by uploading log files and parsing the data and subsequently saving it to the database. It helps user to go through the logs by filtering through it.

Upload
Upload file through a post operation at localhost:8080/logs-analyzer/upload
The body must contain a form-data with Key as 'file' & Value as the required file to be uploaded.

Searching Logs
Searching of logs can be done at localhost:8080/logs-analyzer/filter-logs. Below are some sample filter criterias. Only String & Number filtering filtering is working currently. Below are some samples:

localhost:8080/logs-analyzer/filter-logs?search=responseTime:1240
localhost:8080/logs-analyzer/filter-logs?search=responseTime<400,requestType:post

Other End Points
localhost:8080/logs-analyzer/logs/{id}
localhost:8080/logs-analyzer/logs?page=0&size=2


Rate-Limiting
I'm using a Open Source Library for it. All the methods marked as @Throttling is being throttled.