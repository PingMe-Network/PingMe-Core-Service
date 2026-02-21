package org.ping_me.service.mail;

import org.ping_me.dto.request.mail.GetOtpRequest;
import org.ping_me.dto.request.mail.OtpVerificationRequest;
import org.ping_me.dto.response.mail.GetOtpResponse;
import org.ping_me.dto.response.mail.OtpVerificationResponse;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 18/01/2026, Sunday
 **/
public interface OtpService {
    GetOtpResponse sendOtp(GetOtpRequest request);

    OtpVerificationResponse verifyOtp(OtpVerificationRequest request);

    boolean checkAdminIsVerified();
}
