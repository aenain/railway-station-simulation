#!/bin/bash

# Skrypt do tworzenia rekordu symulacji w webaplikacji na HEROKU.
# Rekord populuje parametrami z pliku $CONFIG_FILE.
# Następnie kompresuje rezultat działania programu $OUTPUT_FILE
# i zapisuje go na HEROKU jako rezultat stworzonej chwilę wcześniej symulacji.

# sciezki na HEROKU
SIMULATION_URL='http://railway-station.herokuapp.com'
API_ENDPOINT="$SIMULATION_URL/api/simulations"

# sciezki lokalne
CONFIG_FILE='config.json'
OUTPUT_FILE='output.json'

# pomocniczne stale
ERROR=-1


# tworzy nowy obiekt symulacji i zwraca jego ID lub error w przypadku błędu
function create {
  # utworzenie rekordu symulacji
  echo "----- Create new simulation from $CONFIG_FILE"
  response=$(curl -H "Content-Type: application/json" -X POST -d @$CONFIG_FILE $API_ENDPOINT)

  if [[ $response =~ 'error' ]]; then
    echo -e "\n[ERROR]\nSomething went wrong:\n$response\n"
    exit
  fi

  # wyekstrahowanie id nowego rekordu
  number='[1-9][0-9]*'
  if [[ ! $response =~ $number ]]; then
    echo -e "\n[ERROR]\nCan't extract id from the response:\n$response\n"
    exit
  else
    return ${BASH_REMATCH[0]}
  fi
}


# aktualizacja parametrow symulacji
# param: simulation_id
function update_config {
  ensure_id_passed $1
  id=$1

  echo "----- Update simulation's parameters from $CONFIG_FILE"
  response=$(curl -H "Content-Type: application/json" -X PUT -d @$CONFIG_FILE $API_ENDPOINT/$id)

  if [[ $response =~ 'error' ]]; then
    echo -e "\n[ERROR]\nSomething went wrong:\n$response\n"
    exit
  fi
}

# uploaduje rezultat symulacji
# param: simulation_id
function upload_result {
  ensure_id_passed $1
  id=$1

  # kompresja pliku
  echo "----- Compress $OUTPUT_FILE into $OUTPUT_FILE.gz"
  gzip -c -9 $OUTPUT_FILE > $OUTPUT_FILE.gz


  # upload skompresowanego rezultatu
  echo '----- Upload compressed result of the simulation'
  response=$(curl -i -X PUT -F "result=@$OUTPUT_FILE.gz" $API_ENDPOINT/$id/upload_gzip)

  ok_status='HTTP/[0-9].[0-9] 2[0-9]{2}'
  if [[ ! $response =~ $ok_status ]]; then
    echo -e "\n[ERROR]\nNot everything went as expected:\n$response\n"
    exit
  fi
}

function ensure_id_passed {
  number='[1-9][0-9]*'
  if [[ ! $1 =~ $number ]]; then
    echo -e "\n[ERROR]\nID not passed!\n"
    exit
  fi
}

# wypisz url
# param: simulation_id
function print_url {
  ensure_id_passed $1
  id=$1
  echo -e "\n[SUCCESS]\nSee simulation at: $SIMULATION_URL/simulations/$id"
}


# sprawdzenie, czy potrzebne pliki istnieja
function ensure_files_existence {
  if [[ ! -f $CONFIG_FILE ]]; then
    echo -e "\n[ERROR]\nConfig file ($CONFIG_FILE) is missing."
    exit
  fi
  if [[ ! -f $OUTPUT_FILE ]]; then
    echo -e "\n[ERROR]\nOutput file ($OUTPUT_FILE) is missing."
    exit
  fi
}
