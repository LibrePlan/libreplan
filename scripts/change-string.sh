#!/bin/bash
# author: J. Baten
# date: 2014-03-21

echo "Script to search for a string, find files containing it, and asks they can be changed"

read -p "Enter string to search for : " inputstring
find . -type f \( ! -iname "*.class" \) -exec grep -H "$inputstring" {} \;
echo "Done".
read -p "Do you want to replace all these strings (y/n) : " answer
if [[ ${answer,,} = 'y' ]]
then
  read -p "Enter replacement string : " newstring
  find .  -type f \( ! -iname "*.class" \) -exec grep -l "$inputstring" {} \; | xargs sed -i -e "s/${inputstring}/${newstring}/g"
  echo "Done".
fi
