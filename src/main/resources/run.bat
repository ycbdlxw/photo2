taskkill /F /IM "photo.jar"
start /B /D  java -jar -Xms2G -Xmx2G photo.jar --spring.config.location=application.yml > photo.out 2>&1