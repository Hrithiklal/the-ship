FROM openjdk:11-jre-slim

WORKDIR /app

COPY OffshoreProxy.java .
COPY compile_and_run.sh .

RUN apt-get update && \
    apt-get install -y gcc && \
    apt-get clean && \
    chmod +x compile_and_run.sh

CMD ["./compile_and_run.sh"]