package cc.datafabric.exchange.lib


//todo разобраться как это сделать правильно
// как минимум убрать отсюда константы которые не используются в библиотеке непосредственно
// + копирую файл из adapter-lib в exchange-lib т.к. не удалось подключить adapter-lib в test-extensions-consumer
object ConfigNames {





    const val  traceEnabled = "adp_trace_enabled"
    const val  traceDataEnabled = "adp_trace_data_enabled"
    const val  massiveTraceEnabled = "adp_massive_trace_enabled"
    const val  timerTraceEnabled = "adp_timer_trace_enabled"
    const val  mongoUri = "adp_mongo_uri"
    const val  mongoExchangeDb = "adp_mongo_exchange_db"
    const val  exchangeAgentIntervalMs = "adp_exchange_agent_interval_ms"
    const val  messageLoggingEnabled = "adp_message_logging_enabled"
    const val  platformApiUrl = "adp_platform_api_url"
    const val  keycloakUrl = "adp_keycloak_url"
    const val  keycloakClientName = "adp_keycloak_client_name"
    const val  keycloakClientPassword = "adp_keycloak_client_password"
    const val  keycloakUserName = "adp_keycloak_user_name"
    const val  keycloakUserPassword = "adp_keycloak_user_password"
    const val  profileVersionIri = "adp_profile_version_iri"
    const val  rabbitQueue = "adp_rabbit_queue"
    const val  rabbitUri = "adp_rabbit_uri"
    const val  rabbitExchange = "adp_rabbit_exchange"
    const val  skConsumingIntervalMs = "adp_sk_consuming_interval_ms"
    const val  skVersion = "adp_sk_version"
    const val  skModelUid = "adp_sk_model_uid"
    const val  skEndpoint = "adp_sk_endpoint"
    const val  skDomain = "adp_sk_domain"
    const val  skUser = "adp_sk_user"
    const val  skPassword = "adp_sk_password"
    const val  skAuthType = "adp_sk_auth_type"
    const val  skAuthEndpoint = "adp_sk_auth_endpoint"
    const val  skDmNamespace = "adp_sk_dm_namespace"
    const val  exchangeConfigurationDir = "adp_exchange_configuration_dir"
    const val  pipelineLoggingEnabled = "adp_pipeline_logging_enabled"
    const val  exchangeSettingsPath = "adp_exchange_settings_path"
    const val  exchangeBuilderPath = "adp_exchange_builder_path"
    const val  processorName = "adp_processor_name"
    const val  skUrl = "adp_sk_url"
    const val mailSender = "adp_mail_sender"
    const val mailLogin = "adp_mail_login"
    const val mailPassword = "adp_mail_password"
    const val mailSmtpHost = "adp_mail_smtp_host"
    const val mailSmtpPort = "adp_mail_smtp_port"
    const val mailUseSsl = "adp_mail_use_ssl"
    const val uiUrl = "adp_ui_url"
    const val apiUser = "adp_api_user"
    const val apiPassword = "adp_api_password"
    const val apiUrl = "adp_api_url"


}