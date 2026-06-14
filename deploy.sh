#!/usr/bin/env bash
set -Eeuo pipefail

APP_NAME="${APP_NAME:-portfell-admin}"
APP_USER="${APP_USER:-portfell-admin}"
APP_DIR="${APP_DIR:-/var/www/gatto/portfell-admin}"
UPLOAD_DIR="${UPLOAD_DIR:-/var/www/gatto/portfell}"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

if [[ "${EUID}" -ne 0 ]]; then
	echo "Run as root: sudo ./deploy.sh"
	exit 1
fi

if ! command -v java >/dev/null 2>&1; then
	echo "Java 21 is required."
	exit 1
fi

JAVA_MAJOR="$(java -version 2>&1 | awk -F '[".]' '/version/ { print $2; exit }')"
if [[ "${JAVA_MAJOR}" -lt 21 ]]; then
	echo "Java 21 or newer is required. Found Java ${JAVA_MAJOR}."
	exit 1
fi

if ! id "${APP_USER}" >/dev/null 2>&1; then
	useradd --system --home-dir "${APP_DIR}" --shell /usr/sbin/nologin "${APP_USER}"
fi

cd "${PROJECT_DIR}"
./gradlew clean test bootJar
JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)"

if [[ -z "${JAR_FILE}" ]]; then
	echo "Built JAR was not found."
	exit 1
fi

install -d -o "${APP_USER}" -g "${APP_USER}" -m 0750 "${APP_DIR}"
install -d -o "${APP_USER}" -g "${APP_USER}" -m 0755 "${UPLOAD_DIR}"
install -o "${APP_USER}" -g "${APP_USER}" -m 0640 "${JAR_FILE}" "${APP_DIR}/${APP_NAME}.jar"

if [[ ! -f "${APP_DIR}/.env" ]]; then
	if [[ ! -f "${PROJECT_DIR}/.env" ]]; then
		echo "Create ${PROJECT_DIR}/.env from .env.example before the first deployment."
		exit 1
	fi
	install -o "${APP_USER}" -g "${APP_USER}" -m 0600 "${PROJECT_DIR}/.env" "${APP_DIR}/.env"
fi

cat > "${SERVICE_FILE}" <<EOF
[Unit]
Description=Portfell HTML admin
After=network.target

[Service]
Type=simple
User=${APP_USER}
Group=${APP_USER}
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -jar ${APP_DIR}/${APP_NAME}.jar
Restart=on-failure
RestartSec=5
SuccessExitStatus=143

NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=${APP_DIR} ${UPLOAD_DIR}

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable --now "${APP_NAME}"
systemctl restart "${APP_NAME}"

echo "Deployed ${APP_NAME} to ${APP_DIR}"
echo "Service status: systemctl status ${APP_NAME}"
echo "Logs: journalctl -u ${APP_NAME} -f"
