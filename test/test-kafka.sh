#! /bin/bash

if ! kubectl exec kafka-0 -- bash bin/kafka-topics.sh --list --bootstrap-server kafka:9092; then
    echo "Error: kafka-topics.sh --list failed"
    exit 1
fi
