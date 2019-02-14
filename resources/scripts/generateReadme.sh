#!/usr/bin/env bash
#
# somple script to glue all steps help in a single README.md file
#
#

if [ $# -lt 1 ]; then
  echo "usage: ${0} folder"
  exit 1
fi

FOLDER="${1}"
README="${FOLDER}/README.md"
echo "FOLDER=${1}"
echo "README=${FOLDER}/README.md"

echo "# Steps Documentation" > ${README}
for i in ${FOLDER}/*.txt
do
  echo "Procesing $i"
  step=$(basename $i .txt)
  echo "## ${step}" >>  ${README}
  cat $i >>  ${README}
  echo "" >> ${README}
done
