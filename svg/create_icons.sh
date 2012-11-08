#!/bin/bash
for file in *.svg
do
  echo $file
  convert -transparent white -fuzz 50% -background none $file -geometry 36x36 ../res/drawable-ldpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry 48x48 ../res/drawable-mdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry 72x72 ../res/drawable-hdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry 92x92 ../res/drawable-xhdpi/${file%.*}.png
done
