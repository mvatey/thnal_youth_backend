package org.example.tnal_youth_backend.authentication.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PhoneNumberUtil {

    private PhoneNumberUtil() {
    }

    /**
     * Convert to the format stored in the database.
     *
     * +855978974661 -> 0978974661
     * 855978974661  -> 0978974661
     * 0978974661    -> 0978974661
     */
    public static String toDatabaseFormat(String phone) {

        String cleaned = clean(phone);

        if (cleaned.startsWith("+855")) {
            return "0" + cleaned.substring(4);
        }

        if (cleaned.startsWith("855")) {
            return "0" + cleaned.substring(3);
        }

        if (cleaned.startsWith("0")) {
            return cleaned;
        }

        throw invalidPhone();
    }

    /**
     * Convert to the format required by Plasgate.
     *
     * 0978974661    -> 855978974661
     * +855978974661 -> 855978974661
     * 855978974661  -> 855978974661
     */
    public static String toSmsFormat(String phone) {

        String cleaned = clean(phone);

        if (cleaned.startsWith("+855")) {
            return cleaned.substring(1);
        }

        if (cleaned.startsWith("855")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            return "855" + cleaned.substring(1);
        }

        throw invalidPhone();
    }

    private static String clean(String phone) {

        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number is required"
            );
        }

        return phone
                .trim()
                .replaceAll("[^0-9+]", "");
    }

    private static ResponseStatusException invalidPhone() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid Cambodian phone number"
        );
    }
}