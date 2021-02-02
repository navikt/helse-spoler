package no.nav.helse.spoler

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private val log = LoggerFactory.getLogger("no.nav.helse.Spoler")

fun main() {
    val env = System.getenv()
    spol(env)
}

private fun spol(env: Map<String, String>) {
    Thread.setDefaultUncaughtExceptionHandler { _, throwable -> log.error(throwable.message, throwable) }

    listOf(
        Spol(env, "helse-rapid-v1", "spre-stonadsstatistikk-v2", LocalDateTime.of(2021, 2, 1, 20, 9)),
    ).forEach(Spol::spol)
}

class Spol(
    private val env: Map<String, String>,
    private val topic: String,
    private val groupId: String,
    private val timestamp: LocalDateTime
) {
    fun spol() {
        KafkaConsumer(consumerConfig(env, groupId), StringDeserializer(), StringDeserializer()).use { consumer ->
            consumer.partitionsFor(topic)
                .map { TopicPartition(topic, it.partition()) }
                .also {
                    consumer.assign(it)
                    while (consumer.poll(Duration.ofSeconds(1)).count() == 0) {
                        log.debug("Poll count == 0")
                    }
                }
                .map { it to timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }.toMap()
                .let { consumer.offsetsForTimes(it) }
                .mapValues { (_, offsetAndTimestamp) -> offsetAndTimestamp.offset() }
                .onEach { (topicPartition, offset) -> log.info("For groupId $groupId, setter offset for partisjon $topicPartition til $offset") }
                .forEach { (topicPartition, offset) -> consumer.seek(topicPartition, offset) }

            consumer.commitSync()
        }
    }
}

internal fun consumerConfig(env: Map<String, String>, groupId: String) = Properties().apply {
    putAll(kafkaBaseConfig(env))
    put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-spoler")
    put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
}

private fun kafkaBaseConfig(env: Map<String, String>) = Properties().apply {
    val username = "/var/run/secrets/nais.io/service_user/username".readFile()
    val password = "/var/run/secrets/nais.io/service_user/password".readFile()
    val truststore = env["NAV_TRUSTSTORE_PATH"]
    val truststorePassword = env["NAV_TRUSTSTORE_PASSWORD"]

    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.getValue("KAFKA_BOOTSTRAP_SERVERS"))
    put(SaslConfigs.SASL_MECHANISM, "PLAIN")
    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")

    put(
        SaslConfigs.SASL_JAAS_CONFIG,
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
    )

    if (truststore != null) {
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(truststore).absolutePath)
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword)

    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)

