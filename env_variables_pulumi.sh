#!/bin/bash

export HAS_RUN="Had run"

export VPC_CIDR_BLOCK="null"
#"10.1.0.0/16"
export VPC_INSTANCE_TENANCY="null"
#"default"
export VPC_TAG_NAME="null"
#"defaultVpc"
export IG_TAG_NAME="thename"
#"defaultGW"
export RT_ROUTE_CIDR_BLOCK="null"
#"0.0.0.0/0"
export RT_PUBLIC_ROUTE_TABLE_NAME="null"
#"default_pub_route_table"
export RT_PRIVATE_ROUTE_TABLE_NAME="null"
#"default_private_route_table"
export PUBLIC_SUBNET_CIDER_PREFIX="null"
#"10.1."
export PUBLIC_SUBNET_CIDER_START="4"
#"0"
export PUBLIC_SUBNET_TAG_NAME_PREFIX="null"
#"public_subnet"
export NUM_OF_PUBLIC_SUBNETS="null"
#"3"
export PRIV_SUBNET_CIDER_PREFIX="null"
#"10.1."
export PRIV_SUBNET_CIDER_START="null"
#"100"
export PRIV_SUBNET_TAG_NAME_PREFIX="null"
"private_subnet"
export NUM_OF_PUBLIC_SUBNETS_PRIV="null"
#"3"

# to change aws account to demo:
# export AWS_PROFILE=demo

