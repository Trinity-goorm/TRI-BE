package com.trinity.ctc.global.util.formatter;

public class PhoneNumberUtil {
    public static String formatPhoneNumber(String phone) {
        // 9자리) ex. 031705777 -> 031-705-777
        if (phone.matches("^(02|0[3-9]\\d)\\d{7}$")) {
            return phone.replaceAll("^(02|0[3-9]\\d)(\\d{3})(\\d{4})$", "$1-$2-$3");
        }
        // 10자리)
        // - 서울) ex. 0212345678 -> 02-1234-5678
        if (phone.matches("^(02)\\d{8}$")) {
            return phone.replaceAll("^(02)(\\d{4})(\\d{4})$", "$1-$2-$3");
        }
        // - 그 외 지역 번호) ex. 0315170105 -> 031-517-0105
        if (phone.matches("^(0[3-9]\\d)\\d{7}$")) {
            return phone.replaceAll("^(0[3-9]\\d)(\\d{3})(\\d{4})$", "$1-$2-$3");
        }
        // 11자리) ex. 05071359968 -> 050-7135-9968
        if (phone.matches("^(0[3-9]\\d)\\d{8}$")) {
            return phone.replaceAll("^(0[3-9]\\d)(\\d{4})(\\d{4})$", "$1-$2-$3");
        }
        // 12자리)
        // - 기업용 또는 특수 번호 (1588-123456 또는 0505-1234-5678)
        if (phone.matches("^(0\\d{2,3})(\\d{4})(\\d{4})$")) {
            return phone.replaceAll("^(0\\d{2,3})(\\d{4})(\\d{4})$", "$1-$2-$3");
        }
        if (phone.matches("^(0\\d{3})(\\d{3})(\\d{6})$")) {
            return phone.replaceAll("^(0\\d{3})(\\d{3})(\\d{6})$", "$1-$2-$3");
        }
        // 해당 패턴에 맞지 않으면 원본 반환
        return phone;
    }
}
