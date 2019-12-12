# Data file Uploader - AWS S3 Version


# Prechecks
: "${DATA_DIR:=/tmp/pos_data}"
: "${PREFIX:=scans}"
: "${S3_LOCATION=s3://incorta-datalake}"

if [ ! -d $DATA_DIR ]; then
	echo "Please ensure that $DATA_DIR is writable."
  exit 1
fi
if [ ! -d $DATA_DIR/stage ]; then
  echo "creating stage directory"
  mkdir $DATA_DIR/stage
fi
if [ ! -d $DATA_DIR/processed ]; then
  echo "creating processed directory"
  mkdir $DATA_DIR/processed
fi

# Check that AWS CLI is installed
if [ ! -d ~/.aws ]; then
  echo "AWS Command Line Interface not installed or configured."
  echo " Try:  $ pip3 install awscli --upgrade --user"
  echo " Then: $ aws configure"
  exit 1
fi
#set -e

# Create files until told to stop
while true
do
  # Move group of files to staging area (in the future may be more than one uploader)
  mv -f $DATA_DIR/${PREFIX}_*.csv $DATA_DIR/stage 2> /dev/null
  if [ $? -eq 0 ]; then
    # upload each file in the staging group to S3 and then move to processed
    for file in $DATA_DIR/stage/${PREFIX}_*.csv
    do
      echo "Uploading " $file

      # Extract the timestamp from the file name and get the Year / Month / Day
      # Pattern must be prefix_timestamp.csv
      ts=$(basename -s .csv ${file} | cut -d _ -f 2)
      DATEPATH=$(date -r $(($ts / 1000)) +%Y/%m/%d)
      aws s3 cp "$file" "${S3_LOCATION}"/${PREFIX}/"${DATEPATH}"/
      if [ $? -ne 0 ]; then
        echo "Exiting due to upload error"
        exit;
      fi
      mv $file $DATA_DIR/processed
    done # with batch
  fi # if files present
  sleep 10
done
