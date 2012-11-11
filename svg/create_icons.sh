#!/bin/bash
for file in *.svg
do
  small="36x36"
  medium="48x48"
  large="72x72"
  xlarge="96x96"
  echo $file
  case "$file" in
    *large* )
      small="48x48"
      medium="72x72"
      large="96x96"
      xlarge="144x144";;
  esac
  convert -transparent white -fuzz 50% -background none $file -geometry $small  ../res/drawable-ldpi/${file%.*}.png

  convert -transparent white -fuzz 50% -background none $file -geometry $medium ../res/drawable-mdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry $large  ../res/drawable-hdpi/${file%.*}.png
  convert -transparent white -fuzz 50% -background none $file -geometry $xlarge ../res/drawable-xhdpi/${file%.*}.png
done
