#!/bin/bash

source simulation.sh

ensure_files_existence
create
id=$?
upload_result $id
print_url $id