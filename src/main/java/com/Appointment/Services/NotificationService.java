package com.Appointment.Services;

import com.Appointment.Entities.Appointment;
import com.Appointment.Repositories.AppointmentRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Автоматично изпращане на напомняния в 8:00 всяка сутрин
    @Scheduled(cron = "0 0 8 * * *") // Формат: секунди, минути, час, ден, месец, ден от седмицата
    public void sendDailyReminders() {
        // Намираме срещите за днешния ден
        List<Appointment> todaysAppointments = appointmentRepository.findByAppointmentDate(LocalDate.parse(LocalDate.now().toString()));
        for (Appointment appointment : todaysAppointments) {
            sendReminder(appointment);
        }
    }

    private void sendReminder(Appointment appointment) {
        // Логика за изпращане на имейл
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(appointment.getEmail());
            helper.setSubject("Напомняне за преглед");
            helper.setText("Здравейте, имате преглед на " + appointment.getAppointmentTime() + " часа.");

            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void testSendReminderEmails() {
    }
}
