package clj.beans;

import java.time.Instant;
import java.util.Map;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

// // Uncomment to use custom object serializer
// import java.io.ByteArrayOutputStream;
// import java.io.ObjectOutputStream;


/**
 * Input Message Data Transfer Object (java bean).
 * Contains information about order placed. Transfers placed order to Kafka through KSQLDB
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public final class InputMessageDTO implements Serializable {
    public enum ChargeType {
        USAGE,
        OTF
    }

    public static class DTOSerializer implements org.apache.kafka.common.serialization.Serializer<InputMessageDTO> {
        private final ObjectMapper objectMapper;

        public DTOSerializer(){
            super();
            objectMapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            objectMapper.registerModule(module);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        @Override
        public byte[] serialize(String topic, InputMessageDTO data) {
            try {
                if (data == null){
                    System.out.println("Null received at serializing");
                    return null;
                }
                System.out.println("Serializing...");
                return objectMapper.writeValueAsBytes(data);

                // // Uncomment to use custom object serializer
                // ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                // ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
                // objectStream.writeObject(data);
                // objectStream.flush();
                // objectStream.close();
                // return byteStream.toByteArray();

            } catch (Exception e) {
                e.printStackTrace();
                throw new SerializationException("Error when serializing InputMessageDTO to byte[]");
            }
        }

        @Override
        public void close() {
        }
    }

    public static class DTODeserializer implements org.apache.kafka.common.serialization.Deserializer<InputMessageDTO> {
        private final ObjectMapper objectMapper;

        public DTODeserializer(){
            super();
            objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
        }

        @Override
        public void close() {

        }

        @Override
        public void configure(Map<String, ?> arg0, boolean arg1) {

        }

        @Override
        public InputMessageDTO deserialize(String arg0, byte[] arg1) {
            InputMessageDTO user = null;
            try {
                user = objectMapper.readValue(arg1, InputMessageDTO.class);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return user;
        }
    }



    /**
     * Although getters/setters are redundant to Domain Model as described in
     * [Anemic Domain Model Anti Pattern](https://martinfowler.com/bliki/AnemicDomainModel.html "Anemic Domain Model Anti Pattern")
     * using @Data annotation to define them.
     */

    /*
     * Not using java.concurrent.Atomic* here as the reference assignment (`=`) is itself atomic
     * See the [Java Language Specification 3ed, Section 17.7](https://docs.oracle.com/javase/specs/jls/se7/html/jls-17.html#jls-17.7 "17.7. Non-atomic Treatment of double and long").
     * This is a reason not using `long` or `double` types here.
     */
    private String id;
    private Instant createTimestamp;
    private String customerId;
    private String productId;
    private ChargeType chargeType;
    private Integer amount;
    private String unit;
}
