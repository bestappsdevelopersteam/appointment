package com.Appointment.Repositories;

import com.Appointment.Entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByName(String name);
    List<Appointment> findByPhone(String phone);
    List<Appointment> findByAppointmentDate(LocalDate appointmentDate); // Търсене по дата


}
