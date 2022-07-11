{{- define "jdbc.url" -}}jdbc:mysql://mysql-service.{{ .Release.Namespace }}.svc:3306/trino_proxy?useSSL=false{{- end -}}

{{- define "redisConnection.host" -}}{{ .Release.Namespace }}-redis-master.{{ .Release.Namespace }}.svc{{- end -}}
