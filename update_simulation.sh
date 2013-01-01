#!/bin/bash

source simulation.sh

ensure_files_existence
ensure_id_passed $1
id=$1

update_config $id
upload_result $id
print_url $1