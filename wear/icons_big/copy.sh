#!/bin/sh
convert $1 -resize 144x144 ../src/main/res/drawable-xxhdpi/$1
convert $1 -resize 96x96 ../src/main/res/drawable-xhdpi/$1
convert $1 -resize 72x72 ../src/main/res/drawable-hdpi/$1
convert $1 -resize 48x48 ../src/main/res/drawable-mdpi/$1