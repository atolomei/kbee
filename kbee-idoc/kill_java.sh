PIDS=$(ps -ef | grep java | grep -v grep | awk {'print $2'}) 
echo ${PIDS} 
for i in ${PIDS} 
do 
	/bin/kill -9 $i 
	echo $i 
done 
exit 0 
 
 
# --- 
# Test
# Created: