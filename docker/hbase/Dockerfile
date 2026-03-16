FROM eclipse-temurin:17-jdk

ENV HBASE_VERSION=2.6.1
ENV HBASE_HOME=/opt/hbase
ENV PATH=$HBASE_HOME/bin:$PATH

# Download and install HBase
RUN apt-get update && apt-get install -y --no-install-recommends wget netcat-openbsd && \
    wget -q "https://archive.apache.org/dist/hbase/${HBASE_VERSION}/hbase-${HBASE_VERSION}-bin.tar.gz" -O /tmp/hbase.tar.gz && \
    mkdir -p /opt && \
    tar -xzf /tmp/hbase.tar.gz -C /opt && \
    mv /opt/hbase-${HBASE_VERSION} ${HBASE_HOME} && \
    rm /tmp/hbase.tar.gz && \
    apt-get remove -y wget && apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Configure standalone mode
RUN echo '<?xml version="1.0"?>\n\
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>\n\
<configuration>\n\
  <property><name>hbase.cluster.distributed</name><value>false</value></property>\n\
  <property><name>hbase.zookeeper.property.dataDir</name><value>/data/zookeeper</value></property>\n\
  <property><name>hbase.tmp.dir</name><value>/data/hbase</value></property>\n\
  <property><name>hbase.unsafe.stream.capability.enforce</name><value>false</value></property>\n\
  <property><name>hbase.zookeeper.property.clientPort</name><value>2181</value></property>\n\
  <property><name>hbase.master.init.timeout.milliseconds</name><value>600000</value></property>\n\
</configuration>' > ${HBASE_HOME}/conf/hbase-site.xml

# Set JAVA_HOME for HBase
RUN echo "export JAVA_HOME=${JAVA_HOME}" >> ${HBASE_HOME}/conf/hbase-env.sh

EXPOSE 2181 16010 16020 16030

VOLUME ["/data"]

CMD ["/opt/hbase/bin/hbase", "master", "start"]
