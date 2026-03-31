#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

backend_pid=
frontend_pid=

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "缺少必要命令: $1" >&2
    exit 1
  fi
}

cleanup() {
  trap - INT TERM EXIT

  if [ -n "$frontend_pid" ] && kill -0 "$frontend_pid" 2>/dev/null; then
    kill "$frontend_pid" 2>/dev/null || true
    wait "$frontend_pid" 2>/dev/null || true
  fi

  if [ -n "$backend_pid" ] && kill -0 "$backend_pid" 2>/dev/null; then
    kill "$backend_pid" 2>/dev/null || true
    wait "$backend_pid" 2>/dev/null || true
  fi
}

if [ ! -d "$BACKEND_DIR" ] || [ ! -d "$FRONTEND_DIR" ]; then
  echo "未找到 backend 或 frontend 目录，请确认脚本位于项目根目录。" >&2
  exit 1
fi

require_command mvn
require_command npm

trap cleanup INT TERM EXIT

echo "启动后端: http://localhost:8080"
(
  cd "$BACKEND_DIR"
  exec mvn spring-boot:run
) &
backend_pid=$!

echo "启动前端: http://localhost:5173"
(
  cd "$FRONTEND_DIR"
  exec npm run dev
) &
frontend_pid=$!

echo "联调环境已启动，按 Ctrl+C 可同时关闭前后端。"

while :; do
  if ! kill -0 "$backend_pid" 2>/dev/null; then
    wait "$backend_pid" || backend_status=$?
    echo "后端进程已退出。" >&2
    exit "${backend_status:-1}"
  fi

  if ! kill -0 "$frontend_pid" 2>/dev/null; then
    wait "$frontend_pid" || frontend_status=$?
    echo "前端进程已退出。" >&2
    exit "${frontend_status:-1}"
  fi

  sleep 2
done