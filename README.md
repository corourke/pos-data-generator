# Data Generator

Real-time demo data generator for point-of-sale data.

* `data-generator.jar` -- Generates CSV data files
* `file_uploader_s3.sh` -- Uploads files to AWS S3

Walkthrough video:
https://www.dropbox.com/s/zsjykewz5lvsl4h/DataGenerator_01.mp4?dl=0

There is a shell script that uploads the CSV files to S3, and a Java program that generates the CSV files. Transaction generation (rate and on/off) can be controlled via JMX.

The purpose is to be able to either generate a large amount of data, or generate continuous data in real-time.

See below for current status.

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

### Current Project Status

The code needs to be refactored quite a bit.

I'm currently generating CSV files, but I have another project that outputs to a Kafka stream that I will merge in here.

Currently, the actual production of data is way too simplistic, its imperative code and really just a stub for now. Ideally, data would be generated based on a declarative specification. I would also like to incorporate a good mocking library to bring in realistic data types, numerical constraints and string patterns. 

Other requirements:

* Able to generate data continuously, to files or a stream

* Generates foreign keys against supplied parent tables

* Uses data in parent tables to generate data in ratios (A jar of mustard is sold more often than a coffee machine)

* Can use formulas or code to implement cycles or biases for: 

  * Retail sales seasonality (holday season, summer wear, etc.)
  * Product affinity (products that are purchased together, or buyer archetypes)
  * Regional (geographic) preferences (i.e. more outerwear purchased in colder areas)
  * Buying patterns based on time of day, day of week, holiday, major sporting events, etc.
  * Long-term trends (products coming into and falling out of favor, products being introduced and discontinued)
  * The affect of advertising, word-of-mouth, in-store promotions, etc.
  * The affect of regional and in-store stockouts.

  
