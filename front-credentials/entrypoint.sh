#!/bin/sh

# Substituir variáveis de ambiente nos arquivos JavaScript
envsubst < /usr/share/nginx/html/config.js.template > /usr/share/nginx/html/config.js

# Iniciar o nginx
exec "$@"
