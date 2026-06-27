package com.spring.boot.carro.usuarios.service.interfaces;

import java.io.File;

public interface IEmailService {

    void sendEmail(String[] toUser, String subject, String message);

    void sendEmailWiithFile(String[] toUser, String subject, String message, File file);
}
