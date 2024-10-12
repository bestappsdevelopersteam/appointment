package com.Appointment.Controllers;

import com.Appointment.Entities.Appointment;
import com.Appointment.Repositories.AppointmentRepository;
import com.Appointment.Services.ICalLink;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@Valid
@RequestMapping("/admin")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ICalLink iCalLink; // Добавяме ICalLink услугата

    @GetMapping("/appointments")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @GetMapping("/appointments/search")
    public ResponseEntity<List<Appointment>> searchAppointments(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) String phone) {
        List<Appointment> appointments;
        if (name != null && !name.isEmpty()) {
            appointments = appointmentRepository.findByName(name);
        } else if (phone != null && !phone.isEmpty()) {
            appointments = appointmentRepository.findByPhone(phone);
        } else {
            appointments = new ArrayList<>();
        }
        return ResponseEntity.ok(appointments);
    }

    @PostMapping("/appointments")
    public Map<String, String> createAppointment(@Valid @RequestBody Appointment appointment) {
        // Проверка за null стойности
        if (appointment.getName() == null || appointment.getName().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (appointment.getEmail() == null || appointment.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Проверка дали датата на прегледа не е в миналото
        LocalDate today = LocalDate.now();
        if (appointment.getAppointmentDate().isBefore(today)) {
            throw new IllegalArgumentException("Не може да създадете нов преглед със стара дата.");
        }

        // Запазване на прегледа в базата данни
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Генериране на Google Calendar линк
        String calendarLink = iCalLink.generateICalLink(savedAppointment);

        // Връщаме линка за календара в отговора
        Map<String, String> response = new HashMap<>();
        response.put("message", "Прегледът беше създаден успешно.");
        response.put("calendarLink", calendarLink); // Връщаме линка като част от JSON отговора

        return response;
    }

    // Метод за актуализиране на съществуващ преглед
    @PutMapping("/appointments/{id}")
    public Map<String, String> updateAppointment(@PathVariable Long id, @Valid @RequestBody Appointment appointmentDetails) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Преглед с ID " + id + " не е намерен"));

        // Обновяване на данните на съществуващия преглед
        existingAppointment.setName(appointmentDetails.getName());
        existingAppointment.setPhone(appointmentDetails.getPhone());
        existingAppointment.setEmail(appointmentDetails.getEmail());
        existingAppointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
        existingAppointment.setAppointmentTime(appointmentDetails.getAppointmentTime());
        existingAppointment.setAdditionalInfo(appointmentDetails.getAdditionalInfo());

        // Запазване на обновената среща
        appointmentRepository.save(existingAppointment);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Прегледът беше обновен успешно.");
        return response;
    }

    @DeleteMapping("/appointments/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
    }
}
