# inspector

## How to Use

1.  get the distribution:inspector-distribute-1.0-SNAPSHOT-bin.zip
you can download the recent distribution or compile from the source code.

2. unzip 
    unzip inspector-distribute-1.0-SNAPSHOT-bin.zip

3. find the process id of your JVM
    jps
or
    ps -ef|grep java

4. replace the classes
    ./inspector.sh /home/xxx.class yyy
