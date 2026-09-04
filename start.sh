#!/bin/bash
su $(id -nu 1000) -c "java -jar app.jar" &
nginx -g 'daemon off;' &
tail -f /var/log/nginx/access.log &
tail -f /var/log/nginx/error.log &
wait -n
exit $?
# TODO: Attach output from /var/log/nginx/*