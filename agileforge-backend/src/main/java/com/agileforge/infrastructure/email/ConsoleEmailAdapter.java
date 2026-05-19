package com.agileforge.infrastructure.email;

import com.agileforge.domain.port.out.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailAdapter.class);

    @Override
    public void sendInvitationEmail(String toEmail, String organizationName, String inviterName, String acceptUrl) {
        log.info("INVITATION EMAIL TO: {}, ORG: {}, LINK: {}", toEmail, organizationName, acceptUrl);
        log.info("Invited by: {}", inviterName);
    }
}
