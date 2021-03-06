#!/bin/bash
dir=$2
dir=${dir%*/}
START=$(date +%s)
echo -n "[${dir}]"   
echo -n "a"
rm -f ${dir}/channel_*
rm -f ${dir}/*.arff
rm -f ${dir}/*_quantified.csv
rm -f ${dir}/foreground*
$JAVA.SplitRGB ${dir}/*rgb*

#apply filter
echo -n "b"
$JAVA.RGB2HSV ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
$JAVA.RGB2XYZ ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8
$JAVA.RGB2LAB ${dir}/*rgb_r.tif ${dir}/*rgb_g.tif ${dir}/*rgb_b.tif 8

echo -n "c"
for img in ${dir}/channel*;
do
	$JAVA.FILTER ${img} ${img} 3 3 BLUR
	$JAVA.FILTER ${img} ${img} 4 4 MEDIAN
done

echo -n "d"
$JAVA.ArffFromImageFileGenerator 2 "${dir}"

echo -n "e"
$SPLITCMD "${dir}" "${dir}_2.arff" "true" "fgbg" "150000000"

echo -n "f"
#create foreground png
cp "${dir}/channel_rgb_r.tif" "${dir}/fgbgresult.tif"
$JAVA.ApplyClass0ToImage "${dir}/fgbgresult.tif"
rm "${dir}/fgbgresult.tif"

echo -n "g"
cp "${dir}/foreground.png" "${dir}/foreground_cluster.png"
$JAVA.Quantify 0 ${dir}/foreground
#rm "${dir}/foreground_cluster.png"
cat ${dir}/*_quantified.csv >> all_prediction_results.csv

if [ -f ${dir}/mask_2.png ];
then
	echo -n "h"
	$JAVA.CreateDiffImage ${dir}/mask_2.png ${dir}/foreground.png ${dir}/foreground_diff.png
fi

rm -f ${dir}/channel_*
rm -f ${dir}/*.arff

END=$(date +%s)
DIFF=$(echo "$END - $START" | bc)
echo -n " (finished ${dir} in ${DIFF} sec.) "
