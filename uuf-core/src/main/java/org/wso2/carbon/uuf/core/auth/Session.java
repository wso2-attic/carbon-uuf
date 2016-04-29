package org.wso2.carbon.uuf.core.auth;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Session implements Serializable{

    private static final SessionIdGenerator sessionIdGenerator = new SessionIdGenerator();

    private final String sessionId;
    private final User user;

    public Session(User user) {
        this.sessionId = sessionIdGenerator.generateId();
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    private static class SessionIdGenerator {
        /**
         * The default message digest algorithm.
         */
        private static final String DEFAULT_MESSAGE_DIGEST_ALGORITHM = "MD5";
        private static final int DEFAULT_SESSION_ID_LENGTH = 16;
        private final Random random;
        private final MessageDigest messageDigest;

        public SessionIdGenerator() {
            this(DEFAULT_MESSAGE_DIGEST_ALGORITHM);
        }

        public SessionIdGenerator(String algorithm) {
            try {
                this.messageDigest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("No algorithms for session ID generator", ex);
            }

            byte[] randomBytes = new byte[32];
            ThreadLocalRandom.current().nextBytes(randomBytes);
            char[] entropy = Base64.getEncoder().encodeToString(randomBytes).toCharArray();

            long seed = System.currentTimeMillis();
            for (int i = 0; i < entropy.length; i++) {
                long update = ((byte) entropy[i]) << ((i % 8) * 8);
                seed ^= update;
            }

            this.random = new SecureRandom();
            this.random.setSeed(seed);
        }

        public String generateId() {
            return generateId(DEFAULT_SESSION_ID_LENGTH);
        }

        public synchronized String generateId(int sessionIdLength) {
            byte randomBytes[] = new byte[16];
            // Render the result as a String of hexadecimal digits
            StringBuilder buffer = new StringBuilder();

            int resultLenBytes = 0;

            while (resultLenBytes < sessionIdLength) {
                random.nextBytes(randomBytes);
                randomBytes = messageDigest.digest(randomBytes);
                for (int j = 0; j < randomBytes.length && resultLenBytes < sessionIdLength; j++) {
                    byte b1 = (byte) ((randomBytes[j] & 0xf0) >> 4);
                    byte b2 = (byte) (randomBytes[j] & 0x0f);
                    if (b1 < 10) {
                        buffer.append((char) ('0' + b1));
                    } else {
                        buffer.append((char) ('A' + (b1 - 10)));
                    }
                    if (b2 < 10) {
                        buffer.append((char) ('0' + b2));
                    } else {
                        buffer.append((char) ('A' + (b2 - 10)));
                    }
                    resultLenBytes++;
                }
            }
            return buffer.toString();
        }
    }
}
