package com.taha.javabankapplication.service.impl;

import com.taha.javabankapplication.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
}
