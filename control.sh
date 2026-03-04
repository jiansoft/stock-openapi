#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE_NAME="${IMAGE_NAME:-stock-api-image}"
CONTAINER_NAME="${CONTAINER_NAME:-stock_api}"
HOST_PORT="${HOST_PORT:-7000}"
LOG_DIR="${LOG_DIR:-${SCRIPT_DIR}/logs}"

resolve_jar_file() {
  if [ -f "${SCRIPT_DIR}/stock_api.jar" ]; then
    echo "stock_api.jar"
    return
  fi

  if [ -f "${SCRIPT_DIR}/build/libs/stock_api.jar" ]; then
    echo "build/libs/stock_api.jar"
    return
  fi

  echo ""
}

docker_build() {
  local jar_file
  jar_file="$(resolve_jar_file)"

  if [ -z "${jar_file}" ]; then
    echo "找不到 stock_api.jar，請先放在專案根目錄或 build/libs/stock_api.jar。" >&2
    exit 1
  fi

  docker build \
    --build-arg "JAR_FILE=${jar_file}" \
    -t "${IMAGE_NAME}" \
    "${SCRIPT_DIR}"

  docker system prune -f
}

docker_stop() {
  if docker ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
    docker rm -f "${CONTAINER_NAME}" >/dev/null
  else
    echo "容器 ${CONTAINER_NAME} 不存在，略過停止作業。"
  fi
}

docker_start() {
  if docker ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
    echo "容器 ${CONTAINER_NAME} 已存在，請先執行 docker_stop 或 docker_restart。" >&2
    exit 1
  fi

  mkdir -p "${LOG_DIR}"

  local env_args=()
  if [ -f "${SCRIPT_DIR}/.env" ]; then
    env_args=(--env-file "${SCRIPT_DIR}/.env")
  fi

  docker run \
    --name "${CONTAINER_NAME}" \
    -v "${LOG_DIR}:/app/logs:rw" \
    -p "${HOST_PORT}:7000" \
    "${env_args[@]}" \
    -d \
    "${IMAGE_NAME}"

  docker ps --filter "name=${CONTAINER_NAME}"
}

docker_restart() {
  docker_stop
  sleep 1
  docker_start
}

help() {
  echo "$0 docker_build|docker_stop|docker_start|docker_restart"
  echo "可選環境變數：IMAGE_NAME、CONTAINER_NAME、HOST_PORT、LOG_DIR"
}

case "${1:-}" in
  docker_build)
    docker_build
    ;;
  docker_stop)
    docker_stop
    ;;
  docker_start)
    docker_start
    ;;
  docker_restart)
    docker_restart
    ;;
  *)
    help
    ;;
esac
