##!/usr/bin/env bash
#
#
#   #* Licensed to the Apache Software Foundation (ASF) under one or more
#    #* contributor license agreements.  See the NOTICE file distributed with
#    #* this work for additional information regarding copyright ownership.
#    #* The ASF licenses this file to You under the Apache License, Version 2.0
#    #* (the "License"); you may not use this file except in compliance with
#    #* the License.  You may obtain a copy of the License at
#    #*
#    #*    http://www.apache.org/licenses/LICENSE-2.0
#    #*
#    #* Unless required by applicable law or agreed to in writing, software
#    #* distributed under the License is distributed on an "AS IS" BASIS,
#    #* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    #* See the License for the specific language governing permissions and
#   # * limitations under the License.
#
    JAR_FILE=""
    if [ "$1" == "1" ]; then
        JAR_FILE="TopSpeedWindowingWithCheckpointing.jar"
    else
      JAR_FILE="TopSpeedWindowing.jar"
    fi
    pod=$(kubectl get pods  | head -n2 | awk '{print $1;}' | sed -n '2 p')
    name="checkpointing-load-test"
     if [[ "$pod" == *"$name"* ]]; then
        kubectl delete pod $pod
    fi
    ./src/main/java/org/apache/flink/benchmark/checkpointing/kubernetes-session.sh -Dkubernetes.cluster-id=checkpointing-load-test
    echo "creating pod.."
    sleep 90
    echo "starting to copy jar.."
    pod=$(kubectl get pods  | head -n2 | awk '{print $1;}' | sed -n '2 p')
    kubectl cp src/main/java/org/apache/flink/benchmark/checkpointing/kubernetes/lib/$JAR_FILE default/$pod:/opt/flink/
    (kubectl exec -it pod/$pod -- bash -c "./bin/flink run --target kubernetes-session -Dkubernetes.cluster-id=checkpointing-load-test $JAR_FILE")  & sleep 15 ; kill $!
    echo "Job running.."
    echo "Creating tunnel to Prometheus.."
    (minikube service prometheus-server-np --url | tee src/main/java/org/apache/flink/benchmark/checkpointing/prom_ip.txt) &
    sleep 5
    echo "Waiting for metrics to be available.."
    sleep 90
    echo "Printing kubernetes server request load.."
    ip=$(head -n 1 src/main/java/org/apache/flink/benchmark/checkpointing/prom_ip.txt)
    rm -r src/main/java/org/apache/flink/benchmark/checkpointing/prom_ip.txt
    while true
    do
      sleep 2
      ./src/main/java/org/apache/flink/benchmark/checkpointing/kubernetes/lib/promql --host "$ip" 'sum(rate(apiserver_request_total[5m])) by (job)' | sed -n '2 p'
    done