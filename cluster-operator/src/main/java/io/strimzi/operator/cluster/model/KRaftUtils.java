/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model;

import io.strimzi.api.kafka.model.kafka.KafkaSpec;
import io.strimzi.operator.common.model.InvalidResourceException;
import org.apache.kafka.server.common.MetadataVersion;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared methods for working with KRaft
 */
public class KRaftUtils {
    /**
     * In KRaft mode, multiple features are currently not supported. This method validates the Kafka CR for the
     * unsupported features and if they are used, throws an InvalidResourceException exception.
     *
     * @param kafkaSpec   The .spec section of the Kafka CR which should be checked
     */
    public static void validateKafkaCrForKRaft(KafkaSpec kafkaSpec)   {
        Set<String> errors = new HashSet<>(0);

        if (kafkaSpec == null)  {
            errors.add("The .spec section of the Kafka custom resource is missing");
        }

        if (!errors.isEmpty())  {
            throw new InvalidResourceException("Kafka configuration is not valid: " + errors);
        }
    }

    /**
     * Validates the metadata version
     *
     * @param metadataVersion   Metadata version that should be validated
     */
    public static void validateMetadataVersion(String metadataVersion)   {
        try {
            MetadataVersion version = MetadataVersion.fromVersionString(metadataVersion);

            // KRaft is supposed to be supported from metadata version 3.0-IV1. But only from metadata version 3.3-IV0,
            // the initial metadata version can be set using the kafka-storage.sh utility. And since most metadata
            // versions do not support downgrade, that means 3.3-IV0 is the oldest metadata version that can be used
            // with Strimzi.
            if (version.isLessThan(MetadataVersion.IBP_3_3_IV0)) {
                throw new InvalidResourceException("The oldest supported metadata version is 3.3-IV0");
            }
        } catch (IllegalArgumentException e)    {
            throw new InvalidResourceException("Metadata version " + metadataVersion + " is invalid", e);
        }
    }
}
