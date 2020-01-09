FROM cassandra:3.11.2

ENV LOCAL_JMX no
COPY jmxremote.password /etc/cassandra/jmxremote.password
COPY jmxremote.access /etc/cassandra/jmxremote.access
RUN chown cassandra:cassandra /etc/cassandra/jmxremote.*
RUN chmod 400 /etc/cassandra/jmxremote.*

RUN sed -i 's/#JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.access.file=\/etc\/cassandra\/jmxremote.access"/JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote.access.file=\/etc\/cassandra\/jmxremote.access"/g' /etc/cassandra/cassandra-env.sh
RUN sed -i 's/# JVM_OPTS="$JVM_OPTS -Djava.rmi.server.hostname=<public name>"/JVM_OPTS="$JVM_OPTS -Djava.rmi.server.hostname=${JMX_HOST:-localhost}"/g' /etc/cassandra/cassandra-env.sh
