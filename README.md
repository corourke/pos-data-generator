# Data Generator

Real-time demo data generator for point-of-sale data. 

* `data-generator.jar` -- Generates CSV data files
* `file_uploader_s3.sh` -- Uploads files to AWS S3

Walkthrough video:
https://www.dropbox.com/s/zsjykewz5lvsl4h/DataGenerator_01.mp4?dl=0

There is a shell script that uploads the CSV files to S3,
and a Java program that generates the CSV files. 

### Prerequisites

1. You need to have the AWS CLI set up and configured
2. Need to have an S3 bucket set up

### Start the uploader

`$ S3_LOCATION=s3://<your-bucket-name> ./file_uploader_s3.sh &`

### Start the file generator

`java -jar data-generator-1.0.jar -o ./output`

Add `--rate 100000` if you want to quickly generate a few files. 

Otherwise, let it run for a few minutes, or forever at about 250-1000 transactions per second (set by `--rate`).

Ctrl-C to stop
