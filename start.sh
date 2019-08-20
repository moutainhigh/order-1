#!/usr/bin/env bash
java -javaagent:/agent/skywalking-agent.jar -Dskywalking.agent.service_name=order -Dskywalking.collector.backend_service=skywalking-oap:11800 -jar /app.jar