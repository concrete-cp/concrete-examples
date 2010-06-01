#!/bin/sh

CLASSPATH=$CLASSPATH:$CSPFJ_HOME:$HOME/workspace/concrete/target/classes:$HOME/.m2/repository/commons-cli/commons-cli/1.1/commons-cli-1.1.jar:$HOME/.m2/repository/asm/asm/3.1/asm-3.1.jar


for tight in .99970 .99971 .99972 .99973 .99974 .99975 .99976 .99977 .99978 .99979 .99980 .99981 .99982 .99983 .99984 .99985 .99986 .99987 .99988 .99989 .99990 
do
	for filter in DC1 DC20 DC2
	do
		echo 20-6-8-50-$tight-$filter
		java -Xmx256M -javaagent:../../lib/logfilter.jar rb.RB -prepro cspfj.filter.$filter -parameter cdc.addConstraints=BIN -s dummy 20 6 8 50 PROPORTION $tight UNSTRUCTURED UNSTRUCTURED false false 2 0 | tee 20-6-8-50-$tight-$filter.txt
	done
done


for tight in .40 .41 .42 .43 .44 .45 .46 .47 .48 .49 .50 .51 .52 .53 .54 .55 .56 .57 .58 .59 .60 .61 .62 .63 .64 .65 .66 .67 .68 .69 .70
do
	for filter in DC1 DC20 DC2
	do
		echo 50-50-2-1225-$tight-$filter
		java -Xmx256M -javaagent:../../lib/logfilter.jar rb.RB -prepro cspfj.filter.$filter -parameter cdc.addConstraints=BIN -s dummy 50 50 2 1225 PROPORTION $tight UNSTRUCTURED UNSTRUCTURED false false 50 0 | tee 50-50-2-1225-$tight-$filter.txt
	done
done
