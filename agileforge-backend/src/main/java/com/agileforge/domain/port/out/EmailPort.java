package com.agileforge.domain.port.out;

public interface EmailPort {

    void sendInvitationEmail(String toEmail, String organizationName, String inviterName, String acceptUrl);
}
