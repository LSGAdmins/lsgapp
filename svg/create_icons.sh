#!/bin/bash
for file in *.svg
do
  medium="48x48"
  large="72x72"
  xlarge="96x96"
  xlarge="96x96"
  xxlarge="144x144"
  echo $file
  case "$file" in
    *large* )
      medium="72x72"
      large="96x96"
      xlarge="144x144"
      xxlarge="216x216";;
  esac

  convert -transparent white -fuzz 50% -background none $file -geometry $medium ../res/drawable-mdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry $large  ../res/drawable-hdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry $xlarge ../res/drawable-xhdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry $xxlarge  ../res/drawable-xxhdpi/${file%.*}.png
done
