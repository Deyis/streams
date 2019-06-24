### Technologies
The service is using PLay Framework, Akka-Streams, Postgres.

### How to run Tests

To run tests use the following command:
```bash
sbt dockerComposeTest
```


### Docker

The solution based on the [sbt-docker-compose](https://github.com/Tapad/sbt-docker-compose) plugin.

1. To start a new instance

    `sbt dockerComposeUp`
2. To shutdown all instances

    `sbt dockerComposeStop`


### Running the application

Running the application without docker:
```bash
sbt run
```
Note: Postgres should be up and running and following environment variables should be provided:
```
DB_NAME: 
DB_USERNAME: 
DB_PASSWORD: 
DB_HOSTNAME: 
```

### Swagger documentation

Swagger documentation is available at:  
`http://localhost:9000/docs`

Note: If no docs found put into Swagger search box `/api-docs` path(it's path where swagger json representation of api available). 

Note: you can execute all the requests from the Swagger UI except upload file request. 
There is some problem with it - UI receives 403 Forbidden even though if you copy the curl command that it generates - it will work properly.
Didn't have time to investigate the reason of it.

Example of curl command(make sure that the file is in the same directory where you execute curl.): 

`curl -X POST "http://localhost:9000/banks" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "csv=@test.csv;type=text/csv"`
 
### Assumptions made:

File that uploads is the snapshot of what we want to have in DB so all previous information will be deleted after successful upload.
If file format is broken - no new data will be stored in DB's main table (all or nothing approach). Failed data will be stored in tmp table though for debugging purposes.
Data streams directly to DB so we will not try to consume file into memory which leads to constant memory usage and possibility to process big files.

### Possible improvements:
There is possibility to put max length limit for file.
Use cats types like EitherT for more typesafe error handling instead of exceptions.        
Some other improvements are mentioned as todos across the project.
 