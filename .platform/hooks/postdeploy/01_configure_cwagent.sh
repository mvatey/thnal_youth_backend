#!/bin/bash
set -e

/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -c file:/var/app/current/.platform/cwagent/cwagent.json \
  -s
