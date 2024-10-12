package com.Appointment;

import com.Appointment.Services.NotificationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan
public class AppointmentApplication {

	@Autowired
	private final NotificationService notificationService;

    public AppointmentApplication(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public static void main(String[] args) {
	SpringApplication.run(AppointmentApplication.class, args);

	

}

	@PostConstruct
	public void init() {
		notificationService.testSendReminderEmails(); // Извиква тестовия метод
	}
}
